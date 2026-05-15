/**
 * @file PriceRuleControllerTest.java
 * @description 價格規則 Controller 測試 / Price rule controller tests
 * @description_en Verifies store-aware price rule API contract and data-scope guard calls
 * @description_zh 驗證價格規則 API 契約與門店資料範圍守門呼叫
 */
package com.enterprise.product.controller;

import com.enterprise.organization.service.StoreAccessService;
import com.enterprise.product.dto.PriceRuleRequest;
import com.enterprise.product.dto.PriceRuleResponse;
import com.enterprise.product.entity.PriceRule;
import com.enterprise.product.service.PriceRuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PriceRuleControllerTest {

    @Mock private PriceRuleService priceRuleService;
    @Mock private StoreAccessService storeAccessService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PriceRuleController(priceRuleService, storeAccessService))
                .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void list_checksStoreReadScopeWhenStoreIdIsProvided() throws Exception {
        UUID itemId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        PriceRuleResponse response = response(itemId, storeId);
        when(priceRuleService.listByItem(itemId, storeId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/pos/price-rules")
                        .param("itemId", itemId.toString())
                        .param("storeId", storeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].itemId").value(itemId.toString()))
                .andExpect(jsonPath("$.data[0].storeId").value(storeId.toString()))
                .andExpect(jsonPath("$.data[0].priceType").value("MEMBER"))
                .andExpect(jsonPath("$.data[0].price").value(88.00));

        verify(storeAccessService).requireReadableStore(storeId);
    }

    @Test
    void create_checksStoreOperationScopeForStorePriceRule() throws Exception {
        UUID itemId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        PriceRuleRequest request = new PriceRuleRequest(
                itemId,
                storeId,
                PriceRule.PriceType.MEMBER,
                new BigDecimal("88.00"),
                1,
                LocalDateTime.of(2026, 5, 14, 9, 0),
                null,
                true
        );
        when(priceRuleService.create(request)).thenReturn(response(itemId, storeId));

        mockMvc.perform(post("/api/v1/pos/price-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.priceType").value("MEMBER"));

        verify(storeAccessService).requireOperableStore(storeId);
    }

    private PriceRuleResponse response(UUID itemId, UUID storeId) {
        return new PriceRuleResponse(
                UUID.randomUUID(),
                itemId,
                storeId,
                PriceRule.PriceType.MEMBER,
                new BigDecimal("88.00"),
                1,
                LocalDateTime.of(2026, 5, 14, 9, 0),
                null,
                true
        );
    }
}
