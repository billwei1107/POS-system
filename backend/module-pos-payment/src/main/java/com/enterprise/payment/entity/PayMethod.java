/**
 * @file PayMethod.java
 * @description 支付方式 Entity / Payment method entity
 * @description_en Represents a store-level payment method configuration (cash, card, QR, etc.)
 * @description_zh 門店層級的支付方式設定，包含方式類型與閘道關聯
 */
package com.enterprise.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_pay_methods")
@SQLDelete(sql = "UPDATE pos_pay_methods SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class PayMethod {

    public enum MethodType { CASH, CARD, QR_CODE, GIFT_CARD, MIXED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MethodType methodType;

    private UUID gatewayId;

    @Column(nullable = false)
    private boolean isChangeBack = false;

    @Column(nullable = false)
    private int sortOrder = 0;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime deletedAt;

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
