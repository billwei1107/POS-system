/**
 * @file InvoiceServiceTest.java
 * @description 電子發票服務測試 / E-invoice service tests
 * @description_en Verifies order-completed invoice creation keeps order amount and tax totals consistent
 * @description_zh 驗證訂單完成後自動開立發票時，發票金額與訂單稅額保持一致
 */
package com.enterprise.tax.service;

import com.enterprise.core.event.OrderCompletedEvent;
import com.enterprise.tax.entity.Invoice;
import com.enterprise.tax.event.InvoiceIssuedEvent;
import com.enterprise.tax.invoice.TurnkeyClient;
import com.enterprise.tax.repository.InvoiceAllowanceRepository;
import com.enterprise.tax.repository.InvoiceRepository;
import com.enterprise.tax.repository.InvoiceTrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceAllowanceRepository allowanceRepository;

    @Mock
    private InvoiceTrackRepository trackRepository;

    @Mock
    private InvoiceTrackService trackService;

    @Mock
    private TurnkeyClient turnkeyClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService(
                invoiceRepository,
                allowanceRepository,
                trackRepository,
                trackService,
                turnkeyClient,
                eventPublisher
        );
        ReflectionTestUtils.setField(invoiceService, "defaultSellerId", "12345678");
        ReflectionTestUtils.setField(invoiceService, "defaultSellerName", "企業商店");
    }

    @Test
    void onOrderCompletedCreatesInvoiceWithOrderTaxAmount() {
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        when(invoiceRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(trackRepository.findAvailableTrackForUpdate(storeId)).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(turnkeyClient.uploadInvoice(any(Invoice.class)))
                .thenReturn(TurnkeyClient.TurnkeyResult.ok(null, "mock-upload-success"));

        invoiceService.onOrderCompleted(new OrderCompletedEvent(
                this,
                orderId,
                storeId,
                "ORD-TEST-001",
                null,
                new BigDecimal("113.00"),
                new BigDecimal("5.40"),
                "CASH",
                new BigDecimal("113.00"),
                new BigDecimal("130.00"),
                new BigDecimal("17.00")
        ));

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository, times(2)).save(invoiceCaptor.capture());
        Invoice savedInvoice = invoiceCaptor.getAllValues().get(1);

        assertThat(savedInvoice.getStoreId()).isEqualTo(storeId);
        assertThat(savedInvoice.getOrderId()).isEqualTo(orderId);
        assertThat(savedInvoice.getSalesAmount()).isEqualByComparingTo("107.60");
        assertThat(savedInvoice.getTaxAmount()).isEqualByComparingTo("5.40");
        assertThat(savedInvoice.getTotalAmount()).isEqualByComparingTo("113.00");
        assertThat(savedInvoice.getUploadStatus()).isEqualTo(Invoice.UploadStatus.SUCCESS);
        verify(eventPublisher).publishEvent(any(InvoiceIssuedEvent.class));
    }

    @Test
    void onOrderCompletedSkipsExistingInvoice() {
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        Invoice existing = new Invoice();
        existing.setOrderId(orderId);
        existing.setStoreId(storeId);
        when(invoiceRepository.findByOrderId(orderId)).thenReturn(Optional.of(existing));

        invoiceService.onOrderCompleted(new OrderCompletedEvent(
                this,
                orderId,
                storeId,
                "ORD-TEST-002",
                null,
                new BigDecimal("126.00"),
                new BigDecimal("6.00"),
                "CASH",
                new BigDecimal("126.00"),
                new BigDecimal("130.00"),
                new BigDecimal("4.00")
        ));

        verify(invoiceRepository, never()).save(any(Invoice.class));
        verify(turnkeyClient, never()).uploadInvoice(any(Invoice.class));
    }
}
