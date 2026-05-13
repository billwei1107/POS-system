/**
 * @file InvoiceIssuedEvent.java
 * @description 電子發票開立事件 / Invoice issued Spring event
 * @description_en Published when an e-invoice is successfully issued; consumers can trigger print/email
 * @description_zh 電子發票成功開立後發布，供列印、Email 傳送等模組消費
 */
package com.enterprise.tax.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class InvoiceIssuedEvent extends ApplicationEvent {

    private final UUID invoiceId;
    private final UUID orderId;
    private final UUID storeId;
    private final String fullInvoiceNo;
    private final BigDecimal totalAmount;

    public InvoiceIssuedEvent(Object source, UUID invoiceId, UUID orderId, UUID storeId,
                               String fullInvoiceNo, BigDecimal totalAmount) {
        super(source);
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.storeId = storeId;
        this.fullInvoiceNo = fullInvoiceNo;
        this.totalAmount = totalAmount;
    }
}
