/**
 * @file PaymentTransactionRepository.java
 * @description 支付交易 Repository / Payment transaction JPA repository
 * @description_en Data access for pos_payment_transactions; no soft delete (immutable audit records)
 * @description_zh 存取 pos_payment_transactions，付款記錄為不可變稽核資料，無軟刪除
 */
package com.enterprise.payment.repository;

import com.enterprise.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    List<PaymentTransaction> findByOrderId(UUID orderId);

    @Query("""
        SELECT t FROM PaymentTransaction t
        WHERE t.storeId = :storeId
          AND t.processedAt BETWEEN :from AND :to
        ORDER BY t.processedAt DESC
        """)
    List<PaymentTransaction> findByStoreAndDateRange(
        @Param("storeId") UUID storeId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
}
