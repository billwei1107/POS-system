/**
 * @file OrderRefundRepository.java
 * @description 退款記錄 Repository / Order refund repository
 * @description_en JPA repository for refund records with status filtering
 * @description_zh 退款記錄查詢，支援狀態過濾
 */
package com.enterprise.core.repository;

import com.enterprise.core.entity.OrderRefund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRefundRepository extends JpaRepository<OrderRefund, UUID> {

    List<OrderRefund> findByOrderId(UUID orderId);

    Optional<OrderRefund> findByRefundNo(String refundNo);

    List<OrderRefund> findByStatus(OrderRefund.RefundStatus status);
}
