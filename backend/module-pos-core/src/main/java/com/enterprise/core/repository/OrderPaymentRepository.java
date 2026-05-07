/**
 * @file OrderPaymentRepository.java
 * @description 付款記錄 Repository / Order payment repository
 * @description_en JPA repository for payment records per order
 * @description_zh 訂單付款記錄查詢
 */
package com.enterprise.core.repository;

import com.enterprise.core.entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, UUID> {

    List<OrderPayment> findByOrderId(UUID orderId);
}
