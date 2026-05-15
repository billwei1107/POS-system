/**
 * @file PriceRuleRequest.java
 * @description 價格規則新增/修改請求 DTO / Price rule create/update request DTO
 * @description_en Request payload for creating or updating item price rules
 * @description_zh 建立或更新商品價格規則的請求資料
 */
package com.enterprise.product.dto;

import com.enterprise.product.entity.PriceRule;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PriceRuleRequest(
        @NotNull(message = "商品 ID 不得為空")
        UUID itemId,

        UUID storeId,

        @NotNull(message = "價格類型不得為空")
        PriceRule.PriceType priceType,

        @NotNull(message = "價格不得為空")
        @DecimalMin(value = "0.00", message = "價格不得為負數")
        BigDecimal price,

        @Min(value = 1, message = "最低數量至少為 1")
        Integer minQty,

        LocalDateTime effectiveFrom,

        LocalDateTime effectiveTo,

        Boolean active
) {
}
