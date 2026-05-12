/**
 * @file StockTakeControllerTest.java
 * @description 盤點 Controller 測試 / Stock take controller tests
 * @description_en Verifies stock take API serialization used by inventory count pages
 * @description_zh 驗證盤點單 API 回傳序列化，避免明細父層循環造成前端載入失敗
 */
package com.enterprise.inventory.controller;

import com.enterprise.inventory.entity.StockTake;
import com.enterprise.inventory.entity.StockTakeItem;
import com.enterprise.inventory.service.StockTakeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StockTakeControllerTest {

    @Mock private StockTakeService stockTakeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new StockTakeController(stockTakeService))
                .build();
    }

    @Test
    void listByStore_returnsItemsWithoutRecursiveParentStockTake() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        StockTake take = new StockTake();
        take.setId(UUID.randomUUID());
        take.setStoreId(storeId);

        StockTakeItem item = new StockTakeItem();
        item.setId(UUID.randomUUID());
        item.setStockTake(take);
        item.setItemId(itemId);
        item.setSystemQty(new BigDecimal("12.000"));
        take.getItems().add(item);

        when(stockTakeService.listByStore(storeId)).thenReturn(List.of(take));

        mockMvc.perform(get("/api/v1/inventory/stock-takes/stores/{storeId}", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].storeId").value(storeId.toString()))
                .andExpect(jsonPath("$.data[0].items[0].itemId").value(itemId.toString()))
                .andExpect(jsonPath("$.data[0].items[0].stockTake").doesNotExist());
    }
}
