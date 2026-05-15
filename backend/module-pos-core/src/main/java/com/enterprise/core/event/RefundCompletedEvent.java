/**
 * @file RefundCompletedEvent.java
 * @description 退款完成事件 / Refund completed Spring event
 * @description_en Published when a refund is fully processed; consumers should reverse stock/invoice
 * @description_zh 退款完成後發布，供庫存回補、發票作廢等模組消費
 */
package com.enterprise.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class RefundCompletedEvent extends ApplicationEvent {

    private final UUID refundId;
    private final UUID orderId;
    private final UUID storeId;
    private final UUID memberId;
    private final BigDecimal refundAmount;
    private final BigDecimal orderGrandTotal;
    private final BigDecimal completedRefundTotal;
    private final String refundMethod;

    public RefundCompletedEvent(Object source, UUID refundId, UUID orderId,
                                UUID storeId, BigDecimal refundAmount) {
        this(source, refundId, orderId, storeId, null, refundAmount, null, null, null);
    }

    public RefundCompletedEvent(Object source, UUID refundId, UUID orderId,
                                UUID storeId, BigDecimal refundAmount,
                                BigDecimal orderGrandTotal, String refundMethod) {
        this(source, refundId, orderId, storeId, null, refundAmount, orderGrandTotal, refundAmount, refundMethod);
    }

    public RefundCompletedEvent(Object source, UUID refundId, UUID orderId,
                                UUID storeId, UUID memberId, BigDecimal refundAmount,
                                BigDecimal orderGrandTotal, BigDecimal completedRefundTotal,
                                String refundMethod) {
        super(source);
        this.refundId = refundId;
        this.orderId = orderId;
        this.storeId = storeId;
        this.memberId = memberId;
        this.refundAmount = refundAmount;
        this.orderGrandTotal = orderGrandTotal;
        this.completedRefundTotal = completedRefundTotal;
        this.refundMethod = refundMethod;
    }
}
