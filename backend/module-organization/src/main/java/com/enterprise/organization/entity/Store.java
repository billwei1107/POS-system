package com.enterprise.organization.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

/**
 * 門店實體 / Store location entity
 */
@Entity
@Table(name = "org_stores")
@Data
@EqualsAndHashCode(callSuper = true)
public class Store extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "region_id")
    private UUID regionId;

    @Column(name = "store_code", nullable = false, unique = true, length = 50)
    private String storeCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 50)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String timezone = "Asia/Taipei";

    @Column(nullable = false, length = 10)
    private String currency = "TWD";

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "tax_rate", precision = 5, scale = 4)
    private BigDecimal taxRate = new BigDecimal("0.05");

    @Column(name = "receipt_header", columnDefinition = "TEXT")
    private String receiptHeader;

    @Column(name = "receipt_footer", columnDefinition = "TEXT")
    private String receiptFooter;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";
}
