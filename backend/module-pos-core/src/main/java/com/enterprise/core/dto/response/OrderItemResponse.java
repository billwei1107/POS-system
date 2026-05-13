/**
 * @file OrderItemResponse.java
 * @description 訂單明細回應 DTO / Order item response DTO
 * @description_en Response payload for a single order line item
 * @description_zh 單筆訂單明細回應
 */
package com.enterprise.core.dto.response;

import com.enterprise.core.entity.OrderItem;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
    UUID id,
    UUID itemId,
    UUID variantId,
    String itemNameSnapshot,
    String skuSnapshot,
    BigDecimal unitPrice,
    BigDecimal quantity,
    BigDecimal discountAmount,
    BigDecimal lineTotal,
    String note
) {
    public static OrderItemResponse from(OrderItem oi) {
        return new OrderItemResponse(
            oi.getId(), oi.getItemId(), oi.getVariantId(),
            oi.getItemNameSnapshot(), oi.getSkuSnapshot(),
            oi.getUnitPrice(), oi.getQuantity(), oi.getDiscountAmount(),
            oi.getLineTotal(), oi.getNote()
        );
    }
}
