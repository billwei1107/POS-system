/**
 * @file StockControllerTest.java
 * @description 庫存 Controller 測試 / Stock controller tests
 * @description_en Verifies store stock lookup API contract used by inventory pages
 * @description_zh 驗證庫存頁使用的門店庫存查詢 API 回傳契約
 */
package com.enterprise.inventory.controller;

import com.enterprise.inventory.entity.StoreStock;
import com.enterprise.inventory.repository.StockMovementRepository;
import com.enterprise.inventory.repository.StoreStockRepository;
import com.enterprise.inventory.service.StockAlertService;
import com.enterprise.inventory.service.StockDeductionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    @Mock private StoreStockRepository stockRepository;
    @Mock private StockMovementRepository movementRepository;
    @Mock private StockDeductionService deductionService;
    @Mock private StockAlertService alertService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new StockController(stockRepository, movementRepository, deductionService, alertService))
                .build();
    }

    @Test
    void listStock_returnsAvailableQuantityAfterDeduction() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        StoreStock stock = createStock(storeId, itemId);
        when(stockRepository.findAllByStoreId(storeId)).thenReturn(List.of(stock));

        mockMvc.perform(get("/api/v1/inventory/stores/{storeId}/stock", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].storeId").value(storeId.toString()))
                .andExpect(jsonPath("$.data[0].itemId").value(itemId.toString()))
                .andExpect(jsonPath("$.data[0].quantity").value(98.000))
                .andExpect(jsonPath("$.data[0].reservedQuantity").value(3.000))
                .andExpect(jsonPath("$.data[0].availableQuantity").value(95.000));
    }

    @Test
    void getStock_returnsSingleStoreItemStock() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        StoreStock stock = createStock(storeId, itemId);
        when(stockRepository.findByStoreIdAndItemId(storeId, itemId)).thenReturn(Optional.of(stock));

        mockMvc.perform(get("/api/v1/inventory/stores/{storeId}/stock/{itemId}", storeId, itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.quantity").value(98.000))
                .andExpect(jsonPath("$.data.availableQuantity").value(95.000));
    }

    private StoreStock createStock(UUID storeId, UUID itemId) {
        StoreStock stock = new StoreStock();
        stock.setId(UUID.randomUUID());
        stock.setStoreId(storeId);
        stock.setItemId(itemId);
        stock.setQuantity(new BigDecimal("98.000"));
        stock.setReservedQuantity(new BigDecimal("3.000"));
        stock.setReorderPoint(new BigDecimal("10.000"));
        stock.setReorderQuantity(new BigDecimal("20.000"));
        return stock;
    }
}
