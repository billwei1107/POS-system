/**
 * @file InvoiceItemRepository.java
 * @description 發票明細資料存取層 / Invoice item repository
 * @description_en Queries invoice line items by invoice ID
 * @description_zh 依發票 ID 查詢發票明細
 */
package com.enterprise.tax.repository;

import com.enterprise.tax.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {

    List<InvoiceItem> findByInvoiceIdOrderBySequenceNo(UUID invoiceId);
}
