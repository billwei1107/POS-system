/**
 * @file StockDeductionServiceTest.java
 * @description 庫存扣減服務單元測試 / Stock deduction service unit tests
 * @description_en Unit tests verifying concurrent-safe stock deduction logic and edge cases
 * @description_zh 驗證庫存扣減邏輯的單元測試，涵蓋正常扣減、回補、不足庫存攔截等場景
 */
package com.enterprise.inventory;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.inventory.entity.StockMovement;
import com.enterprise.inventory.entity.StoreStock;
import com.enterprise.inventory.repository.StockMovementRepository;
import com.enterprise.inventory.repository.StoreStockRepository;
import com.enterprise.inventory.service.StockAlertService;
import com.enterprise.inventory.service.StockDeductionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockDeductionServiceTest {

    @Mock private StoreStockRepository stockRepository;
    @Mock private StockMovementRepository movementRepository;
    @Mock private StockAlertService alertService;

    @InjectMocks private StockDeductionService service;

    private UUID storeId;
    private UUID itemId;
    private UUID orderId;
    private StoreStock existingStock;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        existingStock = new StoreStock();
        existingStock.setStoreId(storeId);
        existingStock.setItemId(itemId);
        existingStock.setQuantity(new BigDecimal("100.000"));
        existingStock.setReservedQuantity(BigDecimal.ZERO);
        existingStock.setReorderPoint(new BigDecimal("10.000"));

        when(stockRepository.findByStoreIdAndItemIdForUpdate(storeId, itemId))
                .thenReturn(Optional.of(existingStock));
    }

    // ========================================
    // 正常扣減測試 / Normal deduction test
    // ========================================
    @Test
    @DisplayName("售出 5 件後庫存應從 100 減為 95")
    void deductForSale_normalCase_reducesQuantity() {
        service.deductForSale(storeId, itemId, new BigDecimal("5"), orderId);
        assertThat(existingStock.getQuantity()).isEqualByComparingTo("95");
    }

    // ========================================
    // 扣減後應記錄 SALE 類型異動 / Movement record test
    // ========================================
    @Test
    @DisplayName("售出後應記錄 SALE 異動，quantity_change 為負數")
    void deductForSale_createsNegativeMovement() {
        service.deductForSale(storeId, itemId, new BigDecimal("3"), orderId);
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movementRepository).save(captor.capture());
        StockMovement m = captor.getValue();
        assertThat(m.getMovementType()).isEqualTo(StockMovement.MovementType.SALE);
        assertThat(m.getQuantityChange()).isEqualByComparingTo("-3");
        assertThat(m.getReferenceId()).isEqualTo(orderId);
    }

    // ========================================
    // 退款回補測試 / Return stock on refund
    // ========================================
    @Test
    @DisplayName("退款後庫存應從 100 增加為 105")
    void returnForRefund_addsQuantityBack() {
        UUID refundId = UUID.randomUUID();
        service.returnForRefund(storeId, itemId, new BigDecimal("5"), refundId);
        assertThat(existingStock.getQuantity()).isEqualByComparingTo("105");
    }

    // ========================================
    // 退款後應記錄 RETURN 類型異動 / Return movement record test
    // ========================================
    @Test
    @DisplayName("退款後應記錄 RETURN 異動，quantity_change 為正數")
    void returnForRefund_createsPositiveMovement() {
        UUID refundId = UUID.randomUUID();
        service.returnForRefund(storeId, itemId, new BigDecimal("2"), refundId);
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movementRepository).save(captor.capture());
        StockMovement m = captor.getValue();
        assertThat(m.getMovementType()).isEqualTo(StockMovement.MovementType.RETURN);
        assertThat(m.getQuantityChange()).isEqualByComparingTo("2");
    }

    @Test
    @DisplayName("庫存不足時應阻止付款完成，不得扣成負數")
    void deductForSale_insufficientStock_throwsBusinessException() {
        assertThatThrownBy(() -> service.deductForSale(storeId, itemId, new BigDecimal("200"), orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("庫存不足");

        assertThat(existingStock.getQuantity()).isEqualByComparingTo("100");
        verify(stockRepository, never()).save(existingStock);
        verify(movementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("銷售扣庫找不到庫存記錄時應阻止付款完成")
    void deductForSale_noExistingRecord_throwsBusinessException() {
        lenient().when(stockRepository.findByStoreIdAndItemIdForUpdate(storeId, itemId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deductForSale(storeId, itemId, new BigDecimal("1"), orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("尚未建立庫存");

        verify(stockRepository, never()).save(any(StoreStock.class));
        verify(movementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("可用庫存不足時應以 quantity - reserved_quantity 攔截")
    void deductForSale_availableQuantityInsufficient_throwsBusinessException() {
        existingStock.setReservedQuantity(new BigDecimal("98"));

        assertThatThrownBy(() -> service.deductForSale(storeId, itemId, new BigDecimal("3"), orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("庫存不足");

        assertThat(existingStock.getQuantity()).isEqualByComparingTo("100");
        verify(movementRepository, never()).save(any(StockMovement.class));
    }

    // ========================================
    // 手動調整庫存 / Manual adjustment test
    // ========================================
    @Test
    @DisplayName("手動調整 +10 後庫存從 100 增加為 110")
    void adjust_positiveAdjustment_increasesQuantity() {
        UUID operatedBy = UUID.randomUUID();
        service.adjust(storeId, itemId, new BigDecimal("10"), operatedBy, "盤點調整");
        assertThat(existingStock.getQuantity()).isEqualByComparingTo("110");
    }

    // ========================================
    // 手動調整後應呼叫警示檢查 / Alert check after adjustment
    // ========================================
    @Test
    @DisplayName("手動調整後應觸發庫存警示檢查")
    void adjust_triggersAlertCheck() {
        service.adjust(storeId, itemId, new BigDecimal("-95"), UUID.randomUUID(), "損耗");
        verify(alertService).checkAndRaiseAlert(existingStock);
    }

    // ========================================
    // 扣減後低於補貨點應觸發警示 / Alert on low stock after deduction
    // ========================================
    @Test
    @DisplayName("扣減後庫存低於補貨點應觸發警示檢查")
    void deductForSale_belowReorderPoint_triggersAlert() {
        service.deductForSale(storeId, itemId, new BigDecimal("95"), orderId);
        verify(alertService).checkAndRaiseAlert(existingStock);
    }
}
