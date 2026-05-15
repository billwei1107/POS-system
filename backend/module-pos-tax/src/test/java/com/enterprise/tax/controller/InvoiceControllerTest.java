/**
 * @file InvoiceControllerTest.java
 * @description 發票 Controller 測試 / Invoice controller tests
 * @description_en Verifies invoice lookup API contract used after checkout and refunds
 * @description_zh 驗證結帳與退款後發票查詢 API 回傳契約
 */
package com.enterprise.tax.controller;

import com.enterprise.organization.service.StoreAccessService;
import com.enterprise.tax.dto.response.InvoiceResponse;
import com.enterprise.tax.entity.Invoice;
import com.enterprise.tax.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTest {

    @Mock private InvoiceService invoiceService;
    @Mock private StoreAccessService storeAccessService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new InvoiceController(invoiceService, storeAccessService))
                .build();
    }

    @Test
    void getByOrder_returnsIssuedInvoiceTotalsAndStatus() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        InvoiceResponse invoice = new InvoiceResponse(
                UUID.randomUUID(),
                storeId,
                orderId,
                null,
                Invoice.InvoiceType.B2C,
                "12345678",
                "Demo Store",
                null,
                null,
                null,
                null,
                new BigDecimal("120.00"),
                new BigDecimal("6.00"),
                new BigDecimal("126.00"),
                Invoice.InvoiceStatus.ISSUED,
                Invoice.UploadStatus.SUCCESS,
                LocalDateTime.of(2026, 5, 11, 3, 30),
                null,
                null
        );
        when(invoiceService.getByOrder(orderId)).thenReturn(Optional.of(invoice));

        mockMvc.perform(get("/api/v1/pos/invoices/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data.salesAmount").value(120.00))
                .andExpect(jsonPath("$.data.taxAmount").value(6.00))
                .andExpect(jsonPath("$.data.totalAmount").value(126.00))
                .andExpect(jsonPath("$.data.status").value("ISSUED"))
                .andExpect(jsonPath("$.data.uploadStatus").value("SUCCESS"));

        verify(storeAccessService).requireReadableStore(storeId);
    }

    @Test
    void getByOrder_returnsNotFoundWhenInvoiceIsMissing() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(invoiceService.getByOrder(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/pos/invoices/orders/{orderId}", orderId))
                .andExpect(status().isNotFound());
    }
}
