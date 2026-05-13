/**
 * @file CashDrawerServiceTest.java
 * @description 現金抽屜服務測試 / Cash drawer service tests
 * @description_en Verifies open drawer sales are recorded from cash payment events
 * @description_zh 驗證現金付款事件會彙總到開啟中的現金抽屜
 */
package com.enterprise.payment.service;

import com.enterprise.payment.entity.CashDrawer;
import com.enterprise.payment.entity.CashDrawerEvent;
import com.enterprise.payment.event.PaymentProcessedEvent;
import com.enterprise.payment.repository.CashDrawerEventRepository;
import com.enterprise.payment.repository.CashDrawerRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashDrawerServiceTest {

    @Mock private CashDrawerRepository cashDrawerRepository;
    @Mock private CashDrawerEventRepository eventRepository;

    @InjectMocks private CashDrawerService cashDrawerService;

    @Test
    void onPaymentProcessed_cashPayment_recordsSaleEventOnOpenDrawer() {
        UUID drawerId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID terminalId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        CashDrawer drawer = new CashDrawer();
        drawer.setId(drawerId);
        drawer.setStoreId(storeId);
        drawer.setTerminalId(terminalId);
        drawer.setOpenedBy(employeeId);

        when(cashDrawerRepository.findByTerminalIdAndStatus(terminalId, CashDrawer.DrawerStatus.OPEN))
                .thenReturn(Optional.of(drawer));

        cashDrawerService.onPaymentProcessed(new PaymentProcessedEvent(
                this,
                transactionId,
                orderId,
                storeId,
                terminalId,
                employeeId,
                "ORD-CASH-001",
                "CASH",
                new BigDecimal("126.00")));

        ArgumentCaptor<CashDrawerEvent> eventCaptor = ArgumentCaptor.forClass(CashDrawerEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        CashDrawerEvent event = eventCaptor.getValue();
        assertThat(event.getDrawerId()).isEqualTo(drawerId);
        assertThat(event.getEventType()).isEqualTo(CashDrawerEvent.EventType.SALE);
        assertThat(event.getAmount()).isEqualByComparingTo("126.00");
        assertThat(event.getEmployeeId()).isEqualTo(employeeId);
        assertThat(event.getOrderId()).isEqualTo(orderId);
    }

    @Test
    void onPaymentProcessed_nonCashPayment_doesNotRecordDrawerEvent() {
        cashDrawerService.onPaymentProcessed(new PaymentProcessedEvent(
                this,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD-CARD-001",
                "CARD",
                new BigDecimal("126.00")));

        verify(cashDrawerRepository, never()).findByTerminalIdAndStatus(any(), any());
        verify(eventRepository, never()).save(any(CashDrawerEvent.class));
    }
}
