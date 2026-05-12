/**
 * @file InventoryEventListenerTest.java
 * @description 庫存事件監聽器單元測試 / Inventory event listener unit tests
 * @description_en Verifies order-completed events deduct only tracked inventory items
 * @description_zh 驗證訂單完成事件只針對需追蹤庫存的商品扣庫存
 */
package com.enterprise.inventory;

import com.enterprise.core.entity.OrderItem;
import com.enterprise.core.entity.OrderRefund;
import com.enterprise.core.event.OrderCompletedEvent;
import com.enterprise.core.event.RefundCompletedEvent;
import com.enterprise.core.repository.OrderItemRepository;
import com.enterprise.core.repository.OrderRefundRepository;
import com.enterprise.inventory.entity.StockMovement;
import com.enterprise.inventory.repository.StockMovementRepository;
import com.enterprise.inventory.service.InventoryEventListener;
import com.enterprise.inventory.service.StockDeductionService;
import com.enterprise.product.entity.ProductItem;
import com.enterprise.product.repository.ProductItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryEventListenerTest {

    @Mock private StockDeductionService deductionService;
    @Mock private StockMovementRepository movementRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderRefundRepository refundRepository;
    @Mock private ProductItemRepository productItemRepository;

    @InjectMocks private InventoryEventListener listener;

    @Test
    @DisplayName("訂單完成時應扣減需追蹤庫存商品")
    void onOrderCompleted_deductsTrackedItems() {
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(itemId);
        orderItem.setQuantity(new BigDecimal("2"));
        ProductItem product = new ProductItem();
        product.setTrackInventory(true);

        when(movementRepository.findFirstByReferenceIdAndMovementType(orderId, StockMovement.MovementType.SALE))
                .thenReturn(Optional.empty());
        when(orderItemRepository.findByOrderIdOrderBySortOrder(orderId)).thenReturn(List.of(orderItem));
        when(productItemRepository.findById(itemId)).thenReturn(Optional.of(product));

        listener.onOrderCompleted(event(orderId, storeId));

        verify(deductionService).deductForSale(storeId, itemId, new BigDecimal("2"), orderId);
    }

    @Test
    @DisplayName("不追蹤庫存商品完成訂單時不扣庫存")
    void onOrderCompleted_skipsUntrackedItems() {
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(itemId);
        orderItem.setQuantity(BigDecimal.ONE);
        ProductItem product = new ProductItem();
        product.setTrackInventory(false);

        when(movementRepository.findFirstByReferenceIdAndMovementType(orderId, StockMovement.MovementType.SALE))
                .thenReturn(Optional.empty());
        when(orderItemRepository.findByOrderIdOrderBySortOrder(orderId)).thenReturn(List.of(orderItem));
        when(productItemRepository.findById(itemId)).thenReturn(Optional.of(product));

        listener.onOrderCompleted(event(orderId, storeId));

        verifyNoInteractions(deductionService);
    }

    @Test
    @DisplayName("已處理過的訂單不得重複扣庫存")
    void onOrderCompleted_skipsAlreadyProcessedOrder() {
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        when(movementRepository.findFirstByReferenceIdAndMovementType(orderId, StockMovement.MovementType.SALE))
                .thenReturn(Optional.of(new StockMovement()));

        listener.onOrderCompleted(event(orderId, storeId));

        verifyNoInteractions(orderItemRepository, productItemRepository, deductionService);
    }

    @Test
    @DisplayName("全額退款完成時應回補需追蹤庫存商品")
    void onRefundCompleted_fullRefund_returnsTrackedItems() {
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID refundId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(itemId);
        orderItem.setQuantity(new BigDecimal("2"));
        OrderRefund refund = new OrderRefund();
        refund.setOrderId(orderId);
        refund.setRefundAmount(new BigDecimal("120.00"));
        refund.setStatus(OrderRefund.RefundStatus.COMPLETED);
        ProductItem product = new ProductItem();
        product.setTrackInventory(true);

        when(movementRepository.findFirstByReferenceIdAndMovementType(refundId, StockMovement.MovementType.RETURN))
                .thenReturn(Optional.empty());
        when(refundRepository.findByOrderId(orderId)).thenReturn(List.of(refund));
        when(orderItemRepository.findByOrderIdOrderBySortOrder(orderId)).thenReturn(List.of(orderItem));
        when(productItemRepository.findById(itemId)).thenReturn(Optional.of(product));

        listener.onRefundCompleted(new RefundCompletedEvent(
                this,
                refundId,
                orderId,
                storeId,
                new BigDecimal("120.00"),
                new BigDecimal("120.00"),
                "CASH"));

        verify(deductionService).returnForRefund(storeId, itemId, new BigDecimal("2"), refundId);
    }

    @Test
    @DisplayName("部分退款完成時不應在缺少品項資訊下回補整單庫存")
    void onRefundCompleted_partialRefund_skipsStockReturn() {
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID refundId = UUID.randomUUID();
        OrderRefund refund = new OrderRefund();
        refund.setOrderId(orderId);
        refund.setRefundAmount(new BigDecimal("60.00"));
        refund.setStatus(OrderRefund.RefundStatus.COMPLETED);

        when(movementRepository.findFirstByReferenceIdAndMovementType(refundId, StockMovement.MovementType.RETURN))
                .thenReturn(Optional.empty());
        when(refundRepository.findByOrderId(orderId)).thenReturn(List.of(refund));

        listener.onRefundCompleted(new RefundCompletedEvent(
                this,
                refundId,
                orderId,
                storeId,
                new BigDecimal("60.00"),
                new BigDecimal("120.00"),
                "CASH"));

        verifyNoInteractions(orderItemRepository, productItemRepository, deductionService);
    }

    private OrderCompletedEvent event(UUID orderId, UUID storeId) {
        return new OrderCompletedEvent(
                this,
                orderId,
                storeId,
                "POS-TEST",
                null,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                "CASH",
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.ZERO);
    }
}
