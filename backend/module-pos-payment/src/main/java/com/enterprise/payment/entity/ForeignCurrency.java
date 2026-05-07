/**
 * @file ForeignCurrency.java
 * @description 外幣匯率設定 Entity / Foreign currency exchange rate entity
 * @description_en Store-level foreign currency buy/sell rates for multi-currency transactions
 * @description_zh 門店層級的外幣買賣匯率設定，供多幣別收款使用
 */
package com.enterprise.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_foreign_currencies")
@SQLDelete(sql = "UPDATE pos_foreign_currencies SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class ForeignCurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false, length = 3)
    private String currencyCode;

    @Column(nullable = false, length = 50)
    private String currencyName;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal buyRate;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal sellRate;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private LocalDate effectiveDate = LocalDate.now();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime deletedAt;

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
