/**
 * @file PaymentProcessedEvent.java
 * @description 支付完成事件 / Payment processed Spring event
 * @description_en Published after a payment transaction is successfully recorded; consumed by staff/reporting modules
 * @description_zh 支付交易成功記錄後發布，供排班統計、報表、Z Report 等模組消費
 */
package com.enterprise.payment.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class PaymentProcessedEvent extends ApplicationEvent {

    private final UUID transactionId;
    private final UUID orderId;
    private final UUID storeId;
    private final UUID terminalId;
    private final UUID employeeId;
    private final String orderNo;
    private final String methodType;
    private final BigDecimal amount;

    public PaymentProcessedEvent(Object source, UUID transactionId, UUID orderId,
                                  UUID storeId, String orderNo, String methodType, BigDecimal amount) {
        this(source, transactionId, orderId, storeId, null, null, orderNo, methodType, amount);
    }

    public PaymentProcessedEvent(Object source, UUID transactionId, UUID orderId,
                                  UUID storeId, UUID terminalId, UUID employeeId,
                                  String orderNo, String methodType, BigDecimal amount) {
        super(source);
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.storeId = storeId;
        this.terminalId = terminalId;
        this.employeeId = employeeId;
        this.orderNo = orderNo;
        this.methodType = methodType;
        this.amount = amount;
    }
}
