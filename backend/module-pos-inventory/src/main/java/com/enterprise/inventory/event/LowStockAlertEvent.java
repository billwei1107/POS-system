/**
 * @file LowStockAlertEvent.java
 * @description 低庫存警示事件 / Low stock alert Spring event
 * @description_en Published when stock falls below reorder point; consumed by notification/supplier modules
 * @description_zh 庫存低於補貨點時發布，供通知模組與供應商模組消費
 */
package com.enterprise.inventory.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class LowStockAlertEvent extends ApplicationEvent {

    private final UUID storeId;
    private final UUID itemId;
    private final BigDecimal currentQty;
    private final BigDecimal reorderPoint;

    public LowStockAlertEvent(Object source, UUID storeId, UUID itemId,
                               BigDecimal currentQty, BigDecimal reorderPoint) {
        super(source);
        this.storeId = storeId;
        this.itemId = itemId;
        this.currentQty = currentQty;
        this.reorderPoint = reorderPoint;
    }
}
