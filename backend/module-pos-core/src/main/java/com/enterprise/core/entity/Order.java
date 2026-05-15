/**
 * @file Order.java
 * @description POS 訂單主實體 / POS order entity
 * @description_en Represents a POS transaction with lifecycle state management
 * @description_zh POS 交易訂單，支援完整訂單生命週期狀態機
 */
package com.enterprise.core.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_orders")
@SQLDelete(sql = "UPDATE pos_orders SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Order extends BaseEntity {

    @Column(name = "order_no", nullable = false, unique = true, length = 40)
    private String orderNo;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "terminal_id")
    private UUID terminalId;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(name = "order_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderType orderType = OrderType.DINE_IN;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(name = "discount_source", length = 20)
    @Enumerated(EnumType.STRING)
    private DiscountSource discountSource;

    @Column(name = "promotion_rule_id")
    private UUID promotionRuleId;

    @Column(name = "promotion_code", length = 50)
    private String promotionCode;

    @Column(name = "discount_label", length = 120)
    private String discountLabel;

    @Column(name = "tax_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxTotal = BigDecimal.ZERO;

    @Column(name = "rounding_adj", nullable = false, precision = 12, scale = 2)
    private BigDecimal roundingAdj = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "paid_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidTotal = BigDecimal.ZERO;

    @Column(name = "change_given", nullable = false, precision = 12, scale = 2)
    private BigDecimal changeGiven = BigDecimal.ZERO;

    @Column(name = "member_id")
    private UUID memberId;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "table_no", length = 20)
    private String tableNo;

    @Column(name = "guest_count")
    private Integer guestCount;

    @Column(name = "voided_at")
    private LocalDateTime voidedAt;

    @Column(name = "voided_by")
    private UUID voidedBy;

    @Column(name = "void_reason", length = 200)
    private String voidReason;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ========================================
    // 訂單狀態列舉 / Order status enum
    // ========================================
    public enum OrderStatus {
        DRAFT, CONFIRMED, PREPARING, READY, COMPLETED, CLOSED, VOIDED
    }

    // ========================================
    // 訂單類型列舉 / Order type enum
    // ========================================
    public enum OrderType {
        DINE_IN, TAKEOUT, DELIVERY, ONLINE
    }

    // ========================================
    // 折扣來源列舉 / Discount source enum
    // ========================================
    public enum DiscountSource {
        MANUAL, MEMBER, PROMOTION
    }
}
