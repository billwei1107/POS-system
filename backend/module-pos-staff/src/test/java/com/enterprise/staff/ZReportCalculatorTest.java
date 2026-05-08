/**
 * @file ZReportCalculatorTest.java
 * @description Z Report 計算引擎單元測試 / Z Report calculator unit tests
 * @description_en Unit tests for Z Report generation including hash computation and integrity verification
 * @description_zh 驗證 Z Report 產生、Hash 計算與完整性驗證的單元測試
 */
package com.enterprise.staff;

import com.enterprise.staff.entity.StaffShift;
import com.enterprise.staff.entity.ZReport;
import com.enterprise.staff.repository.StaffShiftRepository;
import com.enterprise.staff.repository.ZReportRepository;
import com.enterprise.staff.service.ZReportCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZReportCalculatorTest {

    @Mock private StaffShiftRepository shiftRepository;
    @Mock private ZReportRepository zReportRepository;

    @InjectMocks private ZReportCalculator calculator;

    private UUID storeId;
    private UUID generatedBy;
    private LocalDate reportDate;
    private StaffShift shift1;
    private StaffShift shift2;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        generatedBy = UUID.randomUUID();
        reportDate = LocalDate.of(2026, 5, 8);

        shift1 = new StaffShift();
        shift1.setStoreId(storeId);
        shift1.setEmployeeId(UUID.randomUUID());
        shift1.setShiftNo("SH001");
        shift1.setOpenedAt(Instant.now());
        shift1.setOpeningCash(new BigDecimal("1000.00"));
        shift1.setStatus(StaffShift.ShiftStatus.CLOSED);
        shift1.addSale(new BigDecimal("3000.00"), new BigDecimal("150.00"), new BigDecimal("100.00"));

        shift2 = new StaffShift();
        shift2.setStoreId(storeId);
        shift2.setEmployeeId(UUID.randomUUID());
        shift2.setShiftNo("SH002");
        shift2.setOpenedAt(Instant.now());
        shift2.setOpeningCash(new BigDecimal("500.00"));
        shift2.setStatus(StaffShift.ShiftStatus.CLOSED);
        shift2.addSale(new BigDecimal("2000.00"), new BigDecimal("100.00"), new BigDecimal("0"));
        shift2.addRefund(new BigDecimal("200.00"));

        // ========================================
        // 使用 lenient 以避免 duplicate 測試觸發 UnnecessaryStubbingException
        // Use lenient to avoid UnnecessaryStubbingException in duplicate test
        // ========================================
        lenient().when(zReportRepository.findByStoreIdAndReportDate(storeId, reportDate)).thenReturn(Optional.empty());
        lenient().when(shiftRepository.findByStoreIdAndDateRange(eq(storeId), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(shift1, shift2));
        lenient().when(zReportRepository.save(any(ZReport.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ========================================
    // Z Report 總銷售彙總正確 / Verify total sales aggregation
    // ========================================
    @Test
    @DisplayName("Z Report totalSales 應彙總所有班次銷售 = 5000")
    void generate_totalSales_aggregated() {
        ZReport report = calculator.generate(storeId, reportDate, new BigDecimal("4300.00"), generatedBy);
        assertThat(report.getTotalSales()).isEqualByComparingTo("5000.00");
    }

    // ========================================
    // Z Report 退款彙總正確 / Verify total refunds
    // ========================================
    @Test
    @DisplayName("Z Report totalRefunds 應彙總所有班次退款 = 200")
    void generate_totalRefunds_aggregated() {
        ZReport report = calculator.generate(storeId, reportDate, new BigDecimal("4300.00"), generatedBy);
        assertThat(report.getTotalRefunds()).isEqualByComparingTo("200.00");
    }

    // ========================================
    // Z Report 淨銷售計算正確 / Verify net sales
    // ========================================
    @Test
    @DisplayName("Z Report netSales = totalSales - totalRefunds = 4800")
    void generate_netSales_correct() {
        ZReport report = calculator.generate(storeId, reportDate, new BigDecimal("4300.00"), generatedBy);
        assertThat(report.getNetSales()).isEqualByComparingTo("4800.00");
    }

    // ========================================
    // Z Report 現金差異計算正確 / Verify cash variance
    // ========================================
    @Test
    @DisplayName("Z Report cashVariance = cashInDrawer - expectedCash")
    void generate_cashVariance_correct() {
        // expectedCash = openingCash(1000+500) + netSales(4800) = 6300
        // cashInDrawer = 6000 → variance = -300
        ZReport report = calculator.generate(storeId, reportDate, new BigDecimal("6000.00"), generatedBy);
        assertThat(report.getExpectedCash()).isEqualByComparingTo("6300.00");
        assertThat(report.getCashVariance()).isEqualByComparingTo("-300.00");
    }

    // ========================================
    // Z Report 班次數量正確 / Verify shift count
    // ========================================
    @Test
    @DisplayName("Z Report shiftCount 應等於當日班次數 = 2")
    void generate_shiftCount_correct() {
        ZReport report = calculator.generate(storeId, reportDate, new BigDecimal("4300.00"), generatedBy);
        assertThat(report.getShiftCount()).isEqualTo(2);
    }

    // ========================================
    // Z Report Hash 計算並通過驗證 / Hash computed and verifiable
    // ========================================
    @Test
    @DisplayName("Z Report contentHash 不為空且 verifyHash 回傳 true")
    void generate_hash_isValidAndVerifiable() {
        ZReport report = calculator.generate(storeId, reportDate, new BigDecimal("4300.00"), generatedBy);
        assertThat(report.getContentHash()).isNotBlank();
        assertThat(calculator.verifyHash(report)).isTrue();
    }

    // ========================================
    // Hash 篡改後驗證失敗 / Tampered hash fails verification
    // ========================================
    @Test
    @DisplayName("篡改 contentHash 後 verifyHash 應回傳 false")
    void verifyHash_tamperedHash_returnsFalse() {
        ZReport report = calculator.generate(storeId, reportDate, new BigDecimal("4300.00"), generatedBy);
        report.setContentHash("tampered000000000000000000000000000000000000000000000000000000000");
        assertThat(calculator.verifyHash(report)).isFalse();
    }

    // ========================================
    // 同日重複產生 Z Report 應拋例外 / Duplicate Z Report throws exception
    // ========================================
    @Test
    @DisplayName("同一門店同一日期已有 Z Report，重複產生應拋例外")
    void generate_duplicate_throwsException() {
        when(zReportRepository.findByStoreIdAndReportDate(storeId, reportDate))
                .thenReturn(Optional.of(new ZReport()));
        assertThatThrownBy(() -> calculator.generate(storeId, reportDate, new BigDecimal("4300.00"), generatedBy))
                .hasMessageContaining("Z_REPORT_EXISTS");
    }
}
