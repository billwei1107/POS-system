/**
 * @file HeldOrderRepository.java
 * @description 掛單 Repository / Held order repository
 * @description_en JPA repository for store-scoped held orders
 * @description_zh 以門店與終端機為作用域查詢暫存掛單
 */
package com.enterprise.core.repository;

import com.enterprise.core.entity.HeldOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HeldOrderRepository extends JpaRepository<HeldOrder, UUID> {

    List<HeldOrder> findByStoreIdOrderByHeldAtDesc(UUID storeId);

    List<HeldOrder> findByStoreIdAndTerminalIdOrderByHeldAtDesc(UUID storeId, UUID terminalId);
}
