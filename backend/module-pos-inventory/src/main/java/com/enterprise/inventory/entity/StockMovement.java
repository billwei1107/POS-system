/**
 * @file StockMovement.java
 * @description 庫存異動紀錄實體 / Stock movement audit log entity
 * @description_en Immutable audit record for every stock quantity change
 * @description_zh 每次庫存數量變動的不可變稽核紀錄
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
@Table(name = "pos_inv_stock_movements")
@Getter @Setter @NoArgsConstructor
public class StockMovement extends BaseEntity {

    public enum MovementType {
        SALE, RETURN, TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT, RECEIVING, WASTE
    }

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "quantity_change", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityChange;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "operated_by")
    private UUID operatedBy;

    @Column(name = "notes")
    private String notes;
}
