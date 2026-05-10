/**
 * @file OrderService.java
 * @description 訂單服務 / Order service
 * @description_en Handles order creation, status transitions, and completion flow
 * @description_zh 處理訂單建立、狀態流轉與結帳完成流程，發布對應 Spring 事件
 */
package com.enterprise.core.service;

import com.enterprise.common.dto.PageResponse;
import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.core.dto.request.CreateOrderRequest;
import com.enterprise.core.dto.response.OrderItemResponse;
import com.enterprise.core.dto.response.OrderResponse;
import com.enterprise.core.entity.Order;
import com.enterprise.core.entity.OrderItem;
import com.enterprise.core.entity.OrderPayment;
import com.enterprise.core.event.OrderCompletedEvent;
import com.enterprise.core.event.OrderCreatedEvent;
import com.enterprise.core.event.OrderVoidedEvent;
import com.enterprise.core.repository.OrderItemRepository;
import com.enterprise.core.repository.OrderPaymentRepository;
import com.enterprise.core.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final PricingEngine pricingEngine;
    private final OrderStateMachine stateMachine;
    private final ApplicationEventPublisher eventPublisher;

    // ========================================
    // 建立訂單 / Create new order
    // ========================================
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        var pricing = pricingEngine.calculate(req.items(), req.discountAmount(), req.taxIncluded());

        Order order = new Order();
        order.setOrderNo(generateOrderNo(req.storeId()));
        order.setStoreId(req.storeId());
        order.setTerminalId(req.terminalId());
        order.setEmployeeId(req.employeeId());
        order.setOrderType(req.orderType() != null ? req.orderType() : Order.OrderType.DINE_IN);
        order.setMemberId(req.memberId());
        order.setTableNo(req.tableNo());
        order.setGuestCount(req.guestCount());
        order.setNote(req.note());
        order.setSubtotal(pricing.subtotal());
        order.setDiscountTotal(pricing.discountTotal());
        order.setTaxTotal(pricing.taxTotal());
        order.setRoundingAdj(pricing.roundingAdj());
        order.setGrandTotal(pricing.grandTotal());
        orderRepository.save(order);

        // 儲存明細 / Save line items
        List<OrderItem> savedItems = req.items().stream().map(itemReq -> {
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setItemId(itemReq.itemId());
            oi.setVariantId(itemReq.variantId());
            oi.setItemNameSnapshot(itemReq.itemNameSnapshot());
            oi.setSkuSnapshot(itemReq.skuSnapshot());
            oi.setUnitPrice(itemReq.unitPrice());
            oi.setQuantity(itemReq.quantity());
            oi.setDiscountAmount(BigDecimal.ZERO);
            oi.setLineTotal(itemReq.unitPrice().multiply(itemReq.quantity()));
            oi.setNote(itemReq.note());
            return orderItemRepository.save(oi);
        }).toList();

        eventPublisher.publishEvent(new OrderCreatedEvent(this, order.getId(), order.getStoreId(), order.getOrderNo()));

        return OrderResponse.from(order, savedItems.stream().map(OrderItemResponse::from).toList());
    }

    // ========================================
    // 完成付款，推進至 COMPLETED / Complete payment
    // ========================================
    @Transactional
    public OrderResponse completeOrder(UUID orderId, String payMethod, BigDecimal tendered) {
        Order order = getOrThrow(orderId);
        advanceToCompleted(order);

        BigDecimal change = pricingEngine.calculateChange(tendered, order.getGrandTotal());
        order.setPaidTotal(order.getGrandTotal());
        order.setChangeGiven(change);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        OrderPayment payment = new OrderPayment();
        payment.setOrderId(orderId);
        payment.setPayMethod(payMethod);
        payment.setAmount(order.getGrandTotal());
        payment.setTendered(tendered);
        payment.setChangeGiven(change);
        orderPaymentRepository.save(payment);

        eventPublisher.publishEvent(new OrderCompletedEvent(
            this, order.getId(), order.getStoreId(), order.getOrderNo(),
            order.getMemberId(), order.getGrandTotal(), order.getTaxTotal(), payMethod,
            order.getPaidTotal(), tendered, change
        ));

        List<OrderItemResponse> items = orderItemRepository
            .findByOrderIdOrderBySortOrder(orderId).stream()
            .map(OrderItemResponse::from).toList();
        return OrderResponse.from(order, items);
    }

    // ========================================
    // 作廢訂單 / Void order
    // ========================================
    @Transactional
    public OrderResponse voidOrder(UUID orderId, UUID voidedBy, String reason) {
        Order order = getOrThrow(orderId);
        stateMachine.transition(order, Order.OrderStatus.VOIDED);
        order.setVoidedAt(LocalDateTime.now());
        order.setVoidedBy(voidedBy);
        order.setVoidReason(reason);
        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderVoidedEvent(
            this, order.getId(), order.getStoreId(), order.getOrderNo(), reason
        ));

        List<OrderItemResponse> items = orderItemRepository
            .findByOrderIdOrderBySortOrder(orderId).stream()
            .map(OrderItemResponse::from).toList();
        return OrderResponse.from(order, items);
    }

    // ========================================
    // 查詢訂單 / Query orders
    // ========================================
    @Transactional(readOnly = true)
    public OrderResponse getById(UUID orderId) {
        Order order = getOrThrow(orderId);
        List<OrderItemResponse> items = orderItemRepository
            .findByOrderIdOrderBySortOrder(orderId).stream()
            .map(OrderItemResponse::from).toList();
        return OrderResponse.from(order, items);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listByStore(UUID storeId, Order.OrderStatus status,
                                                    LocalDateTime from, LocalDateTime to,
                                                    Pageable pageable) {
        Page<Order> page = orderRepository.findAll(buildStoreFilter(storeId, status, from, to), pageable);
        return PageResponse.of(page.map(o -> {
            List<OrderItemResponse> items = orderItemRepository
                .findByOrderIdOrderBySortOrder(o.getId()).stream()
                .map(OrderItemResponse::from).toList();
            return OrderResponse.from(o, items);
        }));
    }

    // ========================================
    // 工具方法 / Utility methods
    // ========================================
    private Order getOrThrow(UUID id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    // ========================================
    // 推進至完成 / Advance lifecycle to completed
    // ========================================
    private void advanceToCompleted(Order order) {
        if (order.getStatus() == Order.OrderStatus.DRAFT) {
            stateMachine.transition(order, Order.OrderStatus.CONFIRMED);
        }
        if (order.getStatus() == Order.OrderStatus.CONFIRMED) {
            stateMachine.transition(order, Order.OrderStatus.PREPARING);
        }
        if (order.getStatus() == Order.OrderStatus.PREPARING) {
            stateMachine.transition(order, Order.OrderStatus.READY);
        }
        stateMachine.transition(order, Order.OrderStatus.COMPLETED);
    }

    // ========================================
    // 訂單查詢條件 / Order query filters
    // ========================================
    private Specification<Order> buildStoreFilter(UUID storeId, Order.OrderStatus status,
                                                  LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("storeId"), storeId));
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

    private String generateOrderNo(UUID storeId) {
        String prefix = storeId.toString().substring(0, 6).toUpperCase();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = String.valueOf(System.nanoTime()).substring(10);
        String candidate = prefix + "-" + timestamp + "-" + suffix;
        if (orderRepository.existsByOrderNo(candidate)) {
            candidate = candidate + "X";
        }
        return candidate;
    }
}
