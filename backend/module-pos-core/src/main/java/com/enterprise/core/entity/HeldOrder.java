/**
 * @file HeldOrder.java
 * @description 掛單實體 / Held (parked) order entity
 * @description_en Stores temporarily parked orders as JSON payload for later retrieval
 * @description_zh 暫存掛單，以 JSON 序列化方式儲存購物車狀態供後續回復
 */
package com.enterprise.core.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_held_orders")
@SQLDelete(sql = "UPDATE pos_held_orders SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class HeldOrder extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "terminal_id")
    private UUID terminalId;

    @Column(length = 50)
    private String label;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "held_at", nullable = false)
    private LocalDateTime heldAt = LocalDateTime.now();
}
