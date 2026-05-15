/**
 * @file PromotionServiceTest.java
 * @description 促銷服務測試 / Promotion service tests
 * @description_en Verifies best promotion evaluation, code matching and rule validation
 * @description_zh 驗證最佳促銷試算、優惠碼匹配與規則驗證
 */
package com.enterprise.promotion.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.promotion.dto.PromotionEvaluationRequest;
import com.enterprise.promotion.dto.PromotionRuleRequest;
import com.enterprise.promotion.entity.PromotionRule;
import com.enterprise.promotion.repository.PromotionRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRuleRepository promotionRuleRepository;

    private PromotionService promotionService;

    @BeforeEach
    void setUp() {
        promotionService = new PromotionService(promotionRuleRepository);
    }

    @Test
    void evaluate_autoRules_usesHighestDiscountAndCapsAmount() {
        UUID storeId = UUID.randomUUID();
        PromotionRule percentRule = rule("滿百 9 折", null, PromotionRule.TriggerType.AUTO,
                PromotionRule.DiscountType.PERCENT, "10.00", "100.00", "12.00");
        PromotionRule amountRule = rule("滿百折 20", null, PromotionRule.TriggerType.AUTO,
                PromotionRule.DiscountType.AMOUNT, "20.00", "100.00", null);

        when(promotionRuleRepository.findByStoreIdIsNullAndActiveTrue()).thenReturn(List.of(percentRule, amountRule));
        when(promotionRuleRepository.findByStoreIdAndActiveTrue(storeId)).thenReturn(List.of());

        var response = promotionService.evaluate(new PromotionEvaluationRequest(
                storeId, new BigDecimal("180.00"), null, LocalDateTime.now()
        ));

        assertThat(response.applied()).isTrue();
        assertThat(response.name()).isEqualTo("滿百折 20");
        assertThat(response.discountAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void evaluate_codeRule_requiresMatchingCode() {
        UUID storeId = UUID.randomUUID();
        PromotionRule codeRule = rule("優惠碼 CAFE20", "CAFE20", PromotionRule.TriggerType.CODE,
                PromotionRule.DiscountType.AMOUNT, "20.00", "120.00", null);

        when(promotionRuleRepository.findByStoreIdIsNullAndActiveTrue()).thenReturn(List.of(codeRule));
        when(promotionRuleRepository.findByStoreIdAndActiveTrue(storeId)).thenReturn(List.of());

        var withoutCode = promotionService.evaluate(new PromotionEvaluationRequest(
                storeId, new BigDecimal("180.00"), null, LocalDateTime.now()
        ));
        var withCode = promotionService.evaluate(new PromotionEvaluationRequest(
                storeId, new BigDecimal("180.00"), "cafe20", LocalDateTime.now()
        ));

        assertThat(withoutCode.applied()).isFalse();
        assertThat(withCode.applied()).isTrue();
        assertThat(withCode.discountAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void create_autoRuleWithCode_throwsBusinessException() {
        PromotionRuleRequest request = new PromotionRuleRequest(
                null,
                "自動折扣",
                "AUTO10",
                PromotionRule.TriggerType.AUTO,
                PromotionRule.DiscountType.PERCENT,
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                null,
                null,
                null,
                true
        );

        assertThatThrownBy(() -> promotionService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("自動促銷不可設定優惠碼");
    }

    private PromotionRule rule(String name, String code, PromotionRule.TriggerType triggerType,
                               PromotionRule.DiscountType discountType, String discountValue,
                               String minimumSubtotal, String maxDiscountAmount) {
        PromotionRule rule = new PromotionRule();
        rule.setId(UUID.randomUUID());
        rule.setName(name);
        rule.setCode(code);
        rule.setTriggerType(triggerType);
        rule.setDiscountType(discountType);
        rule.setDiscountValue(new BigDecimal(discountValue));
        rule.setMinimumSubtotal(new BigDecimal(minimumSubtotal));
        rule.setMaxDiscountAmount(maxDiscountAmount != null ? new BigDecimal(maxDiscountAmount) : null);
        rule.setActive(true);
        return rule;
    }
}
