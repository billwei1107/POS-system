/**
 * @file ReconciliationServiceTest.java
 * @description 對帳服務測試 / Reconciliation service tests
 * @description_en Verifies reconciliation generation preserves confirmed records
 * @description_zh 驗證對帳產生流程不會覆蓋已確認紀錄
 */
package com.enterprise.payment.service;

import com.enterprise.payment.entity.PayMethod;
import com.enterprise.payment.entity.Reconciliation;
import com.enterprise.payment.repository.PayMethodRepository;
import com.enterprise.payment.repository.PaymentTransactionRepository;
import com.enterprise.payment.repository.ReconciliationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock private ReconciliationRepository reconciliationRepository;
    @Mock private PaymentTransactionRepository transactionRepository;
    @Mock private PayMethodRepository payMethodRepository;

    private ReconciliationService reconciliationService;

    @BeforeEach
    void setUp() {
        reconciliationService = new ReconciliationService(
            reconciliationRepository,
            transactionRepository,
            payMethodRepository
        );
    }

    @Test
    void generateDaily_existingMatchedRecord_doesNotResetToPending() {
        UUID storeId = UUID.randomUUID();
        UUID payMethodId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 11);
        PayMethod cash = new PayMethod();
        cash.setId(payMethodId);
        cash.setStoreId(storeId);
        cash.setCode("CASH");
        cash.setMethodType(PayMethod.MethodType.CASH);

        Reconciliation matched = new Reconciliation();
        matched.setStoreId(storeId);
        matched.setReconDate(date);
        matched.setPayMethodId(payMethodId);
        matched.setMethodType("CASH");
        matched.setNetAmount(new BigDecimal("126.00"));
        matched.setGatewayAmount(new BigDecimal("126.00"));
        matched.setVariance(BigDecimal.ZERO);
        matched.setStatus(Reconciliation.ReconStatus.MATCHED);

        when(payMethodRepository.findByStoreIdAndIsActiveTrueOrderBySortOrder(storeId))
            .thenReturn(List.of(cash));
        when(reconciliationRepository.findByStoreIdAndReconDateAndPayMethodId(storeId, date, payMethodId))
            .thenReturn(Optional.of(matched));

        List<Reconciliation> result = reconciliationService.generateDaily(storeId, date);

        assertThat(result).containsExactly(matched);
        assertThat(result.get(0).getStatus()).isEqualTo(Reconciliation.ReconStatus.MATCHED);
        assertThat(result.get(0).getGatewayAmount()).isEqualByComparingTo("126.00");
        verify(transactionRepository, never()).findByStoreAndDateRange(any(), any(), any());
        verify(reconciliationRepository, never()).save(any());
    }
}
