/**
 * @file ZReport.java
 * @description Z Report 實體 / Z Report entity (end-of-day close with tamper-proof hash)
 * @description_en Represents a Z Report: daily close that resets the register, protected by SHA-256 hash
 * @description_zh Z Report：每日結帳，重置寄存器並產生防篡改 SHA-256 Hash
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
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "pos_staff_z_reports")
@Getter
@Setter
public class ZReport extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "report_no", nullable = false, unique = true, length = 30)
    private String reportNo;

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

    @Column(name = "gross_sales", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossSales = BigDecimal.ZERO;

    @Column(name = "cash_sales", nullable = false, precision = 14, scale = 2)
    private BigDecimal cashSales = BigDecimal.ZERO;

    @Column(name = "card_sales", nullable = false, precision = 14, scale = 2)
    private BigDecimal cardSales = BigDecimal.ZERO;

    @Column(name = "other_sales", nullable = false, precision = 14, scale = 2)
    private BigDecimal otherSales = BigDecimal.ZERO;

    @Column(name = "cash_in_drawer", nullable = false, precision = 12, scale = 2)
    private BigDecimal cashInDrawer = BigDecimal.ZERO;

    @Column(name = "expected_cash", nullable = false, precision = 12, scale = 2)
    private BigDecimal expectedCash = BigDecimal.ZERO;

    @Column(name = "cash_variance", nullable = false, precision = 12, scale = 2)
    private BigDecimal cashVariance = BigDecimal.ZERO;

    @Column(name = "transaction_count", nullable = false)
    private int transactionCount = 0;

    @Column(name = "refund_count", nullable = false)
    private int refundCount = 0;

    @Column(name = "void_count", nullable = false)
    private int voidCount = 0;

    @Column(name = "shift_count", nullable = false)
    private int shiftCount = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_data", columnDefinition = "jsonb")
    private Map<String, Object> reportData;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "generated_by", nullable = false)
    private UUID generatedBy;

    @Column(name = "reset_at")
    private Instant resetAt;
}
