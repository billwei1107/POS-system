/**
 * @file OrderServiceTest.java
 * @description 訂單服務測試 / Order service tests
 * @description_en Verifies checkout completion and order lifecycle behavior
 * @description_zh 驗證結帳完成與訂單生命週期推進行為
 */
package com.enterprise.core.service;

import com.enterprise.core.dto.response.OrderResponse;
import com.enterprise.core.dto.request.CreateOrderRequest;
import com.enterprise.core.dto.request.OrderItemRequest;
import com.enterprise.core.dto.response.PricingResult;
import com.enterprise.core.entity.Order;
import com.enterprise.core.entity.OrderPayment;
import com.enterprise.core.entity.OrderItem;
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
import static org.mockito.ArgumentMatchers.eq;
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
    private PriceRuleResolver priceRuleResolver;

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
            priceRuleResolver,
            new OrderStateMachine(),
            eventPublisher
        );
    }

    // ========================================
    // 建立訂單 / Create order
    // ========================================
    @Test
    void createOrder_repricesItemsBeforeCalculatingAndSavingLines() {
        UUID storeId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        OrderItemRequest originalItem = new OrderItemRequest(
            itemId, null, "Client Name", "CLIENT-SKU",
            new BigDecimal("120.00"), new BigDecimal("2.00"),
            BigDecimal.ZERO, null, List.of()
        );
        OrderItemRequest pricedItem = new OrderItemRequest(
            itemId, null, "Master Name", "MASTER-SKU",
            new BigDecimal("90.00"), new BigDecimal("2.00"),
            BigDecimal.ZERO, null, List.of()
        );
        CreateOrderRequest request = new CreateOrderRequest(
            storeId, UUID.randomUUID(), UUID.randomUUID(), Order.OrderType.DINE_IN,
            List.of(originalItem), BigDecimal.ZERO, null, null, null, null, memberId, null, 1, null, false
        );
        PricingResult pricing = new PricingResult(
            new BigDecimal("180.00"), BigDecimal.ZERO, new BigDecimal("9.00"),
            BigDecimal.ZERO, new BigDecimal("189.00")
        );

        when(priceRuleResolver.resolveOrderItems(any(), any(), any(), any())).thenReturn(List.of(pricedItem));
        when(pricingEngine.calculate(List.of(pricedItem), BigDecimal.ZERO, false)).thenReturn(pricing);
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getItemNameSnapshot()).isEqualTo("Master Name");
        assertThat(itemCaptor.getValue().getSkuSnapshot()).isEqualTo("MASTER-SKU");
        assertThat(itemCaptor.getValue().getUnitPrice()).isEqualByComparingTo("90.00");
        assertThat(itemCaptor.getValue().getLineTotal()).isEqualByComparingTo("180.00");
        assertThat(response.grandTotal()).isEqualByComparingTo("189.00");
        verify(priceRuleResolver).resolveOrderItems(eq(List.of(originalItem)), eq(storeId), eq(memberId), any());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void createOrder_promotionDiscount_persistsDiscountMetadata() {
        UUID storeId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID promotionRuleId = UUID.randomUUID();
        OrderItemRequest item = new OrderItemRequest(
            itemId, null, "燕麥拿鐵 12oz", "DEMO-OAT-LATTE-12OZ",
            new BigDecimal("145.00"), BigDecimal.ONE,
            BigDecimal.ZERO, null, List.of()
        );
        CreateOrderRequest request = new CreateOrderRequest(
            storeId, UUID.randomUUID(), UUID.randomUUID(), Order.OrderType.DINE_IN,
            List.of(item), new BigDecimal("14.50"), Order.DiscountSource.PROMOTION,
            promotionRuleId, "cafe20", "咖啡滿百 9 折", null, null, 1, null, false
        );
        PricingResult pricing = new PricingResult(
            new BigDecimal("145.00"), new BigDecimal("14.50"), new BigDecimal("6.53"),
            BigDecimal.ZERO, new BigDecimal("137.03")
        );

        when(priceRuleResolver.resolveOrderItems(any(), any(), any(), any())).thenReturn(List.of(item));
        when(pricingEngine.calculate(List.of(item), new BigDecimal("14.50"), false)).thenReturn(pricing);
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getDiscountSource()).isEqualTo(Order.DiscountSource.PROMOTION);
        assertThat(orderCaptor.getValue().getPromotionRuleId()).isEqualTo(promotionRuleId);
        assertThat(orderCaptor.getValue().getPromotionCode()).isEqualTo("CAFE20");
        assertThat(orderCaptor.getValue().getDiscountLabel()).isEqualTo("咖啡滿百 9 折");
        assertThat(response.discountSource()).isEqualTo(Order.DiscountSource.PROMOTION);
        assertThat(response.promotionRuleId()).isEqualTo(promotionRuleId);
        assertThat(response.promotionCode()).isEqualTo("CAFE20");
        assertThat(response.discountLabel()).isEqualTo("咖啡滿百 9 折");
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
