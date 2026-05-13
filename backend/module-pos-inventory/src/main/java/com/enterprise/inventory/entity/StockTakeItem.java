/**
 * @file StockTakeItem.java
 * @description 盤點明細實體 / Stock take line item entity
 * @description_en Records system quantity vs counted quantity for each item in a stock take
 * @description_zh 記錄盤點中每個商品的系統庫存與實際盤點數量的差異
 */
package com.enterprise.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pos_inv_stock_take_items")
@Getter @Setter @NoArgsConstructor
public class StockTakeItem extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_take_id", nullable = false)
    private StockTake stockTake;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "system_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal systemQty = BigDecimal.ZERO;

    @Column(name = "counted_qty", precision = 12, scale = 3)
    private BigDecimal countedQty;

    @Column(name = "difference", precision = 12, scale = 3)
    private BigDecimal difference;

    @Column(name = "notes")
    private String notes;

    // ========================================
    // 差異計算 / Calculate difference on count submission
    // ========================================
    public void submitCount(BigDecimal counted) {
        this.countedQty = counted;
        this.difference = counted.subtract(systemQty);
    }
}
