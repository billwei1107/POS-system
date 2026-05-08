/**
 * @file StockAlert.java
 * @description 庫存警示實體 / Stock alert entity
 * @description_en Tracks low-stock, out-of-stock, and expiring batch alerts
 * @description_zh 追蹤低庫存、缺貨與即將到期的批次警示
 */
package com.enterprise.inventory.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pos_inv_stock_alerts")
@Getter @Setter @NoArgsConstructor
public class StockAlert extends BaseEntity {

    public enum AlertType {
        LOW_STOCK, OUT_OF_STOCK, EXPIRING
    }

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 20)
    private AlertType alertType;

    @Column(name = "current_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal currentQty;

    @Column(name = "threshold_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal thresholdQty;

    @Column(name = "acknowledged", nullable = false)
    private boolean acknowledged = false;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;
}
