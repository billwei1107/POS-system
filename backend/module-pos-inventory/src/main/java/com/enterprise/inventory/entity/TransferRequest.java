/**
 * @file TransferRequest.java
 * @description 門店調撥申請實體 / Inter-store transfer request entity
 * @description_en Manages stock transfer between stores with approval workflow
 * @description_zh 管理門店間調撥申請與審批流程
 */
package com.enterprise.inventory.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pos_inv_transfer_requests")
@Getter @Setter @NoArgsConstructor
public class TransferRequest extends BaseEntity {

    public enum TransferStatus {
        REQUESTED, APPROVED, IN_TRANSIT, RECEIVED, CANCELLED
    }

    @Column(name = "transfer_no", nullable = false, unique = true, length = 30)
    private String transferNo;

    @Column(name = "from_store_id", nullable = false)
    private UUID fromStoreId;

    @Column(name = "to_store_id", nullable = false)
    private UUID toStoreId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferStatus status = TransferStatus.REQUESTED;

    @Column(name = "notes")
    private String notes;

    @Column(name = "requested_by")
    private UUID requestedBy;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @OneToMany(mappedBy = "transferRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransferItem> items = new ArrayList<>();
}
