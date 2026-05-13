/**
 * @file StoreStock.java
 * @description 門店庫存實體 / Store-level stock entity
 * @description_en Tracks available and reserved quantity per item per store
 * @description_zh 記錄每個門店每個商品的可用庫存與預留庫存
 */
package com.enterprise.inventory.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pos_inv_store_stock",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "item_id"}))
@Getter @Setter @NoArgsConstructor
public class StoreStock extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "reserved_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "reorder_point", nullable = false, precision = 12, scale = 3)
    private BigDecimal reorderPoint = BigDecimal.ZERO;

    @Column(name = "reorder_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal reorderQuantity = BigDecimal.ZERO;

    // ========================================
    // 可用庫存計算 / Available quantity calculation
    // ========================================
    public BigDecimal getAvailableQuantity() {
        return quantity.subtract(reservedQuantity);
    }

    public boolean isLowStock(BigDecimal threshold) {
        return quantity.compareTo(threshold) <= 0;
    }
}
