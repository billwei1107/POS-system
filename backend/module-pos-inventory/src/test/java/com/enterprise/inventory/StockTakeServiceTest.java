/**
 * @file StockTakeServiceTest.java
 * @description 盤點服務測試 / Stock take service tests
 * @description_en Verifies stock take completion applies counted quantities back to store stock
 * @description_zh 驗證盤點完成時會將實盤數量同步回門店庫存
 */
package com.enterprise.inventory;

import com.enterprise.inventory.entity.StockMovement;
import com.enterprise.inventory.entity.StockTake;
import com.enterprise.inventory.entity.StockTakeItem;
import com.enterprise.inventory.entity.StoreStock;
import com.enterprise.inventory.repository.StockMovementRepository;
import com.enterprise.inventory.repository.StockTakeRepository;
import com.enterprise.inventory.repository.StoreStockRepository;
import com.enterprise.inventory.service.StockTakeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockTakeServiceTest {

    @Mock private StockTakeRepository stockTakeRepository;
    @Mock private StoreStockRepository stockRepository;
    @Mock private StockMovementRepository movementRepository;

    private StockTakeService stockTakeService;

    @BeforeEach
    void setUp() {
        stockTakeService = new StockTakeService(stockTakeRepository, stockRepository, movementRepository);
    }

    @Test
    void complete_appliesCountedQuantityToStoreStockAndRecordsAdjustmentMovement() {
        UUID storeId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID stockTakeId = UUID.randomUUID();

        StockTake take = new StockTake();
        take.setId(stockTakeId);
        take.setStoreId(storeId);
        take.setStatus(StockTake.StockTakeStatus.IN_PROGRESS);

        StockTakeItem item = new StockTakeItem();
        item.setId(UUID.randomUUID());
        item.setStockTake(take);
        item.setItemId(itemId);
        item.setSystemQty(new BigDecimal("10.000"));
        item.submitCount(new BigDecimal("7.000"));
        take.getItems().add(item);

        StoreStock stock = new StoreStock();
        stock.setId(UUID.randomUUID());
        stock.setStoreId(storeId);
        stock.setItemId(itemId);
        stock.setQuantity(new BigDecimal("10.000"));

        when(stockTakeRepository.findById(stockTakeId)).thenReturn(Optional.of(take));
        when(stockRepository.findByStoreIdAndItemId(storeId, itemId)).thenReturn(Optional.of(stock));
        when(stockTakeRepository.save(any(StockTake.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockTake completed = stockTakeService.complete(stockTakeId);

        assertThat(completed.getStatus()).isEqualTo(StockTake.StockTakeStatus.COMPLETED);
        assertThat(completed.getCompletedAt()).isNotNull();
        assertThat(stock.getQuantity()).isEqualByComparingTo("7.000");

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movementRepository).save(movementCaptor.capture());
        StockMovement movement = movementCaptor.getValue();
        assertThat(movement.getStoreId()).isEqualTo(storeId);
        assertThat(movement.getItemId()).isEqualTo(itemId);
        assertThat(movement.getQuantityChange()).isEqualByComparingTo("-3.000");
        assertThat(movement.getMovementType()).isEqualTo(StockMovement.MovementType.ADJUSTMENT);
        assertThat(movement.getReferenceId()).isEqualTo(stockTakeId);
    }
}
