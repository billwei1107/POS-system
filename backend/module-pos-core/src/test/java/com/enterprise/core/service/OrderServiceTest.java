/**
 * @file OrderServiceTest.java
 * @description 訂單服務測試 / Order service tests
 * @description_en Verifies checkout completion and order lifecycle behavior
 * @description_zh 驗證結帳完成與訂單生命週期推進行為
 */
package com.enterprise.core.service;

import com.enterprise.core.dto.response.OrderResponse;
import com.enterprise.core.entity.Order;
import com.enterprise.core.entity.OrderPayment;
import com.enterprise.core.repository.OrderItemRepository;
import com.enterprise.core.repository.OrderPaymentRepository;
import com.enterprise.core.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderPaymentRepository orderPaymentRepository;

    @Mock
    private PricingEngine pricingEngine;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
            orderRepository,
            orderItemRepository,
            orderPaymentRepository,
            pricingEngine,
            new OrderStateMachine(),
            eventPublisher
        );
    }

    // ========================================
    // 完成付款 / Complete payment
    // ========================================
    @Test
    void completeOrder_draftOrder_advancesThroughLifecycleAndSavesPayment() {
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        Order order = createOrder(orderId, storeId, Order.OrderStatus.DRAFT);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(pricingEngine.calculateChange(new BigDecimal("200.00"), order.getGrandTotal()))
            .thenReturn(new BigDecimal("74.00"));
        when(orderItemRepository.findByOrderIdOrderBySortOrder(orderId)).thenReturn(List.of());

        OrderResponse response = orderService.completeOrder(orderId, "CASH", new BigDecimal("200.00"));

        assertThat(response.status()).isEqualTo(Order.OrderStatus.COMPLETED);
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);
        assertThat(order.getPaidTotal()).isEqualByComparingTo("126.00");
        assertThat(order.getChangeGiven()).isEqualByComparingTo("74.00");
        assertThat(order.getCompletedAt()).isNotNull();

        ArgumentCaptor<OrderPayment> paymentCaptor = ArgumentCaptor.forClass(OrderPayment.class);
        verify(orderPaymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(paymentCaptor.getValue().getPayMethod()).isEqualTo("CASH");
        assertThat(paymentCaptor.getValue().getAmount()).isEqualByComparingTo("126.00");
        assertThat(paymentCaptor.getValue().getTendered()).isEqualByComparingTo("200.00");
        assertThat(paymentCaptor.getValue().getChangeGiven()).isEqualByComparingTo("74.00");

        verify(orderRepository).save(order);
        verify(eventPublisher).publishEvent(any());
    }

    private Order createOrder(UUID orderId, UUID storeId, Order.OrderStatus status) {
        Order order = new Order();
        order.setId(orderId);
        order.setStoreId(storeId);
        order.setOrderNo("TEST-ORDER-001");
        order.setStatus(status);
        order.setGrandTotal(new BigDecimal("126.00"));
        order.setSubtotal(new BigDecimal("120.00"));
        order.setTaxTotal(new BigDecimal("6.00"));
        order.setDiscountTotal(BigDecimal.ZERO);
        order.setRoundingAdj(BigDecimal.ZERO);
        return order;
    }
}
