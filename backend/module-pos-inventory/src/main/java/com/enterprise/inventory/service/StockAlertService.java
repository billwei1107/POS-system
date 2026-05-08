/**
 * @file StockAlertService.java
 * @description 庫存警示服務 / Stock alert service
 * @description_en Generates and manages LOW_STOCK / OUT_OF_STOCK alerts based on reorder point
 * @description_zh 依補貨點自動產生低庫存與缺貨警示，並提供確認功能
 */
package com.enterprise.inventory.service;

import com.enterprise.inventory.entity.StockAlert;
import com.enterprise.inventory.entity.StoreStock;
import com.enterprise.inventory.repository.StockAlertRepository;
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
public class StockAlertService {

    private final StockAlertRepository alertRepository;

    // ========================================
    // 扣庫存後自動檢查並產生警示 / Auto-check after stock deduction
    // ========================================
    @Transactional
    public void checkAndRaiseAlert(StoreStock stock) {
        BigDecimal qty = stock.getQuantity();
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            raiseAlert(stock.getStoreId(), stock.getItemId(), StockAlert.AlertType.OUT_OF_STOCK,
                       qty, BigDecimal.ZERO);
        } else if (stock.getReorderPoint().compareTo(BigDecimal.ZERO) > 0
                   && qty.compareTo(stock.getReorderPoint()) <= 0) {
            raiseAlert(stock.getStoreId(), stock.getItemId(), StockAlert.AlertType.LOW_STOCK,
                       qty, stock.getReorderPoint());
        }
    }

    // ========================================
    // 確認警示 / Acknowledge alert
    // ========================================
    @Transactional
    public StockAlert acknowledge(UUID alertId, UUID acknowledgedBy) {
        StockAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(acknowledgedBy);
        alert.setAcknowledgedAt(Instant.now());
        return alertRepository.save(alert);
    }

    public List<StockAlert> listUnacknowledged(UUID storeId) {
        return alertRepository.findAllByStoreIdAndAcknowledgedFalseOrderByCreatedAtDesc(storeId);
    }

    private void raiseAlert(UUID storeId, UUID itemId, StockAlert.AlertType type,
                             BigDecimal currentQty, BigDecimal threshold) {
        boolean exists = alertRepository
                .findByStoreIdAndItemIdAndAlertTypeAndAcknowledgedFalse(storeId, itemId, type)
                .isPresent();
        if (exists) return;

        StockAlert alert = new StockAlert();
        alert.setStoreId(storeId);
        alert.setItemId(itemId);
        alert.setAlertType(type);
        alert.setCurrentQty(currentQty);
        alert.setThresholdQty(threshold);
        alertRepository.save(alert);
        log.info("Stock alert raised: type={}, storeId={}, itemId={}, qty={}", type, storeId, itemId, currentQty);
    }
}
