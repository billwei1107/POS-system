/**
 * @file ReconciliationRepository.java
 * @description 對帳 Repository / Reconciliation JPA repository
 * @description_en Data access for pos_reconciliation; unique per store+date+method
 * @description_zh 存取 pos_reconciliation，每門店每日每支付方式唯一一筆
 */
package com.enterprise.payment.repository;

import com.enterprise.payment.entity.Reconciliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReconciliationRepository extends JpaRepository<Reconciliation, UUID> {

    List<Reconciliation> findByStoreIdAndReconDateOrderByMethodType(UUID storeId, LocalDate reconDate);

    Optional<Reconciliation> findByStoreIdAndReconDateAndPayMethodId(UUID storeId, LocalDate reconDate, UUID payMethodId);

    List<Reconciliation> findByStoreIdAndReconDateBetweenOrderByReconDateDesc(UUID storeId, LocalDate from, LocalDate to);
}
