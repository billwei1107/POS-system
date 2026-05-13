/**
 * @file OrderRepository.java
 * @description 訂單 Repository / Order repository
 * @description_en JPA repository for POS order queries with store-scoped filtering
 * @description_zh POS 訂單查詢，以門店為作用域進行過濾
 */
package com.enterprise.core.repository;

import com.enterprise.core.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByOrderNo(String orderNo);

    boolean existsByOrderNo(String orderNo);
}
