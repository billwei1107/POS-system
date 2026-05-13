/**
 * @file OrderItemModifier.java
 * @description 訂單明細客製化選項快照 / Order item modifier snapshot entity
 * @description_en Captures modifier selections at order time to preserve historical accuracy
 * @description_zh 儲存下單時的客製化選項，確保歷史訂單不受商品改動影響
 */
package com.enterprise.core.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pos_order_item_modifiers")
@SQLDelete(sql = "UPDATE pos_order_item_modifiers SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class OrderItemModifier extends BaseEntity {

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(name = "modifier_group_id", nullable = false)
    private UUID modifierGroupId;

    @Column(name = "modifier_id", nullable = false)
    private UUID modifierId;

    @Column(name = "modifier_name_snapshot", nullable = false, length = 100)
    private String modifierNameSnapshot;

    @Column(name = "price_adjustment", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAdjustment = BigDecimal.ZERO;
}
