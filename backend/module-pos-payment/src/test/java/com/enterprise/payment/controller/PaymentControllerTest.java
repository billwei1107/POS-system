/**
 * @file PaymentControllerTest.java
 * @description 支付 Controller 測試 / Payment controller tests
 * @description_en Verifies payment transaction query API contract
 * @description_zh 驗證付款交易查詢 API 回傳契約
 */
package com.enterprise.payment.controller;

import com.enterprise.core.service.OrderService;
import com.enterprise.organization.service.StoreAccessService;
import com.enterprise.payment.dto.response.GatewayConfigResponse;
import com.enterprise.payment.entity.GatewayConfig;
import com.enterprise.payment.dto.response.PaymentTransactionResponse;
import com.enterprise.payment.entity.PaymentTransaction;
import com.enterprise.payment.service.GatewayConfigService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock private PaymentService paymentService;
    @Mock private PayMethodService payMethodService;
    @Mock private GatewayConfigService gatewayConfigService;
    @Mock private OrderService orderService;
    @Mock private StoreAccessService storeAccessService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PaymentController(
                        paymentService,
                        payMethodService,
                        gatewayConfigService,
                        orderService,
                        storeAccessService))
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
        when(orderService.findStoreId(orderId)).thenReturn(storeId);
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

        verify(storeAccessService).requireReadableStore(storeId);
    }

    @Test
    void listMethods_checksStoreReadScopeBeforeReturningPayMethods() throws Exception {
        UUID storeId = UUID.randomUUID();
        when(payMethodService.listByStore(storeId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/pos/payments/methods").param("storeId", storeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(storeAccessService).requireReadableStore(storeId);
    }

    @Test
    void listGateways_hidesSecretsAndChecksStoreReadScope() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID gatewayId = UUID.randomUUID();
        GatewayConfigResponse response = new GatewayConfigResponse(
                gatewayId,
                storeId,
                GatewayConfig.GatewayType.MOCK_CARD,
                "模擬刷卡",
                "MID-001",
                "https://gateway.example.local",
                "{\"capture\":\"manual\"}",
                true,
                true,
                true,
                true,
                LocalDateTime.of(2026, 5, 15, 3, 30),
                LocalDateTime.of(2026, 5, 15, 3, 31)
        );
        when(gatewayConfigService.listByStore(storeId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/pos/payments/gateways").param("storeId", storeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(gatewayId.toString()))
                .andExpect(jsonPath("$.data[0].gatewayType").value("MOCK_CARD"))
                .andExpect(jsonPath("$.data[0].displayName").value("模擬刷卡"))
                .andExpect(jsonPath("$.data[0].merchantId").value("MID-001"))
                .andExpect(jsonPath("$.data[0].apiKeyConfigured").value(true))
                .andExpect(jsonPath("$.data[0].apiSecretConfigured").value(true))
                .andExpect(jsonPath("$.data[0].apiKey").doesNotExist())
                .andExpect(jsonPath("$.data[0].apiSecret").doesNotExist());

        verify(storeAccessService).requireReadableStore(storeId);
    }
}
