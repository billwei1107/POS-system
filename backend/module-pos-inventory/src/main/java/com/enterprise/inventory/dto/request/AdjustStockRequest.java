/**
 * @file AdjustStockRequest.java
 * @description 手動調整庫存請求 DTO / Manual stock adjustment request DTO
 * @description_en Request body for manual stock quantity adjustment (positive=add, negative=subtract)
 * @description_zh 手動調整庫存數量請求體（正數為增加，負數為扣減）
 */
package com.enterprise.inventory.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AdjustStockRequest(
        @NotNull UUID storeId,
        @NotNull UUID itemId,
        @NotNull BigDecimal adjustQty,
        UUID operatedBy,
        String notes
) {}
