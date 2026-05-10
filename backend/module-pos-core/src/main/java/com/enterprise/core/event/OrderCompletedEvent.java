/**
 * @file OrderCompletedEvent.java
 * @description 訂單完成事件 / Order completed Spring event
 * @description_en Published when an order reaches COMPLETED status (payment received)
 * @description_zh 訂單完成付款後發布，供庫存扣減、開發票、累點等模組消費
 */
package com.enterprise.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class OrderCompletedEvent extends ApplicationEvent {

    private final UUID orderId;
    private final UUID storeId;
    private final String orderNo;
    private final UUID memberId;
    private final BigDecimal grandTotal;
    private final BigDecimal taxAmount;
    private final String payMethod;
    private final BigDecimal paidAmount;
    private final BigDecimal tenderedAmount;
    private final BigDecimal changeGiven;

    public OrderCompletedEvent(Object source, UUID orderId, UUID storeId, String orderNo,
                               UUID memberId, BigDecimal grandTotal) {
        this(source, orderId, storeId, orderNo, memberId, grandTotal, null, null, grandTotal, null, BigDecimal.ZERO);
    }

    public OrderCompletedEvent(Object source, UUID orderId, UUID storeId, String orderNo,
                               UUID memberId, BigDecimal grandTotal, String payMethod,
                               BigDecimal paidAmount, BigDecimal tenderedAmount,
                               BigDecimal changeGiven) {
        this(source, orderId, storeId, orderNo, memberId, grandTotal, null, payMethod,
                paidAmount, tenderedAmount, changeGiven);
    }

    public OrderCompletedEvent(Object source, UUID orderId, UUID storeId, String orderNo,
                               UUID memberId, BigDecimal grandTotal, BigDecimal taxAmount,
                               String payMethod, BigDecimal paidAmount, BigDecimal tenderedAmount,
                               BigDecimal changeGiven) {
        super(source);
        this.orderId = orderId;
        this.storeId = storeId;
        this.orderNo = orderNo;
        this.memberId = memberId;
        this.grandTotal = grandTotal;
        this.taxAmount = taxAmount;
        this.payMethod = payMethod;
        this.paidAmount = paidAmount;
        this.tenderedAmount = tenderedAmount;
        this.changeGiven = changeGiven;
    }
}
