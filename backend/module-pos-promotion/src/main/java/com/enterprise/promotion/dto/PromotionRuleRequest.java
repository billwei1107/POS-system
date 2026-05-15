/**
 * @file PromotionRuleRequest.java
 * @description 促銷規則請求 DTO / Promotion rule request DTO
 * @description_en Request payload for creating or updating promotion rules
 * @description_zh 建立或更新促銷規則的請求資料
 */
package com.enterprise.promotion.dto;

import com.enterprise.promotion.entity.PromotionRule;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PromotionRuleRequest(
        UUID storeId,

        @NotBlank(message = "促銷名稱不得為空")
        String name,

        String code,

        @NotNull(message = "觸發類型不得為空")
        PromotionRule.TriggerType triggerType,

        @NotNull(message = "折扣類型不得為空")
        PromotionRule.DiscountType discountType,

        @NotNull(message = "折扣值不得為空")
        @DecimalMin(value = "0.01", message = "折扣值必須大於 0")
        BigDecimal discountValue,

        @DecimalMin(value = "0.00", message = "最低小計不得為負數")
        BigDecimal minimumSubtotal,

        @DecimalMin(value = "0.00", message = "最高折抵不得為負數")
        BigDecimal maxDiscountAmount,

        LocalDateTime startsAt,

        LocalDateTime endsAt,

        Boolean active
) {
}
