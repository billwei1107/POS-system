/**
 * @file RefundServiceTest.java
 * @description 退款服務測試 / Refund service tests
 * @description_en Verifies refund amount guards, approval state and completion events
 * @description_zh 驗證退款金額防呆、審批狀態與完成事件發布
 */
package com.enterprise.core.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.core.dto.request.CreateRefundRequest;
import com.enterprise.core.dto.response.RefundResponse;
import com.enterprise.core.entity.Order;
import com.enterprise.core.entity.OrderRefund;
import com.enterprise.core.event.RefundCompletedEvent;
import com.enterprise.core.repository.OrderRefundRepository;
import com.enterprise.core.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private OrderRefundRepository refundRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private RefundService refundService;

    @BeforeEach
    void setUp() {
        refundService = new RefundService(refundRepository, orderRepository, eventPublisher);
    }

    // ========================================
    // 建立退款 / Create refund
    // ========================================
    @Test
    void createRefund_withApprovedBy_setsApprovedStatus() {
        UUID orderId = UUID.randomUUID();
        UUID approvedBy = UUID.randomUUID();
        Order order = createCompletedOrder(orderId, new BigDecimal("126.00"));
        CreateRefundRequest request = new CreateRefundRequest(
            orderId,
            new BigDecimal("50.00"),
            "CASH",
            "Customer return",
            approvedBy
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(refundRepository.findByOrderId(orderId)).thenReturn(List.of());
        when(refundRepository.save(any(OrderRefund.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRefund refund = refundService.createRefund(request);

        assertThat(refund.getOrderId()).isEqualTo(orderId);
        assertThat(refund.getRefundAmount()).isEqualByComparingTo("50.00");
        assertThat(refund.getRefundMethod()).isEqualTo("CASH");
        assertThat(refund.getApprovedBy()).isEqualTo(approvedBy);
        assertThat(refund.getStatus()).isEqualTo(OrderRefund.RefundStatus.APPROVED);
        verify(refundRepository).save(refund);
    }

    @Test
    void createRefund_whenActiveRefundsExceedRemaining_throwsException() {
        UUID orderId = UUID.randomUUID();
        Order order = createCompletedOrder(orderId, new BigDecimal("126.00"));
        OrderRefund existing = new OrderRefund();
        existing.setOrderId(orderId);
        existing.setRefundAmount(new BigDecimal("100.00"));
        existing.setStatus(OrderRefund.RefundStatus.COMPLETED);
        CreateRefundRequest request = new CreateRefundRequest(
            orderId,
            new BigDecimal("30.00"),
            "CASH",
            "Second refund",
            UUID.randomUUID()
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(refundRepository.findByOrderId(orderId)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> refundService.createRefund(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("remaining refundable total");
    }

    // ========================================
    // 完成退款 / Complete refund
    // ========================================
    @Test
    void completeRefund_approvedRefund_marksCompletedAndPublishesEvent() {
        UUID refundId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Order order = createCompletedOrder(orderId, new BigDecimal("126.00"));
        order.setMemberId(memberId);
        OrderRefund refund = new OrderRefund();
        refund.setId(refundId);
        refund.setOrderId(orderId);
        refund.setRefundNo("RF-TEST");
        refund.setRefundAmount(new BigDecimal("50.00"));
        refund.setRefundMethod("CASH");
        refund.setStatus(OrderRefund.RefundStatus.APPROVED);

        when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(refundRepository.findByOrderId(orderId)).thenReturn(List.of(refund));

        OrderRefund completed = refundService.completeRefund(refundId);

        assertThat(completed.getStatus()).isEqualTo(OrderRefund.RefundStatus.COMPLETED);
        assertThat(completed.getProcessedAt()).isNotNull();
        verify(refundRepository).save(refund);
        ArgumentCaptor<RefundCompletedEvent> eventCaptor = ArgumentCaptor.forClass(RefundCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        RefundCompletedEvent event = eventCaptor.getValue();
        assertThat(event.getRefundId()).isEqualTo(refundId);
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getMemberId()).isEqualTo(memberId);
        assertThat(event.getCompletedRefundTotal()).isEqualByComparingTo("50.00");
    }

    // ========================================
    // 查詢退款 / Query refunds
    // ========================================
    @Test
    void listByStore_filtersRefundsAndMapsPageResponse() {
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderRefund refund = refund(UUID.randomUUID(), orderId);
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        PageRequest pageable = PageRequest.of(0, 10);
        when(refundRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<OrderRefund>>any(),
                org.mockito.ArgumentMatchers.eq(pageable)
        ))
                .thenReturn(new PageImpl<>(List.of(refund), pageable, 1));

        var response = refundService.listByStore(
                storeId,
                orderId,
                OrderRefund.RefundStatus.COMPLETED,
                from,
                to,
                pageable
        );

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(1);
        RefundResponse row = response.getContent().get(0);
        assertThat(row.id()).isEqualTo(refund.getId());
        assertThat(row.orderId()).isEqualTo(orderId);
        assertThat(row.status()).isEqualTo(OrderRefund.RefundStatus.APPROVED);
    }

    @Test
    void getById_existingRefund_returnsResponse() {
        UUID refundId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderRefund refund = refund(refundId, orderId);
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));

        RefundResponse response = refundService.getById(refundId);

        assertThat(response.id()).isEqualTo(refundId);
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.refundAmount()).isEqualByComparingTo("50");
    }

    private Order createCompletedOrder(UUID orderId, BigDecimal grandTotal) {
        Order order = new Order();
        order.setId(orderId);
        order.setStoreId(UUID.randomUUID());
        order.setOrderNo("TEST-ORDER");
        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setGrandTotal(grandTotal);
        return order;
    }

    private OrderRefund refund(UUID refundId, UUID orderId) {
        OrderRefund refund = new OrderRefund();
        refund.setId(refundId);
        refund.setOrderId(orderId);
        refund.setRefundNo("RF-TEST");
        refund.setRefundAmount(BigDecimal.valueOf(50));
        refund.setRefundMethod("CASH");
        refund.setStatus(OrderRefund.RefundStatus.APPROVED);
        return refund;
    }
}
