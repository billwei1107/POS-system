/**
 * @file InvoiceTrack.java
 * @description 發票字軌實體 / Invoice character track entity
 * @description_en Manages the MOFA-allocated invoice number ranges (字軌) per bi-monthly period
 * @description_zh 管理財政部每兩月配發的電子發票字軌號碼範圍
 */
package com.enterprise.tax.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_invoice_tracks")
@Getter
@Setter
public class InvoiceTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "seller_id", nullable = false, length = 8)
    private String sellerId;

    @Column(name = "track_prefix", nullable = false, length = 2)
    private String trackPrefix;

    @Column(name = "year_month", nullable = false, length = 6)
    private String yearMonth;

    @Column(nullable = false, length = 10)
    private String period;

    @Column(name = "start_no", nullable = false, length = 8)
    private String startNo;

    @Column(name = "end_no", nullable = false, length = 8)
    private String endNo;

    // 已分配至的最後一個號碼，00000000 = 尚未使用
    @Column(name = "current_no", nullable = false, length = 8)
    private String currentNo = "00000000";

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public boolean isExhausted() {
        return currentNo.compareTo(endNo) >= 0;
    }
}
