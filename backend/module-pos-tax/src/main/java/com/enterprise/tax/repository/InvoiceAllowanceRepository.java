/**
 * @file InvoiceAllowanceRepository.java
 * @description 發票折讓單資料存取層 / Invoice allowance repository
 * @description_en Queries allowances by invoice and refund IDs
 * @description_zh 依發票、退款 ID 查詢折讓單
 */
package com.enterprise.tax.repository;

import com.enterprise.tax.entity.InvoiceAllowance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceAllowanceRepository extends JpaRepository<InvoiceAllowance, UUID> {

    List<InvoiceAllowance> findByInvoiceId(UUID invoiceId);

    Optional<InvoiceAllowance> findByRefundId(UUID refundId);
}
