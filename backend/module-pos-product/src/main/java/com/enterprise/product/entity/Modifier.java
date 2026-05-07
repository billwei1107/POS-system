/**
 * @file Modifier.java
 * @description 客製化選項實體 / Modifier option entity
 * @description_en Individual modifier option with optional price adjustment (e.g. "Extra Shot +$1.5")
 * @description_zh 客製化選項，可附加價格調整（如加濃 +$1.5）
 */
package com.enterprise.product.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pos_prod_modifiers")
@SQLDelete(sql = "UPDATE pos_prod_modifiers SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
public class Modifier extends BaseEntity {

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "price_adjustment", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
