/**
 * @file InvoiceResponse.java
 * @description 電子發票回應 DTO / Invoice response DTO
 * @description_en Flattened invoice view returned from API; excludes sensitive upload response body
 * @description_zh 電子發票 API 回傳的扁平化視圖，不含上傳回應原文
 */
package com.enterprise.tax.dto.response;

import com.enterprise.tax.entity.Invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID storeId,
        UUID orderId,
        String fullInvoiceNo,
        Invoice.InvoiceType invoiceType,
        String sellerId,
        String sellerName,
        String buyerId,
        String buyerName,
        String buyerEmail,
        Invoice.CarrierType carrierType,
        BigDecimal salesAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        Invoice.InvoiceStatus status,
        Invoice.UploadStatus uploadStatus,
        LocalDateTime issueAt,
        LocalDateTime voidAt,
        String voidReason
) {
    public static InvoiceResponse from(Invoice inv) {
        return new InvoiceResponse(
                inv.getId(), inv.getStoreId(), inv.getOrderId(),
                inv.getFullInvoiceNo(), inv.getInvoiceType(),
                inv.getSellerId(), inv.getSellerName(),
                inv.getBuyerId(), inv.getBuyerName(), inv.getBuyerEmail(),
                inv.getCarrierType(),
                inv.getSalesAmount(), inv.getTaxAmount(), inv.getTotalAmount(),
                inv.getStatus(), inv.getUploadStatus(),
                inv.getIssueAt(), inv.getVoidAt(), inv.getVoidReason()
        );
    }
}
