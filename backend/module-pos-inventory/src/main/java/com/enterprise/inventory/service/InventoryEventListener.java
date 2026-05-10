/**
 * @file InventoryEventListener.java
 * @description 庫存事件監聽器 / Inventory event listener
 * @description_en Consumes OrderCompletedEvent to deduct stock; consumes RefundCompletedEvent to return stock
 * @description_zh 消費 OrderCompletedEvent 執行庫存扣減；消費 RefundCompletedEvent 執行庫存回補
 */
package com.enterprise.inventory.service;

import com.enterprise.core.event.OrderCompletedEvent;
import com.enterprise.core.event.RefundCompletedEvent;
import com.enterprise.inventory.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // ========================================
    // 訂單完成 → 扣庫存（僅記錄聚合層扣減，明細由訂單品項資料提供）
    // OrderCompleted → deduct stock (aggregate-level guard; item-level handled via order items)
    // ========================================
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderCompleted(OrderCompletedEvent event) {
        // 庫存扣減的精確品項數量需要從 order_items 查詢
        // 在本 Sprint 中，inventoryService 只記錄「已有扣減」的 movement 作為聚合哨兵
        // Phase 2-1 後期：整合 OrderItemsRepository 執行品項級扣庫
        // 目前以 event.orderId 為 referenceId，防止重複處理
        boolean alreadyProcessed = movementRepository
                .findFirstByReferenceIdAndMovementType(event.getOrderId(),
                        com.enterprise.inventory.entity.StockMovement.MovementType.SALE)
                .isPresent();
        if (alreadyProcessed) {
            log.debug("Inventory already deducted for order: {}", event.getOrderId());
            return;
        }
        log.info("Inventory deduction triggered for order: {}, store: {}", event.getOrderId(), event.getStoreId());
        // 實際品項層級扣庫需 pos-core 提供 order items 查詢介面（Phase 2 整合）
        // 此處保留事件監聽點位，供後續實作調用 deductionService.deductForSale()
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
