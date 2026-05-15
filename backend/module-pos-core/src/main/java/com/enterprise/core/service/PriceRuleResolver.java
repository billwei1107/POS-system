/**
 * @file PriceRuleResolver.java
 * @description 訂單價格規則解析器 / Order price rule resolver
 * @description_en Resolves effective product unit prices from product master data and active price rules
 * @description_zh 依商品主檔與啟用中的價格規則解析訂單實際單價
 */
package com.enterprise.core.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.core.dto.request.OrderItemRequest;
import com.enterprise.product.entity.PriceRule;
import com.enterprise.product.entity.ProductItem;
import com.enterprise.product.repository.PriceRuleRepository;
import com.enterprise.product.repository.ProductItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PriceRuleResolver {

    private final ProductItemRepository productItemRepository;
    private final PriceRuleRepository priceRuleRepository;

    // ========================================
    // 訂單明細重新定價 / Reprice order lines
    // ========================================
    public List<OrderItemRequest> resolveOrderItems(List<OrderItemRequest> items,
                                                     UUID storeId,
                                                     UUID memberId,
                                                     LocalDateTime orderedAt) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        LocalDateTime effectiveAt = orderedAt != null ? orderedAt : LocalDateTime.now();
        return items.stream()
                .map(item -> resolveOrderItem(item, storeId, memberId, effectiveAt))
                .toList();
    }

    private OrderItemRequest resolveOrderItem(OrderItemRequest item,
                                              UUID storeId,
                                              UUID memberId,
                                              LocalDateTime orderedAt) {
        ProductItem product = productItemRepository.findById(item.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.itemId()));
        if (!Boolean.TRUE.equals(product.getActive()) || !Boolean.TRUE.equals(product.getSellable())) {
            throw new BusinessException(400, "Product is not sellable: " + item.itemId());
        }

        BigDecimal effectiveUnitPrice = resolveUnitPrice(product, item.quantity(), storeId, memberId, orderedAt);
        return new OrderItemRequest(
                item.itemId(),
                item.variantId(),
                product.getName(),
                product.getSku(),
                effectiveUnitPrice,
                item.quantity(),
                item.modifierPriceAdjustment(),
                item.note(),
                item.modifiers()
        );
    }

    private BigDecimal resolveUnitPrice(ProductItem product,
                                        BigDecimal quantity,
                                        UUID storeId,
                                        UUID memberId,
                                        LocalDateTime orderedAt) {
        List<PriceRule> candidates = new ArrayList<>();
        candidates.addAll(priceRuleRepository.findByItemIdAndStoreIdIsNullAndActiveTrue(product.getId()));
        if (storeId != null) {
            candidates.addAll(priceRuleRepository.findByItemIdAndStoreIdAndActiveTrue(product.getId(), storeId));
        }

        return candidates.stream()
                .filter(rule -> isApplicable(rule, quantity, memberId, orderedAt))
                .min(Comparator.comparing(PriceRule::getPrice))
                .map(PriceRule::getPrice)
                .orElse(product.getBasePrice());
    }

    private boolean isApplicable(PriceRule rule,
                                 BigDecimal quantity,
                                 UUID memberId,
                                 LocalDateTime orderedAt) {
        if (rule.getEffectiveFrom() != null && orderedAt.isBefore(rule.getEffectiveFrom())) {
            return false;
        }
        if (rule.getEffectiveTo() != null && orderedAt.isAfter(rule.getEffectiveTo())) {
            return false;
        }
        if (rule.getMinQty() != null && quantity.compareTo(BigDecimal.valueOf(rule.getMinQty())) < 0) {
            return false;
        }
        return switch (rule.getPriceType()) {
            case MEMBER -> memberId != null;
            case STAFF -> false;
            case BASE, HAPPY_HOUR, BULK -> true;
        };
    }
}
