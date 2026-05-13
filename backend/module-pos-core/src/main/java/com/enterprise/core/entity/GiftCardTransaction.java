/**
 * @file GiftCardTransaction.java
 * @description 禮品卡交易記錄實體 / Gift card transaction entity
 * @description_en Records every balance change on a gift card (top-up, consume, refund)
 * @description_zh 記錄禮品卡每次餘額變動（儲值、消費、退款）
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
@Table(name = "pos_gift_card_transactions")
@SQLDelete(sql = "UPDATE pos_gift_card_transactions SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class GiftCardTransaction extends BaseEntity {

    @Column(name = "card_id", nullable = false)
    private UUID cardId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "txn_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TxnType txnType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 200)
    private String note;

    public enum TxnType {
        TOPUP, CONSUME, REFUND, VOID
    }
}
