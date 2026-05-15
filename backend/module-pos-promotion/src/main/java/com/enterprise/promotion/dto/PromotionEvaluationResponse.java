/**
 * @file PromotionEvaluationResponse.java
 * @description 促銷試算回應 DTO / Promotion evaluation response DTO
 * @description_en Result of evaluating order-level promotion rules
 * @description_zh 訂單層級促銷規則試算結果
 */
package com.enterprise.promotion.dto;

import com.enterprise.promotion.entity.PromotionRule;

import java.math.BigDecimal;
import java.util.UUID;

public record PromotionEvaluationResponse(
        boolean applied,
        UUID ruleId,
        String name,
        String code,
        PromotionRule.DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal discountAmount,
        String reason
) {
    public static PromotionEvaluationResponse none(String reason) {
        return new PromotionEvaluationResponse(
                false, null, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO, reason
        );
    }

    public static PromotionEvaluationResponse applied(PromotionRule rule, BigDecimal discountAmount) {
        return new PromotionEvaluationResponse(
                true,
                rule.getId(),
                rule.getName(),
                rule.getCode(),
                rule.getDiscountType(),
                rule.getDiscountValue(),
                discountAmount,
                null
        );
    }
}
