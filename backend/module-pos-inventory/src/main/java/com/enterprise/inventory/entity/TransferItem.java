/**
 * @file TransferItem.java
 * @description 調撥品項明細實體 / Transfer request line item entity
 * @description_en Line item for transfer request tracking requested, shipped, and received qty
 * @description_zh 調撥申請的品項明細，追蹤申請/出貨/收貨數量
 */
package com.enterprise.inventory.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pos_inv_transfer_items")
@Getter @Setter @NoArgsConstructor
public class TransferItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private TransferRequest transferRequest;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "requested_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal requestedQty = BigDecimal.ZERO;

    @Column(name = "shipped_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal shippedQty = BigDecimal.ZERO;

    @Column(name = "received_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal receivedQty = BigDecimal.ZERO;
}
