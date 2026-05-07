/**
 * @file PaymentTransaction.java
 * @description 支付交易記錄 Entity / Payment transaction entity
 * @description_en Immutable audit record for every payment attempt tied to an order
 * @description_zh 每筆訂單付款嘗試的不可變稽核記錄，含閘道回應
 */
package com.enterprise.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_payment_transactions")
@Getter
@Setter
public class PaymentTransaction {

    public enum TxnStatus { SUCCESS, FAILED, VOIDED, REFUNDED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private UUID payMethodId;

    @Column(nullable = false, length = 30)
    private String methodType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 12, scale = 2)
    private BigDecimal tendered;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal changeGiven = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TxnStatus status = TxnStatus.SUCCESS;

    @Column(length = 200)
    private String gatewayRef;

    @Column(columnDefinition = "TEXT")
    private String gatewayResp;

    @Column(length = 50)
    private String errorCode;

    @Column(columnDefinition = "TEXT")
    private String errorMsg;

    @Column(nullable = false)
    private LocalDateTime processedAt = LocalDateTime.now();

    private LocalDateTime voidedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
