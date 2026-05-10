/**
 * @file PaymentServiceTest.java
 * @description 支付服務測試 / Payment service tests
 * @description_en Verifies OrderCompletedEvent creates canonical payment transactions
 * @description_zh 驗證訂單完成事件會建立正式支付交易記錄
 */
package com.enterprise.payment.service;

import com.enterprise.core.event.OrderCompletedEvent;
import com.enterprise.payment.entity.GatewayConfig;
import com.enterprise.payment.entity.PayMethod;
import com.enterprise.payment.entity.PaymentTransaction;
import com.enterprise.payment.gateway.PaymentGateway;
import com.enterprise.payment.gateway.dto.GatewayRequest;
import com.enterprise.payment.gateway.dto.GatewayResponse;
import com.enterprise.payment.repository.PayMethodRepository;
import com.enterprise.payment.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PayMethodRepository payMethodRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PaymentGateway cashGateway;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
            payMethodRepository,
            transactionRepository,
            eventPublisher,
            List.of(cashGateway)
        );
    }

    // ========================================
    // 訂單完成事件 / Order completed event
    // ========================================
    @Test
    void onOrderCompleted_cashPayment_savesTenderedAndChange() {
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID payMethodId = UUID.randomUUID();
        PayMethod cash = payMethod(storeId, payMethodId);

        when(transactionRepository.findByOrderId(orderId)).thenReturn(List.of());
        when(payMethodRepository.findByStoreIdAndCode(storeId, "CASH")).thenReturn(Optional.of(cash));
        when(cashGateway.gatewayType()).thenReturn(GatewayConfig.GatewayType.CASH);
        when(cashGateway.charge(any(GatewayRequest.class)))
            .thenReturn(GatewayResponse.ok("CASH-TEST", "{\"method\":\"CASH\"}"));
        when(transactionRepository.save(any(PaymentTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.onOrderCompleted(new OrderCompletedEvent(
            this,
            orderId,
            storeId,
            "ORD-001",
            null,
            new BigDecimal("126.00"),
            "CASH",
            new BigDecimal("126.00"),
            new BigDecimal("130.00"),
            new BigDecimal("4.00")
        ));

        ArgumentCaptor<PaymentTransaction> txnCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(transactionRepository).save(txnCaptor.capture());
        PaymentTransaction txn = txnCaptor.getValue();
        assertThat(txn.getOrderId()).isEqualTo(orderId);
        assertThat(txn.getStoreId()).isEqualTo(storeId);
        assertThat(txn.getPayMethodId()).isEqualTo(payMethodId);
        assertThat(txn.getMethodType()).isEqualTo("CASH");
        assertThat(txn.getAmount()).isEqualByComparingTo("126.00");
        assertThat(txn.getTendered()).isEqualByComparingTo("130.00");
        assertThat(txn.getChangeGiven()).isEqualByComparingTo("4.00");
        assertThat(txn.getStatus()).isEqualTo(PaymentTransaction.TxnStatus.SUCCESS);
        assertThat(txn.getGatewayRef()).isEqualTo("CASH-TEST");
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void onOrderCompleted_existingTransaction_doesNotCreateDuplicate() {
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        when(transactionRepository.findByOrderId(orderId)).thenReturn(List.of(new PaymentTransaction()));

        paymentService.onOrderCompleted(new OrderCompletedEvent(
            this,
            orderId,
            storeId,
            "ORD-002",
            null,
            new BigDecimal("126.00"),
            "CASH",
            new BigDecimal("126.00"),
            new BigDecimal("130.00"),
            new BigDecimal("4.00")
        ));

        verify(transactionRepository, never()).save(any(PaymentTransaction.class));
        verify(payMethodRepository, never()).findByStoreIdAndCode(any(), any());
    }

    private PayMethod payMethod(UUID storeId, UUID payMethodId) {
        PayMethod pm = new PayMethod();
        pm.setId(payMethodId);
        pm.setStoreId(storeId);
        pm.setCode("CASH");
        pm.setName("現金");
        pm.setMethodType(PayMethod.MethodType.CASH);
        pm.setChangeBack(true);
        return pm;
    }
}
