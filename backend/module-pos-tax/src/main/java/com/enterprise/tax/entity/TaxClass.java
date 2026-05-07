/**
 * @file TaxClass.java
 * @description 稅率類別實體 / Tax class entity
 * @description_en Defines tax rates applied to products (inclusive/exclusive/exempt/zero-rated)
 * @description_zh 定義商品適用的稅率類別，支援含稅、外加稅、免稅、零稅率
 */
package com.enterprise.tax.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_tax_classes")
@SQLDelete(sql = "UPDATE pos_tax_classes SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class TaxClass {

    public enum TaxType { INCLUSIVE, EXCLUSIVE, EXEMPT, ZERO_RATED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", nullable = false, length = 20)
    private TaxType taxType;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal rate;

    @Column(length = 200)
    private String description;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
