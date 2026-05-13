/**
 * @file Invoice.java
 * @description 電子發票主表實體 / E-invoice master entity
 * @description_en Taiwan MIG 3.2 compliant electronic invoice; supports B2C/B2B, carriers, donation
 * @description_zh 符合台灣財政部電子發票 MIG 3.2 規範，支援B2C/B2B、載具、愛心碼
 */
package com.enterprise.tax.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pos_invoices")
@Getter
@Setter
public class Invoice {

    public enum InvoiceType { B2C, B2B }

    public enum CarrierType { MEMBER, MOBILE, CITIZEN_DIGITAL }

    public enum InvoiceStatus { ISSUED, VOIDED, ALLOWANCE }

    public enum UploadStatus { PENDING, SUCCESS, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id")
    private InvoiceTrack track;

    @Column(name = "invoice_no", length = 8)
    private String invoiceNo;

    @Column(name = "full_invoice_no", length = 12)
    private String fullInvoiceNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false, length = 20)
    private InvoiceType invoiceType = InvoiceType.B2C;

    // ========================================
    // 銷售方資訊 / Seller information
    // ========================================
    @Column(name = "seller_id", nullable = false, length = 8)
    private String sellerId;

    @Column(name = "seller_name", nullable = false, length = 100)
    private String sellerName;

    // ========================================
    // 買方資訊 / Buyer information (B2C optional)
    // ========================================
    @Column(name = "buyer_id", length = 8)
    private String buyerId;

    @Column(name = "buyer_name", length = 100)
    private String buyerName;

    @Column(name = "buyer_email", length = 200)
    private String buyerEmail;

    // ========================================
    // 載具資訊 / Carrier information
    // ========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "carrier_type", length = 20)
    private CarrierType carrierType;

    @Column(name = "carrier_id1", length = 64)
    private String carrierId1;

    @Column(name = "carrier_id2", length = 64)
    private String carrierId2;

    @Column(name = "donate_code", length = 7)
    private String donateCode;

    // ========================================
    // 金額 / Amounts
    // ========================================
    @Column(name = "sales_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal salesAmount;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    // ========================================
    // 狀態 / Status
    // ========================================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false, length = 20)
    private UploadStatus uploadStatus = UploadStatus.PENDING;

    @Column(name = "upload_at")
    private LocalDateTime uploadAt;

    @Column(name = "upload_resp", columnDefinition = "TEXT")
    private String uploadResp;

    @Column(name = "issue_at", nullable = false)
    private LocalDateTime issueAt = LocalDateTime.now();

    @Column(name = "void_at")
    private LocalDateTime voidAt;

    @Column(name = "void_reason", length = 200)
    private String voidReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNo ASC")
    private List<InvoiceItem> items = new ArrayList<>();

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
