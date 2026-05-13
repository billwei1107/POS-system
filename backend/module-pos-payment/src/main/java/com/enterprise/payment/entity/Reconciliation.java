/**
 * @file Reconciliation.java
 * @description 每日對帳記錄 Entity / Daily reconciliation entity
 * @description_en Aggregated daily settlement record per store per payment method
 * @description_zh 每門店每支付方式的每日匯總對帳紀錄，含閘道金額與差異
 */
package com.enterprise.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_reconciliation")
@Getter
@Setter
public class Reconciliation {

    public enum ReconStatus { PENDING, MATCHED, DISCREPANCY }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private LocalDate reconDate;

    @Column(nullable = false)
    private UUID payMethodId;

    @Column(nullable = false, length = 30)
    private String methodType;

    @Column(nullable = false)
    private int transactionCount = 0;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private int refundCount = 0;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal gatewayAmount;

    @Column(precision = 14, scale = 2)
    private BigDecimal variance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReconStatus status = ReconStatus.PENDING;

    private UUID reconciledBy;

    private LocalDateTime reconciledAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
