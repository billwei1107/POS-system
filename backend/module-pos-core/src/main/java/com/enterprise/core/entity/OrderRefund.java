/**
 * @file OrderRefund.java
 * @description 退款記錄實體 / Order refund entity
 * @description_en Tracks refund requests and their approval/processing status
 * @description_zh 記錄退款申請、審批狀態與實際退款結果
 */
package com.enterprise.core.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_order_refunds")
@SQLDelete(sql = "UPDATE pos_order_refunds SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class OrderRefund extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "refund_no", nullable = false, unique = true, length = 40)
    private String refundNo;

    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_method", nullable = false, length = 30)
    private String refundMethod;

    @Column(length = 200)
    private String reason;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public enum RefundStatus {
        PENDING, APPROVED, REJECTED, COMPLETED
    }
}
