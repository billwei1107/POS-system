/**
 * @file ReconciliationControllerTest.java
 * @description 對帳 Controller 測試 / Reconciliation controller tests
 * @description_en Verifies daily reconciliation query and generation API contracts
 * @description_zh 驗證每日對帳查詢與產生 API 回傳契約
 */
package com.enterprise.payment.controller;

import com.enterprise.organization.service.StoreAccessService;
import com.enterprise.payment.entity.Reconciliation;
import com.enterprise.payment.service.ReconciliationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReconciliationControllerTest {

    @Mock private ReconciliationService reconciliationService;
    @Mock private StoreAccessService storeAccessService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReconciliationController(reconciliationService, storeAccessService))
                .build();
    }

    @Test
    void list_returnsCashReconciliationSummary() throws Exception {
        UUID storeId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 11);
        Reconciliation reconciliation = createCashReconciliation(storeId, date);
        when(reconciliationService.listByStoreAndDate(storeId, date)).thenReturn(List.of(reconciliation));

        mockMvc.perform(get("/api/v1/pos/reconciliation")
                        .param("storeId", storeId.toString())
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].methodType").value("CASH"))
                .andExpect(jsonPath("$.data[0].transactionCount").value(1))
                .andExpect(jsonPath("$.data[0].totalAmount").value(126.00))
                .andExpect(jsonPath("$.data[0].netAmount").value(126.00))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));

        verify(storeAccessService).requireReadableStore(storeId);
    }

    @Test
    void generate_returnsGeneratedDailyReconciliation() throws Exception {
        UUID storeId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 11);
        Reconciliation reconciliation = createCashReconciliation(storeId, date);
        when(reconciliationService.generateDaily(storeId, date)).thenReturn(List.of(reconciliation));

        mockMvc.perform(post("/api/v1/pos/reconciliation/generate")
                        .param("storeId", storeId.toString())
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].methodType").value("CASH"))
                .andExpect(jsonPath("$.data[0].totalAmount").value(126.00));

        verify(storeAccessService).requireOperableStore(storeId);
    }

    private Reconciliation createCashReconciliation(UUID storeId, LocalDate date) {
        Reconciliation reconciliation = new Reconciliation();
        reconciliation.setId(UUID.randomUUID());
        reconciliation.setStoreId(storeId);
        reconciliation.setReconDate(date);
        reconciliation.setPayMethodId(UUID.randomUUID());
        reconciliation.setMethodType("CASH");
        reconciliation.setTransactionCount(1);
        reconciliation.setTotalAmount(new BigDecimal("126.00"));
        reconciliation.setRefundCount(0);
        reconciliation.setRefundAmount(BigDecimal.ZERO);
        reconciliation.setNetAmount(new BigDecimal("126.00"));
        reconciliation.setStatus(Reconciliation.ReconStatus.PENDING);
        return reconciliation;
    }
}
