/**
 * @file PromotionService.java
 * @description POS 促銷服務 / POS promotion service
 * @description_en Handles promotion rule management and best-discount evaluation
 * @description_zh 處理促銷規則管理與最佳折扣試算
 */
package com.enterprise.promotion.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.promotion.dto.PromotionEvaluationRequest;
import com.enterprise.promotion.dto.PromotionEvaluationResponse;
import com.enterprise.promotion.dto.PromotionRuleRequest;
import com.enterprise.promotion.dto.PromotionRuleResponse;
import com.enterprise.promotion.entity.PromotionRule;
import com.enterprise.promotion.repository.PromotionRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private static final int MONEY_SCALE = 2;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final PromotionRuleRepository promotionRuleRepository;

    // ========================================
    // 查詢促銷規則 / Query promotion rules
    // ========================================
    @Transactional(readOnly = true)
    public List<PromotionRuleResponse> list(UUID storeId) {
        List<PromotionRule> rules = storeId == null
                ? promotionRuleRepository.findByStoreIdIsNullAndActiveTrue()
                : activeRulesForStore(storeId);
        return rules.stream()
                .sorted(Comparator.comparing(PromotionRule::getName))
                .map(PromotionRuleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UUID findStoreId(UUID id) {
        return getOrThrow(id).getStoreId();
    }

    // ========================================
    // 新增 / 更新 / 停用 / Create / Update / Deactivate
    // ========================================
    @Transactional
    public PromotionRuleResponse create(PromotionRuleRequest request) {
        PromotionRule rule = new PromotionRule();
        applyRequest(rule, request);
        return PromotionRuleResponse.from(promotionRuleRepository.save(rule));
    }

    @Transactional
    public PromotionRuleResponse update(UUID id, PromotionRuleRequest request) {
        PromotionRule rule = getOrThrow(id);
        applyRequest(rule, request);
        return PromotionRuleResponse.from(promotionRuleRepository.save(rule));
    }

    @Transactional
    public void deactivate(UUID id) {
        PromotionRule rule = getOrThrow(id);
        rule.setActive(false);
        promotionRuleRepository.save(rule);
    }

    // ========================================
    // 試算最佳促銷 / Evaluate best promotion
    // ========================================
    @Transactional(readOnly = true)
    public PromotionEvaluationResponse evaluate(PromotionEvaluationRequest request) {
        BigDecimal subtotal = money(request.subtotal());
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return PromotionEvaluationResponse.none("ORDER_SUBTOTAL_IS_ZERO");
        }

        String code = normalizeCode(request.code());
        LocalDateTime orderedAt = request.orderedAt() != null ? request.orderedAt() : LocalDateTime.now();
        return activeRulesForStore(request.storeId()).stream()
                .filter(rule -> isApplicable(rule, subtotal, code, orderedAt))
                .map(rule -> new RuleEvaluation(rule, discountAmount(rule, subtotal)))
                .filter(evaluation -> evaluation.discountAmount().compareTo(BigDecimal.ZERO) > 0)
                .max(Comparator.comparing(RuleEvaluation::discountAmount))
                .map(evaluation -> PromotionEvaluationResponse.applied(
                        evaluation.rule(), evaluation.discountAmount()
                ))
                .orElseGet(() -> PromotionEvaluationResponse.none("NO_APPLICABLE_PROMOTION"));
    }

    private List<PromotionRule> activeRulesForStore(UUID storeId) {
        List<PromotionRule> globalRules = promotionRuleRepository.findByStoreIdIsNullAndActiveTrue();
        if (storeId == null) {
            return globalRules;
        }
        List<PromotionRule> storeRules = promotionRuleRepository.findByStoreIdAndActiveTrue(storeId);
        return java.util.stream.Stream.concat(globalRules.stream(), storeRules.stream()).toList();
    }

    private boolean isApplicable(PromotionRule rule, BigDecimal subtotal, String code, LocalDateTime orderedAt) {
        if (!Boolean.TRUE.equals(rule.getActive())) {
            return false;
        }
        if (rule.getStartsAt() != null && orderedAt.isBefore(rule.getStartsAt())) {
            return false;
        }
        if (rule.getEndsAt() != null && orderedAt.isAfter(rule.getEndsAt())) {
            return false;
        }
        if (subtotal.compareTo(money(rule.getMinimumSubtotal())) < 0) {
            return false;
        }
        if (rule.getTriggerType() == PromotionRule.TriggerType.CODE) {
            return code != null && code.equals(normalizeCode(rule.getCode()));
        }
        return code == null;
    }

    private BigDecimal discountAmount(PromotionRule rule, BigDecimal subtotal) {
        BigDecimal rawDiscount = switch (rule.getDiscountType()) {
            case AMOUNT -> money(rule.getDiscountValue());
            case PERCENT -> subtotal
                    .multiply(rule.getDiscountValue())
                    .divide(ONE_HUNDRED, MONEY_SCALE, RoundingMode.HALF_UP);
        };
        BigDecimal capped = rule.getMaxDiscountAmount() != null
                ? rawDiscount.min(money(rule.getMaxDiscountAmount()))
                : rawDiscount;
        return capped.min(subtotal).max(BigDecimal.ZERO).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private void applyRequest(PromotionRule rule, PromotionRuleRequest request) {
        if (request.startsAt() != null && request.endsAt() != null && request.startsAt().isAfter(request.endsAt())) {
            throw new BusinessException("促銷開始時間不可晚於結束時間");
        }
        PromotionRule.TriggerType triggerType = request.triggerType();
        String code = normalizeCode(request.code());
        if (triggerType == PromotionRule.TriggerType.CODE && code == null) {
            throw new BusinessException("優惠碼促銷必須提供 code");
        }
        if (triggerType == PromotionRule.TriggerType.AUTO && code != null) {
            throw new BusinessException("自動促銷不可設定優惠碼");
        }
        if (request.discountType() == PromotionRule.DiscountType.PERCENT
                && request.discountValue().compareTo(ONE_HUNDRED) > 0) {
            throw new BusinessException("百分比折扣不得超過 100");
        }

        rule.setStoreId(request.storeId());
        rule.setName(request.name().trim());
        rule.setCode(code);
        rule.setTriggerType(triggerType);
        rule.setDiscountType(request.discountType());
        rule.setDiscountValue(money(request.discountValue()));
        rule.setMinimumSubtotal(money(request.minimumSubtotal()));
        rule.setMaxDiscountAmount(request.maxDiscountAmount() != null ? money(request.maxDiscountAmount()) : null);
        rule.setStartsAt(request.startsAt());
        rule.setEndsAt(request.endsAt());
        rule.setActive(request.active() != null ? request.active() : true);
    }

    private PromotionRule getOrThrow(UUID id) {
        return promotionRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromotionRule not found: " + id));
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(MONEY_SCALE) : value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return code.trim().toUpperCase();
    }

    private record RuleEvaluation(PromotionRule rule, BigDecimal discountAmount) {
    }
}
