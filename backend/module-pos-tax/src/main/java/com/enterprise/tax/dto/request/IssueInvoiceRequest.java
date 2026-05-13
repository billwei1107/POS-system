/**
 * @file IssueInvoiceRequest.java
 * @description 開立電子發票請求 DTO / Issue invoice request DTO
 * @description_en Request payload for manually issuing an invoice (auto-issue uses OrderCompletedEvent)
 * @description_zh 手動開立電子發票的請求資料（自動開立透過 OrderCompletedEvent 觸發）
 */
package com.enterprise.tax.dto.request;

import com.enterprise.tax.entity.Invoice;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IssueInvoiceRequest(
        @NotNull UUID storeId,
        @NotNull UUID orderId,
        Invoice.InvoiceType invoiceType,
        String buyerId,
        String buyerName,
        String buyerEmail,
        Invoice.CarrierType carrierType,
        String carrierId1,
        String carrierId2,
        String donateCode
) {
    public IssueInvoiceRequest {
        if (invoiceType == null) invoiceType = Invoice.InvoiceType.B2C;
    }
}
