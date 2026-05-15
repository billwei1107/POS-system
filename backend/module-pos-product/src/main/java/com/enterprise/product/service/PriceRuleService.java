/**
 * @file PriceRuleService.java
 * @description 價格規則業務邏輯層 / Price rule service
 * @description_en Handles CRUD and validation for product price rules
 * @description_zh 處理商品價格規則 CRUD 與驗證
 */
package com.enterprise.product.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.product.dto.PriceRuleRequest;
import com.enterprise.product.dto.PriceRuleResponse;
import com.enterprise.product.entity.PriceRule;
import com.enterprise.product.repository.PriceRuleRepository;
import com.enterprise.product.repository.ProductItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PriceRuleService {

    private final PriceRuleRepository priceRuleRepository;
    private final ProductItemRepository productItemRepository;

    // ========================================
    // 查詢 / Query
    // ========================================
    @Transactional(readOnly = true)
    public List<PriceRuleResponse> listByItem(UUID itemId, UUID storeId) {
        ensureProductExists(itemId);
        List<PriceRule> rules = storeId == null
                ? priceRuleRepository.findByItemIdAndStoreIdIsNullAndActiveTrue(itemId)
                : priceRuleRepository.findByItemIdAndStoreIdAndActiveTrue(itemId, storeId);
        return rules.stream().map(PriceRuleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PriceRuleResponse> listGlobalByItem(UUID itemId) {
        ensureProductExists(itemId);
        return priceRuleRepository.findByItemIdAndStoreIdIsNullAndActiveTrue(itemId)
                .stream()
                .map(PriceRuleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UUID findStoreId(UUID id) {
        return getOrThrow(id).getStoreId();
    }

    // ========================================
    // 新增/修改/刪除 / Create / Update / Delete
    // ========================================
    @Transactional
    public PriceRuleResponse create(PriceRuleRequest request) {
        ensureProductExists(request.itemId());
        PriceRule rule = new PriceRule();
        applyRequest(rule, request);
        return PriceRuleResponse.from(priceRuleRepository.save(rule));
    }

    @Transactional
    public PriceRuleResponse update(UUID id, PriceRuleRequest request) {
        ensureProductExists(request.itemId());
        PriceRule rule = getOrThrow(id);
        applyRequest(rule, request);
        return PriceRuleResponse.from(priceRuleRepository.save(rule));
    }

    @Transactional
    public void deactivate(UUID id) {
        PriceRule rule = getOrThrow(id);
        rule.setActive(false);
        priceRuleRepository.save(rule);
    }

    private PriceRule getOrThrow(UUID id) {
        return priceRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PriceRule not found: " + id));
    }

    private void ensureProductExists(UUID itemId) {
        if (!productItemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("ProductItem not found: " + itemId);
        }
    }

    private void applyRequest(PriceRule rule, PriceRuleRequest request) {
        if (request.effectiveFrom() != null
                && request.effectiveTo() != null
                && request.effectiveFrom().isAfter(request.effectiveTo())) {
            throw new BusinessException("價格規則起始時間不可晚於結束時間");
        }
        rule.setItemId(request.itemId());
        rule.setStoreId(request.storeId());
        rule.setPriceType(request.priceType());
        rule.setPrice(request.price());
        rule.setMinQty(request.minQty() != null ? request.minQty() : 1);
        rule.setEffectiveFrom(request.effectiveFrom());
        rule.setEffectiveTo(request.effectiveTo());
        rule.setActive(request.active() != null ? request.active() : true);
    }
}
