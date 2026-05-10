/**
 * @file HeldOrderResponse.java
 * @description 掛單回應 / Held order response
 * @description_en Response DTO for held POS orders
 * @description_zh 回傳掛單識別、門店、終端機與序列化購物車內容
 */
package com.enterprise.core.dto.response;

import com.enterprise.core.entity.HeldOrder;

import java.time.LocalDateTime;
import java.util.UUID;

public record HeldOrderResponse(
    UUID id,
    UUID storeId,
    UUID terminalId,
    String label,
    String payload,
    LocalDateTime heldAt,
    LocalDateTime createdAt
) {
    public static HeldOrderResponse from(HeldOrder heldOrder) {
        return new HeldOrderResponse(
            heldOrder.getId(),
            heldOrder.getStoreId(),
            heldOrder.getTerminalId(),
            heldOrder.getLabel(),
            heldOrder.getPayload(),
            heldOrder.getHeldAt(),
            heldOrder.getCreatedAt()
        );
    }
}
