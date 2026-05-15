/**
 * @file XReportCalculatorTest.java
 * @description X Report 計算引擎單元測試 / X Report calculator unit tests
 * @description_en Unit tests for X Report generation from shift aggregates
 * @description_zh 驗證 X Report 從班次累計資料正確產生的單元測試
 */
package com.enterprise.staff;

import com.enterprise.payment.entity.PaymentTransaction;
import com.enterprise.payment.repository.PaymentTransactionRepository;
import com.enterprise.staff.entity.StaffShift;
import com.enterprise.staff.entity.XReport;
import com.enterprise.staff.repository.StaffShiftRepository;
import com.enterprise.staff.repository.XReportRepository;
import com.enterprise.staff.service.XReportCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XReportCalculatorTest {

    @Mock private StaffShiftRepository shiftRepository;
    @Mock private XReportRepository xReportRepository;
    @Mock private PaymentTransactionRepository transactionRepository;

    @InjectMocks private XReportCalculator calculator;

    private UUID shiftId;
    private StaffShift shift;
    private UUID storeId;

    @BeforeEach
    void setUp() {
        shiftId = UUID.randomUUID();
        storeId = UUID.randomUUID();

        shift = new StaffShift();
        shift.setStoreId(storeId);
        shift.setEmployeeId(UUID.randomUUID());
        shift.setShiftNo("SH20260508001");
        shift.setOpenedAt(Instant.parse("2026-05-15T02:00:00Z"));
        shift.setOpeningCash(new BigDecimal("1000.00"));
        shift.setStatus(StaffShift.ShiftStatus.OPEN);

        // 模擬多筆銷售累計 / Simulate accumulated sales
        shift.addSale(new BigDecimal("300.00"), new BigDecimal("15.00"), new BigDecimal("0"));
        shift.addSale(new BigDecimal("200.00"), new BigDecimal("10.00"), new BigDecimal("20.00"));
        shift.addRefund(new BigDecimal("50.00"));

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        lenient().when(transactionRepository.findByStoreAndDateRange(
                eq(storeId),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of());
        when(xReportRepository.save(any(XReport.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ========================================
    // X Report 銷售金額正確 / Verify total sales
    // ========================================
    @Test
    @DisplayName("X Report 的 totalSales 應等於班次累計銷售額 500")
    void generate_totalSales_correct() {
        XReport report = calculator.generate(shiftId);
        assertThat(report.getTotalSales()).isEqualByComparingTo("500.00");
    }

    // ========================================
    // X Report 退款金額正確 / Verify total refunds
    // ========================================
    @Test
    @DisplayName("X Report 的 totalRefunds 應等於班次累計退款 50")
    void generate_totalRefunds_correct() {
        XReport report = calculator.generate(shiftId);
        assertThat(report.getTotalRefunds()).isEqualByComparingTo("50.00");
    }

    // ========================================
    // X Report 淨銷售計算正確 / Verify net sales calculation
    // ========================================
    @Test
    @DisplayName("X Report 的 netSales 應為 totalSales - totalRefunds = 450")
    void generate_netSales_correct() {
        XReport report = calculator.generate(shiftId);
        assertThat(report.getNetSales()).isEqualByComparingTo("450.00");
    }

    // ========================================
    // X Report 交易筆數正確 / Verify transaction count
    // ========================================
    @Test
    @DisplayName("X Report 的 transactionCount 應為 2")
    void generate_transactionCount_correct() {
        XReport report = calculator.generate(shiftId);
        assertThat(report.getTransactionCount()).isEqualTo(2);
    }

    // ========================================
    // X Report 支付方式明細正確 / Verify payment breakdown
    // ========================================
    @Test
    @DisplayName("X Report 應依支付交易彙總 cash/card/other 淨額")
    void generate_paymentBreakdown_aggregatesNetAmountByMethodType() {
        when(transactionRepository.findByStoreAndDateRange(
                eq(storeId),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(
                transaction("CASH", "300.00", PaymentTransaction.TxnStatus.SUCCESS),
                transaction("CASH", "50.00", PaymentTransaction.TxnStatus.REFUNDED),
                transaction("CARD", "200.00", PaymentTransaction.TxnStatus.SUCCESS),
                transaction("QR_CODE", "30.00", PaymentTransaction.TxnStatus.SUCCESS),
                transaction("CARD", "99.00", PaymentTransaction.TxnStatus.FAILED)
        ));

        XReport report = calculator.generate(shiftId);

        assertThat(report.getCashSales()).isEqualByComparingTo("250.00");
        assertThat(report.getCardSales()).isEqualByComparingTo("200.00");
        assertThat(report.getOtherSales()).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("X Report 應以班次開啟時間與產生時間查詢支付交易")
    void generate_paymentBreakdown_queriesShiftPeriod() {
        calculator.generate(shiftId);

        verify(transactionRepository).findByStoreAndDateRange(
                eq(storeId),
                eq(LocalDateTime.ofInstant(shift.getOpenedAt(), ZoneId.of("Asia/Taipei"))),
                any(LocalDateTime.class)
        );
    }

    // ========================================
    // X Report 稅額正確 / Verify total tax
    // ========================================
    @Test
    @DisplayName("X Report 的 totalTax 應等於累計稅額 25")
    void generate_totalTax_correct() {
        XReport report = calculator.generate(shiftId);
        assertThat(report.getTotalTax()).isEqualByComparingTo("25.00");
    }

    // ========================================
    // X Report 報表資料存入 DB / Verify report saved to repository
    // ========================================
    @Test
    @DisplayName("X Report 應呼叫 save 儲存至資料庫")
    void generate_savedToRepository() {
        calculator.generate(shiftId);
        ArgumentCaptor<XReport> captor = ArgumentCaptor.forClass(XReport.class);
        verify(xReportRepository).save(captor.capture());
        assertThat(captor.getValue().getShiftId()).isEqualTo(shiftId);
    }

    private PaymentTransaction transaction(
            String methodType,
            String amount,
            PaymentTransaction.TxnStatus status
    ) {
        PaymentTransaction txn = new PaymentTransaction();
        txn.setStoreId(storeId);
        txn.setOrderId(UUID.randomUUID());
        txn.setPayMethodId(UUID.randomUUID());
        txn.setMethodType(methodType);
        txn.setAmount(new BigDecimal(amount));
        txn.setStatus(status);
        return txn;
    }
}
