/**
 * @file InvoiceItem.java
 * @description 發票明細實體 / Invoice line item entity
 * @description_en Each item in an issued invoice with tax breakdown
 * @description_zh 發票中的每一品項，含稅率拆分
 */
package com.enterprise.tax.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_invoice_items")
@Getter
@Setter
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Column(name = "product_id")
    private UUID productId;

    @Column(nullable = false, length = 256)
    private String description;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, length = 20)
    private String unit = "個";

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "tax_type", nullable = false, length = 20)
    private String taxType = "INCLUSIVE";

    @Column(name = "tax_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal taxRate = new BigDecimal("0.0500");

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
