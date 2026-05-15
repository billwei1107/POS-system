/**
 * @file RefundService.java
 * @description 退款服務 / Refund service
 * @description_en Handles refund creation, approval, and completion with event publishing
 * @description_zh 處理退款建立、審批與完成流程，發布退款完成事件
 */
package com.enterprise.core.service;

import com.enterprise.common.dto.PageResponse;
import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.core.dto.request.CreateRefundRequest;
import com.enterprise.core.dto.response.RefundResponse;
import com.enterprise.core.entity.Order;
import com.enterprise.core.entity.OrderRefund;
import com.enterprise.core.event.RefundCompletedEvent;
import com.enterprise.core.repository.OrderRefundRepository;
import com.enterprise.core.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundService {

    private static final ZoneId POS_BUSINESS_ZONE = ZoneId.of("Asia/Taipei");

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

        BigDecimal activeRefundTotal = refundRepository.findByOrderId(req.orderId()).stream()
            .filter(refund -> refund.getStatus() != OrderRefund.RefundStatus.REJECTED)
            .map(OrderRefund::getRefundAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingRefundable = order.getGrandTotal().subtract(activeRefundTotal);
        if (req.refundAmount().compareTo(remainingRefundable) > 0) {
            throw new BusinessException("Refund amount exceeds remaining refundable total");
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
        OrderRefund refund = getRefundOrThrow(refundId);

        if (refund.getStatus() != OrderRefund.RefundStatus.APPROVED) {
            throw new BusinessException("Refund must be APPROVED before completing");
        }

        refund.setStatus(OrderRefund.RefundStatus.COMPLETED);
        refund.setProcessedAt(LocalDateTime.now());
        refundRepository.save(refund);

        Order order = orderRepository.findById(refund.getOrderId()).orElseThrow();
        BigDecimal completedRefundTotal = refundRepository.findByOrderId(refund.getOrderId()).stream()
            .filter(existing -> existing.getStatus() == OrderRefund.RefundStatus.COMPLETED)
            .map(OrderRefund::getRefundAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        eventPublisher.publishEvent(new RefundCompletedEvent(
            this,
            refund.getId(),
            refund.getOrderId(),
            order.getStoreId(),
            order.getMemberId(),
            refund.getRefundAmount(),
            order.getGrandTotal(),
            completedRefundTotal,
            refund.getRefundMethod()
        ));

        return refund;
    }

    // ========================================
    // 查詢退款 / Query refunds
    // ========================================
    @Transactional(readOnly = true)
    public RefundResponse getById(UUID refundId) {
        return RefundResponse.from(getRefundOrThrow(refundId));
    }

    @Transactional(readOnly = true)
    public PageResponse<RefundResponse> listByStore(UUID storeId,
                                                    UUID orderId,
                                                    OrderRefund.RefundStatus status,
                                                    LocalDateTime from,
                                                    LocalDateTime to,
                                                    Pageable pageable) {
        return PageResponse.of(refundRepository
            .findAll(buildRefundFilter(storeId, orderId, status, from, to), pageable)
            .map(RefundResponse::from));
    }

    @Transactional(readOnly = true)
    public UUID findOrderStoreId(UUID orderId) {
        return getOrderOrThrow(orderId).getStoreId();
    }

    @Transactional(readOnly = true)
    public UUID findStoreId(UUID refundId) {
        OrderRefund refund = getRefundOrThrow(refundId);
        return getOrderOrThrow(refund.getOrderId()).getStoreId();
    }

    private Order getOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private OrderRefund getRefundOrThrow(UUID refundId) {
        return refundRepository.findById(refundId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + refundId));
    }

    private Specification<OrderRefund> buildRefundFilter(UUID storeId,
                                                         UUID orderId,
                                                         OrderRefund.RefundStatus status,
                                                         LocalDateTime from,
                                                         LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            var orderSubquery = query.subquery(UUID.class);
            var orderRoot = orderSubquery.from(Order.class);
            orderSubquery.select(orderRoot.get("id"))
                .where(cb.equal(orderRoot.get("storeId"), storeId));
            predicates.add(root.get("orderId").in(orderSubquery));

            if (orderId != null) {
                predicates.add(cb.equal(root.get("orderId"), orderId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String generateRefundNo() {
        return "RF-" + ZonedDateTime.now(POS_BUSINESS_ZONE).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
               + "-" + String.valueOf(System.nanoTime()).substring(10);
    }
}
