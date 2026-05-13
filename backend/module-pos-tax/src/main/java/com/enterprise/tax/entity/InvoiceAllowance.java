/**
 * @file InvoiceAllowance.java
 * @description 發票折讓單實體 / Invoice allowance (credit note) entity
 * @description_en Created when a partial refund occurs; full refund voids the original invoice
 * @description_zh 部分退款時建立折讓單；全額退款則直接作廢原發票
 */
package com.enterprise.tax.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_invoice_allowances")
@Getter
@Setter
public class InvoiceAllowance {

    public enum AllowanceStatus { ISSUED, FAILED }

    public enum UploadStatus { PENDING, SUCCESS, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "refund_id", nullable = false)
    private UUID refundId;

    @Column(name = "allowance_no", length = 20)
    private String allowanceNo;

    @Column(name = "sales_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal salesAmount;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AllowanceStatus status = AllowanceStatus.ISSUED;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false, length = 20)
    private UploadStatus uploadStatus = UploadStatus.PENDING;

    @Column(name = "upload_at")
    private LocalDateTime uploadAt;

    @Column(name = "upload_resp", columnDefinition = "TEXT")
    private String uploadResp;

    @Column(name = "issue_at", nullable = false)
    private LocalDateTime issueAt = LocalDateTime.now();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
