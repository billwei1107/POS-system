/**
 * @file TaxRule.java
 * @description 稅務規則實體 / Tax rule entity
 * @description_en Maps products or categories to specific tax classes with priority ordering
 * @description_zh 將商品或分類對應至指定稅率類別，依優先順序套用
 */
package com.enterprise.tax.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_tax_rules")
@Getter
@Setter
public class TaxRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_class_id", nullable = false)
    private TaxClass taxClass;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(nullable = false)
    private int priority = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
