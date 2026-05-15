/**
 * @file RefundResponse.java
 * @description 退款回應 DTO / Refund response DTO
 * @description_en Response payload for POS refund records
 * @description_zh POS 退款紀錄回應資料
 */
package com.enterprise.core.dto.response;

import com.enterprise.core.entity.OrderRefund;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RefundResponse(
    UUID id,
    UUID orderId,
    String refundNo,
    BigDecimal refundAmount,
    String refundMethod,
    String reason,
    UUID approvedBy,
    OrderRefund.RefundStatus status,
    LocalDateTime processedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static RefundResponse from(OrderRefund refund) {
        return new RefundResponse(
            refund.getId(),
            refund.getOrderId(),
            refund.getRefundNo(),
            refund.getRefundAmount(),
            refund.getRefundMethod(),
            refund.getReason(),
            refund.getApprovedBy(),
            refund.getStatus(),
            refund.getProcessedAt(),
            refund.getCreatedAt(),
            refund.getUpdatedAt()
        );
    }
}
