/**
 * @file PromotionEvaluationRequest.java
 * @description 促銷試算請求 DTO / Promotion evaluation request DTO
 * @description_en Request payload for calculating the best applicable order promotion
 * @description_zh 計算訂單可用最佳促銷的請求資料
 */
package com.enterprise.promotion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PromotionEvaluationRequest(
        @NotNull(message = "門店 ID 不得為空")
        UUID storeId,

        @NotNull(message = "訂單小計不得為空")
        @DecimalMin(value = "0.00", message = "訂單小計不得為負數")
        BigDecimal subtotal,

        String code,

        LocalDateTime orderedAt
) {
}
