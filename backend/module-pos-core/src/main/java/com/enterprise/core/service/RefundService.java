/**
 * @file RefundService.java
 * @description 退款服務 / Refund service
 * @description_en Handles refund creation, approval, and completion with event publishing
 * @description_zh 處理退款建立、審批與完成流程，發布退款完成事件
 */
package com.enterprise.core.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.core.dto.request.CreateRefundRequest;
import com.enterprise.core.entity.Order;
import com.enterprise.core.entity.OrderRefund;
import com.enterprise.core.event.RefundCompletedEvent;
import com.enterprise.core.repository.OrderRefundRepository;
import com.enterprise.core.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final OrderRefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ========================================
    // 建立退款申請 / Create refund request
    // ========================================
    @Transactional
    public OrderRefund createRefund(CreateRefundRequest req) {
        Order order = orderRepository.findById(req.orderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + req.orderId()));

        if (order.getStatus() != Order.OrderStatus.COMPLETED &&
            order.getStatus() != Order.OrderStatus.CLOSED) {
            throw new BusinessException("Refund only allowed on COMPLETED or CLOSED orders");
        }

        if (req.refundAmount().compareTo(order.getGrandTotal()) > 0) {
            throw new BusinessException("Refund amount exceeds order grand total");
        }

        OrderRefund refund = new OrderRefund();
        refund.setOrderId(req.orderId());
        refund.setRefundNo(generateRefundNo());
        refund.setRefundAmount(req.refundAmount());
        refund.setRefundMethod(req.refundMethod());
        refund.setReason(req.reason());
        refund.setApprovedBy(req.approvedBy());
        // 有審批人即視為已核准，否則進入 PENDING
        refund.setStatus(req.approvedBy() != null ? OrderRefund.RefundStatus.APPROVED : OrderRefund.RefundStatus.PENDING);
        return refundRepository.save(refund);
    }

    // ========================================
    // 完成退款 / Complete refund processing
    // ========================================
    @Transactional
    public OrderRefund completeRefund(UUID refundId) {
        OrderRefund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + refundId));

        if (refund.getStatus() != OrderRefund.RefundStatus.APPROVED) {
            throw new BusinessException("Refund must be APPROVED before completing");
        }

        refund.setStatus(OrderRefund.RefundStatus.COMPLETED);
        refund.setProcessedAt(LocalDateTime.now());
        refundRepository.save(refund);

        Order order = orderRepository.findById(refund.getOrderId()).orElseThrow();
        eventPublisher.publishEvent(new RefundCompletedEvent(
            this, refund.getId(), refund.getOrderId(), order.getStoreId(), refund.getRefundAmount()
        ));

        return refund;
    }

    private String generateRefundNo() {
        return "RF-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
               + "-" + String.valueOf(System.nanoTime()).substring(10);
    }
}
