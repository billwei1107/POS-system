/**
 * @file StoreStockRepository.java
 * @description 門店庫存 Repository / Store stock JPA repository
 * @description_en JPA repository for store-level inventory with pessimistic locking for concurrent deduction
 * @description_zh 門店庫存 JPA 儲存層，使用悲觀鎖支援高併發扣減
 */
package com.enterprise.inventory.repository;

import com.enterprise.inventory.entity.StoreStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreStockRepository extends JpaRepository<StoreStock, UUID> {

    // ========================================
    // 悲觀鎖查詢（扣庫存用）/ Pessimistic lock for stock deduction
    // ========================================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StoreStock s WHERE s.storeId = :storeId AND s.itemId = :itemId AND s.deletedAt IS NULL")
    Optional<StoreStock> findByStoreIdAndItemIdForUpdate(@Param("storeId") UUID storeId,
                                                          @Param("itemId") UUID itemId);

    Optional<StoreStock> findByStoreIdAndItemId(UUID storeId, UUID itemId);

    List<StoreStock> findAllByStoreId(UUID storeId);

    // ========================================
    // 低庫存查詢 / Low stock query for alert generation
    // ========================================
    @Query("SELECT s FROM StoreStock s WHERE s.storeId = :storeId AND s.deletedAt IS NULL " +
           "AND s.quantity <= s.reorderPoint AND s.reorderPoint > 0")
    List<StoreStock> findLowStockByStoreId(@Param("storeId") UUID storeId);

    // ========================================
    // Redis 同步輔助：批次更新數量 / Bulk quantity update for Redis sync
    // ========================================
    @Modifying
    @Query("UPDATE StoreStock s SET s.quantity = :qty, s.updatedAt = NOW() WHERE s.storeId = :storeId AND s.itemId = :itemId")
    int updateQuantity(@Param("storeId") UUID storeId, @Param("itemId") UUID itemId,
                       @Param("qty") BigDecimal qty);
}
