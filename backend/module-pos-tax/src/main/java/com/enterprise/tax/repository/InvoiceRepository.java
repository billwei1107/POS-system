/**
 * @file InvoiceRepository.java
 * @description 電子發票資料存取層 / Invoice repository
 * @description_en Queries invoices by order, store, and upload status
 * @description_zh 依訂單、門店、上傳狀態查詢電子發票
 */
package com.enterprise.tax.repository;

import com.enterprise.tax.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByOrderId(UUID orderId);

    List<Invoice> findByStoreIdAndUploadStatus(UUID storeId, Invoice.UploadStatus uploadStatus);

    @Query("SELECT i FROM Invoice i WHERE i.storeId = :storeId AND i.issueAt BETWEEN :from AND :to ORDER BY i.issueAt DESC")
    List<Invoice> findByStoreAndDateRange(@Param("storeId") UUID storeId,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);
}
