/**
 * @file PointLedger.java
 * @description 會員點數異動帳 / Member point ledger entity
 * @description_en Immutable-style ledger row for loyalty point changes
 * @description_zh 記錄會員點數增加與扣減的帳務明細
 */
package com.enterprise.crm.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_crm_point_ledgers")
@SQLDelete(sql = "UPDATE pos_crm_point_ledgers SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class PointLedger extends BaseEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_type", length = 40)
    private String referenceType;

    @Column(name = "points_delta", nullable = false)
    private Integer pointsDelta;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Reason reason;

    @Column(length = 255)
    private String note;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt = LocalDateTime.now();

    public enum Reason {
        ORDER_EARN, MANUAL_ADJUST, REDEEM, REFUND_REVERSE, EXPIRE
    }
}
