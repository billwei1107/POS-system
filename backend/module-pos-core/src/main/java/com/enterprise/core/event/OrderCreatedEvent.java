/**
 * @file OrderCreatedEvent.java
 * @description 訂單建立事件 / Order created Spring event
 * @description_en Published when a new order is successfully created in DRAFT status
 * @description_zh 訂單成功建立（DRAFT 狀態）時發布，供其他模組監聽
 */
package com.enterprise.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class OrderCreatedEvent extends ApplicationEvent {

    private final UUID orderId;
    private final UUID storeId;
    private final String orderNo;

    public OrderCreatedEvent(Object source, UUID orderId, UUID storeId, String orderNo) {
        super(source);
        this.orderId = orderId;
        this.storeId = storeId;
        this.orderNo = orderNo;
    }
}
