/**
 * @file StockTake.java
 * @description 盤點主表實體 / Inventory stock take session entity
 * @description_en Represents a single inventory counting session for a store
 * @description_zh 代表一次門店盤點工作階段
 */
package com.enterprise.inventory.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pos_inv_stock_takes")
@Getter @Setter @NoArgsConstructor
public class StockTake extends BaseEntity {

    public enum StockTakeStatus {
        IN_PROGRESS, COMPLETED, CANCELLED
    }

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StockTakeStatus status = StockTakeStatus.IN_PROGRESS;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "notes")
    private String notes;

    @OneToMany(mappedBy = "stockTake", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockTakeItem> items = new ArrayList<>();
}
