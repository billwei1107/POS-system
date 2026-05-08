/**
 * @file StoreStockResponse.java
 * @description 門店庫存回應 DTO / Store stock response DTO
 * @description_en Response DTO for store-level stock information
 * @description_zh 門店庫存資訊回應 DTO
 */
package com.enterprise.inventory.dto.response;

import com.enterprise.inventory.entity.StoreStock;

import java.math.BigDecimal;
import java.util.UUID;

public record StoreStockResponse(
        UUID id,
        UUID storeId,
        UUID itemId,
        BigDecimal quantity,
        BigDecimal reservedQuantity,
        BigDecimal availableQuantity,
        BigDecimal reorderPoint,
        BigDecimal reorderQuantity
) {
    public static StoreStockResponse from(StoreStock s) {
        return new StoreStockResponse(
                s.getId(), s.getStoreId(), s.getItemId(),
                s.getQuantity(), s.getReservedQuantity(),
                s.getAvailableQuantity(), s.getReorderPoint(), s.getReorderQuantity()
        );
    }
}
