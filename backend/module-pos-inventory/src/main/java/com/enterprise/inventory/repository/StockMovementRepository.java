/**
 * @file StockMovementRepository.java
 * @description 庫存異動紀錄 Repository / Stock movement JPA repository
 * @description_en JPA repository for querying stock movement history
 * @description_zh 庫存異動紀錄查詢 JPA 儲存層
 */
package com.enterprise.inventory.repository;

import com.enterprise.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    List<StockMovement> findAllByStoreIdAndItemIdOrderByCreatedAtDesc(UUID storeId, UUID itemId);

    List<StockMovement> findAllByReferenceId(UUID referenceId);

    Optional<StockMovement> findFirstByReferenceIdAndMovementType(UUID referenceId,
                                                                    StockMovement.MovementType type);
}
