/**
 * @file InventoryEventListenerTest.java
 * @description 庫存事件監聽器單元測試 / Inventory event listener unit tests
 * @description_en Verifies order-completed events deduct only tracked inventory items
 * @description_zh 驗證訂單完成事件只針對需追蹤庫存的商品扣庫存
 */
package com.enterprise.inventory;

import com.enterprise.core.entity.OrderItem;
import com.enterprise.core.event.OrderCompletedEvent;
import com.enterprise.core.repository.OrderItemRepository;
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
