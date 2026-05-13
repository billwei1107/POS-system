/**
 * @file ShiftHandover.java
 * @description 交接班記錄實體 / Shift handover record entity
 * @description_en Records the cash count and transfer at shift change between cashiers
 * @description_zh 交接班記錄，包含現金清點、差異確認
 */
package com.enterprise.staff.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pos_staff_shift_handovers")
@Getter
@Setter
public class ShiftHandover extends BaseEntity {

    @Column(name = "from_shift_id", nullable = false)
    private UUID fromShiftId;

    @Column(name = "to_shift_id")
    private UUID toShiftId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "handover_at", nullable = false)
    private Instant handoverAt;

    @Column(name = "cash_counted", nullable = false, precision = 12, scale = 2)
    private BigDecimal cashCounted = BigDecimal.ZERO;

    @Column(name = "cash_expected", nullable = false, precision = 12, scale = 2)
    private BigDecimal cashExpected = BigDecimal.ZERO;

    @Column(name = "cash_variance", nullable = false, precision = 12, scale = 2)
    private BigDecimal cashVariance = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "confirmed_by")
    private UUID confirmedBy;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;
}
