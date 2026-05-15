/**
 * @file PriceRuleResponse.java
 * @description 價格規則回應 DTO / Price rule response DTO
 * @description_en API response projection for item price rules
 * @description_zh 商品價格規則 API 回應資料
 */
package com.enterprise.product.dto;

import com.enterprise.product.entity.PriceRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PriceRuleResponse(
        UUID id,
        UUID itemId,
        UUID storeId,
        PriceRule.PriceType priceType,
        BigDecimal price,
        Integer minQty,
        LocalDateTime effectiveFrom,
        LocalDateTime effectiveTo,
        Boolean active
) {
    public static PriceRuleResponse from(PriceRule rule) {
        return new PriceRuleResponse(
                rule.getId(),
                rule.getItemId(),
                rule.getStoreId(),
                rule.getPriceType(),
                rule.getPrice(),
                rule.getMinQty(),
                rule.getEffectiveFrom(),
                rule.getEffectiveTo(),
                rule.getActive()
        );
    }
}
