/**
 * @file XReport.java
 * @description X Report 實體 / X Report entity (mid-shift snapshot)
 * @description_en Represents an X Report: a non-resetting mid-shift sales snapshot
 * @description_zh X Report：不重置寄存器的班中銷售快照
 */
package com.enterprise.staff.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "pos_staff_x_reports")
@Getter
@Setter
public class XReport extends BaseEntity {

    @Column(name = "shift_id", nullable = false)
    private UUID shiftId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;

    @Column(name = "total_sales", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalSales = BigDecimal.ZERO;

    @Column(name = "total_refunds", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalRefunds = BigDecimal.ZERO;

    @Column(name = "total_discounts", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalDiscounts = BigDecimal.ZERO;

    @Column(name = "total_tax", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalTax = BigDecimal.ZERO;

    @Column(name = "net_sales", nullable = false, precision = 14, scale = 2)
    private BigDecimal netSales = BigDecimal.ZERO;

    @Column(name = "cash_sales", nullable = false, precision = 14, scale = 2)
    private BigDecimal cashSales = BigDecimal.ZERO;

    @Column(name = "card_sales", nullable = false, precision = 14, scale = 2)
    private BigDecimal cardSales = BigDecimal.ZERO;

    @Column(name = "other_sales", nullable = false, precision = 14, scale = 2)
    private BigDecimal otherSales = BigDecimal.ZERO;

    @Column(name = "transaction_count", nullable = false)
    private int transactionCount = 0;

    @Column(name = "refund_count", nullable = false)
    private int refundCount = 0;

    @Column(name = "void_count", nullable = false)
    private int voidCount = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_data", columnDefinition = "jsonb")
    private Map<String, Object> reportData;
}
