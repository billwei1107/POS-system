/**
 * @file OrderVoidedEvent.java
 * @description 訂單作廢事件 / Order voided Spring event
 * @description_en Published when an order is voided; consumers should reverse inventory/points
 * @description_zh 訂單作廢後發布，消費者應回補庫存、取消累點等
 */
package com.enterprise.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class OrderVoidedEvent extends ApplicationEvent {

    private final UUID orderId;
    private final UUID storeId;
    private final String orderNo;
    private final String voidReason;

    public OrderVoidedEvent(Object source, UUID orderId, UUID storeId, String orderNo, String voidReason) {
        super(source);
        this.orderId = orderId;
        this.storeId = storeId;
        this.orderNo = orderNo;
        this.voidReason = voidReason;
    }
}
