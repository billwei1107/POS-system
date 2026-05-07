/**
 * @file OrderRepository.java
 * @description 訂單 Repository / Order repository
 * @description_en JPA repository for POS order queries with store-scoped filtering
 * @description_zh POS 訂單查詢，以門店為作用域進行過濾
 */
package com.enterprise.core.repository;

import com.enterprise.core.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNo(String orderNo);

    boolean existsByOrderNo(String orderNo);

    @Query("SELECT o FROM Order o WHERE o.storeId = :storeId " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:from IS NULL OR o.createdAt >= :from) " +
           "AND (:to IS NULL OR o.createdAt <= :to) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findByStoreIdAndFilters(
        @Param("storeId") UUID storeId,
        @Param("status") Order.OrderStatus status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        Pageable pageable
    );
}
