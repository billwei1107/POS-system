/**
 * @file PaymentControllerTest.java
 * @description 支付 Controller 測試 / Payment controller tests
 * @description_en Verifies payment transaction query API contract
 * @description_zh 驗證付款交易查詢 API 回傳契約
 */
package com.enterprise.payment.controller;

import com.enterprise.payment.dto.response.PaymentTransactionResponse;
import com.enterprise.payment.entity.PaymentTransaction;
import com.enterprise.payment.service.PayMethodService;
import com.enterprise.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock private PaymentService paymentService;
    @Mock private PayMethodService payMethodService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PaymentController(paymentService, payMethodService))
                .build();
    }

    @Test
    void getByOrder_returnsCashTransactionFieldsNeededByOrderListAndReconciliation() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID payMethodId = UUID.randomUUID();
        PaymentTransactionResponse response = new PaymentTransactionResponse(
                transactionId,
                orderId,
                storeId,
                payMethodId,
                "CASH",
                new BigDecimal("126.00"),
                new BigDecimal("130.00"),
                new BigDecimal("4.00"),
                PaymentTransaction.TxnStatus.SUCCESS,
                "ORDER-001",
                LocalDateTime.of(2026, 5, 11, 3, 30)
        );
        when(paymentService.getByOrder(orderId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/pos/payments/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(transactionId.toString()))
                .andExpect(jsonPath("$.data[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data[0].methodType").value("CASH"))
                .andExpect(jsonPath("$.data[0].amount").value(126.00))
                .andExpect(jsonPath("$.data[0].tendered").value(130.00))
                .andExpect(jsonPath("$.data[0].changeGiven").value(4.00))
                .andExpect(jsonPath("$.data[0].status").value("SUCCESS"));
    }
}
