/**
 * @file StockTakeRepository.java
 * @description 盤點主表 Repository / Stock take JPA repository
 * @description_en JPA repository for inventory stock take sessions
 * @description_zh 盤點工作階段 JPA 儲存層
 */
package com.enterprise.inventory.repository;

import com.enterprise.inventory.entity.StockTake;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockTakeRepository extends JpaRepository<StockTake, UUID> {

    List<StockTake> findAllByStoreIdOrderByCreatedAtDesc(UUID storeId);

    Optional<StockTake> findByStoreIdAndStatus(UUID storeId, StockTake.StockTakeStatus status);
}
