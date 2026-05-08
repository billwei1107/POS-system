/**
 * @file StockAlertRepository.java
 * @description 庫存警示 Repository / Stock alert JPA repository
 * @description_en JPA repository for managing and querying inventory alerts
 * @description_zh 庫存警示管理與查詢 JPA 儲存層
 */
package com.enterprise.inventory.repository;

import com.enterprise.inventory.entity.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockAlertRepository extends JpaRepository<StockAlert, UUID> {

    List<StockAlert> findAllByStoreIdAndAcknowledgedFalseOrderByCreatedAtDesc(UUID storeId);

    Optional<StockAlert> findByStoreIdAndItemIdAndAlertTypeAndAcknowledgedFalse(
            UUID storeId, UUID itemId, StockAlert.AlertType alertType);

    List<StockAlert> findAllByStoreId(UUID storeId);
}
