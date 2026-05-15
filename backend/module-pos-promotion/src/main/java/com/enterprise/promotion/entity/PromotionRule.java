/**
 * @file PromotionRule.java
 * @description POS 促銷規則實體 / POS promotion rule entity
 * @description_en Stores order-level promotion rules with auto or coupon-code triggers
 * @description_zh 儲存訂單層級促銷規則，支援自動套用與優惠碼觸發
 */
package com.enterprise.promotion.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_promo_rules")
@SQLDelete(sql = "UPDATE pos_promo_rules SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class PromotionRule extends BaseEntity {

    @Column(name = "store_id")
    private UUID storeId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(name = "trigger_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TriggerType triggerType = TriggerType.AUTO;

    @Column(name = "discount_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType = DiscountType.PERCENT;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue = BigDecimal.ZERO;

    @Column(name = "minimum_subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumSubtotal = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(nullable = false)
    private Boolean active = true;

    public enum TriggerType {
        AUTO, CODE
    }

    public enum DiscountType {
        PERCENT, AMOUNT
    }
}
