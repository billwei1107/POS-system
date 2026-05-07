/**
 * @file PriceRule.java
 * @description 價格規則實體 / Price rule entity
 * @description_en Supports member pricing, happy-hour, and bulk discount per item and store
 * @description_zh 商品價格規則，支援會員價、歡樂時光、批量折扣，可指定門店
 */
package com.enterprise.product.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_prod_price_rules")
@SQLDelete(sql = "UPDATE pos_prod_price_rules SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
public class PriceRule extends BaseEntity {

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "price_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PriceType priceType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "min_qty", nullable = false)
    private Integer minQty = 1;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(nullable = false)
    private Boolean active = true;

    public enum PriceType {
        BASE, MEMBER, HAPPY_HOUR, BULK, STAFF
    }
}
