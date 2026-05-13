/**
 * @file StockTakeService.java
 * @description 盤點服務 / Stock take service
 * @description_en Manages inventory counting sessions: start, submit counts, complete or cancel
 * @description_zh 管理盤點工作階段：開始盤點、登記盤點數、完成或取消
 */
package com.enterprise.inventory.service;

import com.enterprise.inventory.entity.StockMovement;
import com.enterprise.inventory.entity.StockTake;
import com.enterprise.inventory.entity.StockTakeItem;
import com.enterprise.inventory.entity.StoreStock;
import com.enterprise.inventory.repository.StockMovementRepository;
import com.enterprise.inventory.repository.StockTakeRepository;
import com.enterprise.inventory.repository.StoreStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockTakeService {

    private final StockTakeRepository stockTakeRepository;
    private final StoreStockRepository stockRepository;
    private final StockMovementRepository movementRepository;

    // ========================================
    // 開始盤點 / Start a stock take session
    // ========================================
    @Transactional
    public StockTake start(UUID storeId, UUID createdBy) {
        stockTakeRepository.findByStoreIdAndStatus(storeId, StockTake.StockTakeStatus.IN_PROGRESS)
                .ifPresent(existing -> {
                    throw new IllegalStateException("A stock take is already in progress for store: " + storeId);
                });

        StockTake take = new StockTake();
        take.setStoreId(storeId);
        take.setCreatedBy(createdBy);

        List<StoreStock> stocks = stockRepository.findAllByStoreId(storeId);
        stocks.forEach(stock -> {
            StockTakeItem item = new StockTakeItem();
            item.setStockTake(take);
            item.setItemId(stock.getItemId());
            item.setSystemQty(stock.getQuantity());
            take.getItems().add(item);
        });

        return stockTakeRepository.save(take);
    }

    // ========================================
    // 登記盤點數量 / Submit counted quantity for an item
    // ========================================
    @Transactional
    public StockTake submitCount(UUID stockTakeId, UUID itemId, BigDecimal countedQty) {
        StockTake take = findOrThrow(stockTakeId);
        if (take.getStatus() != StockTake.StockTakeStatus.IN_PROGRESS) {
            throw new IllegalStateException("Stock take is not in progress");
        }
        take.getItems().stream()
                .filter(i -> i.getItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not in stock take: " + itemId))
                .submitCount(countedQty);
        return stockTakeRepository.save(take);
    }

    // ========================================
    // 完成盤點（將差異寫入庫存）/ Complete stock take and apply adjustments
    // ========================================
    @Transactional
    public StockTake complete(UUID stockTakeId) {
        StockTake take = findOrThrow(stockTakeId);
        if (take.getStatus() != StockTake.StockTakeStatus.IN_PROGRESS) {
            throw new IllegalStateException("Stock take is not in progress");
        }

        take.getItems().stream()
                .filter(i -> i.getCountedQty() != null && i.getDifference() != null
                             && i.getDifference().compareTo(BigDecimal.ZERO) != 0)
                .forEach(item -> {
                    stockRepository.findByStoreIdAndItemId(take.getStoreId(), item.getItemId())
                            .ifPresent(stock -> {
                                stock.setQuantity(item.getCountedQty());
                                stockRepository.save(stock);
                            });

                    StockMovement m = new StockMovement();
                    m.setStoreId(take.getStoreId());
                    m.setItemId(item.getItemId());
                    m.setQuantityChange(item.getDifference());
                    m.setMovementType(StockMovement.MovementType.ADJUSTMENT);
                    m.setReferenceId(take.getId());
                    m.setReferenceType("pos_inv_stock_takes");
                    m.setNotes("Stock take adjustment");
                    movementRepository.save(m);
                });

        take.setStatus(StockTake.StockTakeStatus.COMPLETED);
        take.setCompletedAt(Instant.now());
        return stockTakeRepository.save(take);
    }

    // ========================================
    // 取消盤點 / Cancel stock take
    // ========================================
    @Transactional
    public StockTake cancel(UUID stockTakeId) {
        StockTake take = findOrThrow(stockTakeId);
        if (take.getStatus() != StockTake.StockTakeStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only cancel an in-progress stock take");
        }
        take.setStatus(StockTake.StockTakeStatus.CANCELLED);
        return stockTakeRepository.save(take);
    }

    public StockTake findById(UUID id) {
        return findOrThrow(id);
    }

    public List<StockTake> listByStore(UUID storeId) {
        return stockTakeRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId);
    }

    private StockTake findOrThrow(UUID id) {
        return stockTakeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock take not found: " + id));
    }
}
