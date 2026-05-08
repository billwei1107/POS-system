/**
 * @file StaffShift.java
 * @description 班次實體 / Staff shift entity
 * @description_en Represents a cashier shift session with sales aggregates and cash management
 * @description_zh 收銀班次，包含銷售統計與現金管理
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
@Table(name = "pos_staff_shifts")
@Getter
@Setter
public class StaffShift extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "terminal_id")
    private UUID terminalId;

    @Column(name = "shift_no", nullable = false, unique = true, length = 30)
    private String shiftNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShiftStatus status = ShiftStatus.OPEN;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "opening_cash", nullable = false, precision = 12, scale = 2)
    private BigDecimal openingCash = BigDecimal.ZERO;

    @Column(name = "closing_cash", precision = 12, scale = 2)
    private BigDecimal closingCash;

    @Column(name = "expected_cash", precision = 12, scale = 2)
    private BigDecimal expectedCash;

    @Column(name = "cash_variance", precision = 12, scale = 2)
    private BigDecimal cashVariance;

    @Column(name = "total_sales", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalSales = BigDecimal.ZERO;

    @Column(name = "total_refunds", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalRefunds = BigDecimal.ZERO;

    @Column(name = "total_discounts", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalDiscounts = BigDecimal.ZERO;

    @Column(name = "total_tax", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalTax = BigDecimal.ZERO;

    @Column(name = "transaction_count", nullable = false)
    private int transactionCount = 0;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ========================================
    // 班次狀態列舉 / Shift status enum
    // ========================================
    public enum ShiftStatus {
        OPEN, CLOSED, BLIND_CLOSED
    }

    // ========================================
    // 累計銷售金額 / Accumulate sales amount
    // ========================================
    public void addSale(BigDecimal amount, BigDecimal tax, BigDecimal discount) {
        this.totalSales = this.totalSales.add(amount);
        this.totalTax = this.totalTax.add(tax);
        this.totalDiscounts = this.totalDiscounts.add(discount);
        this.transactionCount++;
    }

    // ========================================
    // 累計退款金額 / Accumulate refund amount
    // ========================================
    public void addRefund(BigDecimal amount) {
        this.totalRefunds = this.totalRefunds.add(amount);
    }

    // ========================================
    // 計算淨銷售額 / Calculate net sales
    // ========================================
    public BigDecimal getNetSales() {
        return totalSales.subtract(totalRefunds);
    }
}
