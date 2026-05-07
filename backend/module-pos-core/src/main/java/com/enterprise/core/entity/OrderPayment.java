/**
 * @file OrderPayment.java
 * @description 訂單付款記錄實體 / Order payment record entity
 * @description_en Supports split payments across multiple payment methods per order
 * @description_zh 支援單一訂單的多種付款方式分筆付款
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
@Table(name = "pos_order_payments")
@SQLDelete(sql = "UPDATE pos_order_payments SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class OrderPayment extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "pay_method", nullable = false, length = 30)
    private String payMethod;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 12, scale = 2)
    private BigDecimal tendered;

    @Column(name = "change_given", nullable = false, precision = 12, scale = 2)
    private BigDecimal changeGiven = BigDecimal.ZERO;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(name = "gateway_resp", columnDefinition = "TEXT")
    private String gatewayResp;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.SUCCESS;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt = LocalDateTime.now();

    public enum PaymentStatus {
        SUCCESS, FAILED, REFUNDED, PARTIAL_REFUND
    }
}
