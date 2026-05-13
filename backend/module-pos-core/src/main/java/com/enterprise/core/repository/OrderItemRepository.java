/**
 * @file OrderItemRepository.java
 * @description 訂單明細 Repository / Order item repository
 * @description_en JPA repository for order line item queries
 * @description_zh 訂單明細查詢
 */
package com.enterprise.core.repository;

import com.enterprise.core.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderIdOrderBySortOrder(UUID orderId);
}
