/**
 * @file PromotionRuleResponse.java
 * @description 促銷規則回應 DTO / Promotion rule response DTO
 * @description_en API projection for promotion rules
 * @description_zh 促銷規則 API 回應資料
 */
package com.enterprise.promotion.dto;

import com.enterprise.promotion.entity.PromotionRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PromotionRuleResponse(
        UUID id,
        UUID storeId,
        String name,
        String code,
        PromotionRule.TriggerType triggerType,
        PromotionRule.DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumSubtotal,
        BigDecimal maxDiscountAmount,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        Boolean active
) {
    public static PromotionRuleResponse from(PromotionRule rule) {
        return new PromotionRuleResponse(
                rule.getId(),
                rule.getStoreId(),
                rule.getName(),
                rule.getCode(),
                rule.getTriggerType(),
                rule.getDiscountType(),
                rule.getDiscountValue(),
                rule.getMinimumSubtotal(),
                rule.getMaxDiscountAmount(),
                rule.getStartsAt(),
                rule.getEndsAt(),
                rule.getActive()
        );
    }
}
