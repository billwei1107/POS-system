/**
 * @file InventoryEventListener.java
 * @description 庫存事件監聽器 / Inventory event listener
 * @description_en Consumes OrderCompletedEvent to deduct stock; consumes RefundCompletedEvent to return stock
 * @description_zh 消費 OrderCompletedEvent 執行庫存扣減；消費 RefundCompletedEvent 執行庫存回補
 */
package com.enterprise.inventory.service;

import com.enterprise.core.event.OrderCompletedEvent;
import com.enterprise.core.event.RefundCompletedEvent;
import com.enterprise.core.repository.OrderItemRepository;
import com.enterprise.inventory.entity.StockMovement;
import com.enterprise.inventory.repository.StockMovementRepository;
import com.enterprise.product.repository.ProductItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final StockDeductionService deductionService;
    private final StockMovementRepository movementRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductItemRepository productItemRepository;

    // ========================================
    // 訂單完成 → 交易內扣庫存 / OrderCompleted → deduct stock in order transaction
    // ========================================
    @EventListener
    public void onOrderCompleted(OrderCompletedEvent event) {
        boolean alreadyProcessed = movementRepository
                .findFirstByReferenceIdAndMovementType(event.getOrderId(),
                        StockMovement.MovementType.SALE)
                .isPresent();
        if (alreadyProcessed) {
            log.debug("Inventory already deducted for order: {}", event.getOrderId());
            return;
        }

        orderItemRepository.findByOrderIdOrderBySortOrder(event.getOrderId()).forEach(item -> {
            productItemRepository.findById(item.getItemId())
                    .filter(product -> Boolean.TRUE.equals(product.getTrackInventory()))
                    .ifPresent(product -> deductionService.deductForSale(
                            event.getStoreId(),
                            item.getItemId(),
                            item.getQuantity(),
                            event.getOrderId()));
        });
        log.info("Inventory deducted for completed order: {}, store: {}", event.getOrderId(), event.getStoreId());
    }

    // ========================================
    // 退款完成 → 回補庫存 / RefundCompleted → return stock
    // ========================================
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onRefundCompleted(RefundCompletedEvent event) {
        boolean alreadyProcessed = movementRepository
                .findFirstByReferenceIdAndMovementType(event.getRefundId(),
                        com.enterprise.inventory.entity.StockMovement.MovementType.RETURN)
                .isPresent();
        if (alreadyProcessed) {
            log.debug("Stock already returned for refund: {}", event.getRefundId());
            return;
        }
        log.info("Stock return triggered for refund: {}, store: {}", event.getRefundId(), event.getStoreId());
        // 實際品項層級回補同上，Phase 2 整合 order items 後調用 deductionService.returnForRefund()
    }
}
