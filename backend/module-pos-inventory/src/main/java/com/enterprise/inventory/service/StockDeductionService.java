/**
 * @file StockDeductionService.java
 * @description 庫存扣減服務 / Stock deduction service
 * @description_en Handles concurrent-safe stock deduction using pessimistic locking; auto-creates stock record if absent
 * @description_zh 使用悲觀鎖確保高併發安全的庫存扣減；若庫存紀錄不存在則自動建立
 */
package com.enterprise.inventory.service;

import com.enterprise.inventory.entity.StockMovement;
import com.enterprise.inventory.entity.StoreStock;
import com.enterprise.inventory.repository.StockMovementRepository;
import com.enterprise.inventory.repository.StoreStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDeductionService {

    private final StoreStockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final StockAlertService alertService;

    // ========================================
    // 庫存扣減（售出）/ Deduct stock on sale
    // ========================================
    @Transactional
    public void deductForSale(UUID storeId, UUID itemId, BigDecimal qty, UUID orderId) {
        StoreStock stock = getOrCreateStock(storeId, itemId);
        if (stock.getQuantity().compareTo(qty) < 0) {
            log.warn("Insufficient stock: storeId={}, itemId={}, available={}, requested={}",
                     storeId, itemId, stock.getQuantity(), qty);
        }
        stock.setQuantity(stock.getQuantity().subtract(qty));
        stockRepository.save(stock);
        recordMovement(storeId, itemId, qty.negate(), StockMovement.MovementType.SALE, orderId, "pos_orders");
        alertService.checkAndRaiseAlert(stock);
    }

    // ========================================
    // 庫存回補（退款）/ Return stock on refund
    // ========================================
    @Transactional
    public void returnForRefund(UUID storeId, UUID itemId, BigDecimal qty, UUID refundId) {
        StoreStock stock = getOrCreateStock(storeId, itemId);
        stock.setQuantity(stock.getQuantity().add(qty));
        stockRepository.save(stock);
        recordMovement(storeId, itemId, qty, StockMovement.MovementType.RETURN, refundId, "pos_refunds");
    }

    // ========================================
    // 手動調整庫存 / Manual stock adjustment
    // ========================================
    @Transactional
    public void adjust(UUID storeId, UUID itemId, BigDecimal adjustQty, UUID operatedBy, String notes) {
        StoreStock stock = getOrCreateStock(storeId, itemId);
        stock.setQuantity(stock.getQuantity().add(adjustQty));
        stockRepository.save(stock);

        StockMovement movement = new StockMovement();
        movement.setStoreId(storeId);
        movement.setItemId(itemId);
        movement.setQuantityChange(adjustQty);
        movement.setMovementType(StockMovement.MovementType.ADJUSTMENT);
        movement.setOperatedBy(operatedBy);
        movement.setNotes(notes);
        movementRepository.save(movement);
        alertService.checkAndRaiseAlert(stock);
    }

    // ========================================
    // 入庫 / Receiving stock
    // ========================================
    @Transactional
    public void receive(UUID storeId, UUID itemId, BigDecimal qty, UUID referenceId, String notes) {
        StoreStock stock = getOrCreateStock(storeId, itemId);
        stock.setQuantity(stock.getQuantity().add(qty));
        stockRepository.save(stock);
        StockMovement movement = new StockMovement();
        movement.setStoreId(storeId);
        movement.setItemId(itemId);
        movement.setQuantityChange(qty);
        movement.setMovementType(StockMovement.MovementType.RECEIVING);
        movement.setReferenceId(referenceId);
        movement.setReferenceType("transfer");
        movement.setNotes(notes);
        movementRepository.save(movement);
    }

    // ========================================
    // 取得或建立庫存紀錄（悲觀鎖）/ Get or create stock record with pessimistic lock
    // ========================================
    private StoreStock getOrCreateStock(UUID storeId, UUID itemId) {
        return stockRepository.findByStoreIdAndItemIdForUpdate(storeId, itemId)
                .orElseGet(() -> {
                    StoreStock s = new StoreStock();
                    s.setStoreId(storeId);
                    s.setItemId(itemId);
                    return stockRepository.save(s);
                });
    }

    private void recordMovement(UUID storeId, UUID itemId, BigDecimal change,
                                 StockMovement.MovementType type, UUID refId, String refType) {
        StockMovement m = new StockMovement();
        m.setStoreId(storeId);
        m.setItemId(itemId);
        m.setQuantityChange(change);
        m.setMovementType(type);
        m.setReferenceId(refId);
        m.setReferenceType(refType);
        movementRepository.save(m);
    }
}
