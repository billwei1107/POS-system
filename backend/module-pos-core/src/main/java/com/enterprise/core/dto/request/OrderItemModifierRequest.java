/**
 * @file OrderItemModifierRequest.java
 * @description 客製化選項請求 DTO / Order item modifier request DTO
 * @description_en Carries modifier selection snapshot for order item
 * @description_zh 下單時的客製化選項快照
 */
package com.enterprise.core.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemModifierRequest(
    @NotNull UUID modifierGroupId,
    @NotNull UUID modifierId,
    @NotNull String modifierNameSnapshot,
    BigDecimal priceAdjustment
) {}
