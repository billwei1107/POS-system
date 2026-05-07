/**
 * @file OrderItemRequest.java
 * @description 訂單明細請求 DTO / Order item request DTO
 * @description_en Carries item data for order creation including modifiers
 * @description_zh 建立訂單時的商品明細資料，含客製化選項
 */
package com.enterprise.core.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderItemRequest(
    @NotNull UUID itemId,
    UUID variantId,
    @NotNull String itemNameSnapshot,
    String skuSnapshot,
    @NotNull @Positive BigDecimal unitPrice,
    @NotNull @Positive BigDecimal quantity,
    BigDecimal modifierPriceAdjustment,
    String note,
    List<OrderItemModifierRequest> modifiers
) {}
