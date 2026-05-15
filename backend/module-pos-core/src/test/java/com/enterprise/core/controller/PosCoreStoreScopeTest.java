/**
 * @file PosCoreStoreScopeTest.java
 * @description POS 核心門店資料範圍測試 / POS core store data scope tests
 * @description_en Verifies order, held-order and refund controllers guard store-scoped operations
 * @description_zh 驗證訂單、掛單與退款 Controller 在操作前會先檢查門店資料範圍
 */
package com.enterprise.core.controller;

import com.enterprise.common.dto.PageResponse;
import com.enterprise.core.dto.request.CreateHeldOrderRequest;
import com.enterprise.core.dto.request.CreateOrderRequest;
import com.enterprise.core.dto.request.CreateRefundRequest;
import com.enterprise.core.dto.response.HeldOrderResponse;
import com.enterprise.core.dto.response.OrderResponse;
import com.enterprise.core.dto.response.RefundResponse;
import com.enterprise.core.entity.Order;
import com.enterprise.core.entity.OrderRefund;
import com.enterprise.core.service.HeldOrderService;
import com.enterprise.core.service.OrderService;
import com.enterprise.core.service.RefundService;
import com.enterprise.organization.service.StoreAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PosCoreStoreScopeTest {

    @Mock
    private OrderService orderService;

    @Mock
    private HeldOrderService heldOrderService;

    @Mock
    private RefundService refundService;

    @Mock
    private StoreAccessService storeAccessService;

    private OrderController orderController;
    private HeldOrderController heldOrderController;
    private RefundController refundController;

    @BeforeEach
    void setUp() {
        orderController = new OrderController(orderService, storeAccessService);
        heldOrderController = new HeldOrderController(heldOrderService, storeAccessService);
        refundController = new RefundController(refundService, storeAccessService);
    }

    @Test
    void orderMutationsShouldRequireOperableStore() {
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest(
            storeId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            Order.OrderType.DINE_IN,
            List.of(),
            BigDecimal.ZERO,
            null,
            null,
            null,
            null,
            null,
            null,
            1,
            null,
            false
        );

        when(orderService.createOrder(request)).thenReturn(orderResponse(orderId, storeId));
        when(orderService.findStoreId(orderId)).thenReturn(storeId);
        when(orderService.completeOrder(eq(orderId), eq("CASH"), any())).thenReturn(orderResponse(orderId, storeId));
        when(orderService.voidOrder(eq(orderId), any(), eq("mistake"))).thenReturn(orderResponse(orderId, storeId));

        orderController.createOrder(request);
        orderController.completeOrder(orderId, "CASH", BigDecimal.valueOf(120));
        orderController.voidOrder(orderId, UUID.randomUUID(), "mistake");

        verify(storeAccessService, times(3)).requireOperableStore(storeId);
    }

    @Test
    void orderReadsShouldRequireReadableStore() {
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(orderService.findStoreId(orderId)).thenReturn(storeId);
        when(orderService.getById(orderId)).thenReturn(orderResponse(orderId, storeId));
        when(orderService.listByStore(eq(storeId), any(), any(), any(), any()))
            .thenReturn(PageResponse.of(new PageImpl<>(List.of(orderResponse(orderId, storeId)))));

        orderController.getById(orderId);
        orderController.listByStore(storeId, null, null, null, 0, 20);

        verify(storeAccessService, times(2)).requireReadableStore(storeId);
    }

    @Test
    void heldOrderEndpointsShouldGuardStoreScope() {
        UUID storeId = UUID.randomUUID();
        UUID heldOrderId = UUID.randomUUID();
        UUID terminalId = UUID.randomUUID();
        CreateHeldOrderRequest request = new CreateHeldOrderRequest(storeId, terminalId, "桌邊", "{\"items\":[]}");

        when(heldOrderService.create(request)).thenReturn(heldOrderResponse(heldOrderId, storeId, terminalId));
        when(heldOrderService.list(storeId, terminalId))
            .thenReturn(List.of(heldOrderResponse(heldOrderId, storeId, terminalId)));
        when(heldOrderService.findStoreId(heldOrderId)).thenReturn(storeId);

        heldOrderController.create(request);
        heldOrderController.list(storeId, terminalId);
        heldOrderController.delete(heldOrderId);

        verify(storeAccessService).requireReadableStore(storeId);
        verify(storeAccessService, times(2)).requireOperableStore(storeId);
    }

    @Test
    void refundEndpointsShouldGuardOrderStoreScope() {
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID refundId = UUID.randomUUID();
        CreateRefundRequest request = new CreateRefundRequest(
            orderId,
            BigDecimal.valueOf(50),
            "CASH",
            "退貨",
            UUID.randomUUID()
        );

        when(refundService.findOrderStoreId(orderId)).thenReturn(storeId);
        when(refundService.createRefund(request)).thenReturn(refund(refundId, orderId));
        when(refundService.findStoreId(refundId)).thenReturn(storeId);
        when(refundService.completeRefund(refundId)).thenReturn(refund(refundId, orderId));
        when(refundService.getById(refundId)).thenReturn(RefundResponse.from(refund(refundId, orderId)));
        when(refundService.listByStore(eq(storeId), eq(orderId), any(), any(), any(), any()))
            .thenReturn(PageResponse.of(new PageImpl<>(List.of(RefundResponse.from(refund(refundId, orderId))))));

        refundController.listRefunds(storeId, orderId, null, null, null, 0, 20);
        refundController.getById(refundId);
        refundController.createRefund(request);
        refundController.completeRefund(refundId);

        verify(storeAccessService, times(2)).requireReadableStore(storeId);
        verify(storeAccessService, times(2)).requireOperableStore(storeId);
    }

    private OrderResponse orderResponse(UUID orderId, UUID storeId) {
        return new OrderResponse(
            orderId,
            "TEST-ORDER",
            storeId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            Order.OrderStatus.DRAFT,
            Order.OrderType.DINE_IN,
            BigDecimal.valueOf(100),
            BigDecimal.ZERO,
            null,
            null,
            null,
            null,
            BigDecimal.valueOf(5),
            BigDecimal.ZERO,
            BigDecimal.valueOf(105),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            null,
            null,
            null,
            1,
            null,
            null,
            List.of()
        );
    }

    private HeldOrderResponse heldOrderResponse(UUID id, UUID storeId, UUID terminalId) {
        return new HeldOrderResponse(id, storeId, terminalId, "桌邊", "{\"items\":[]}", null, null);
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
