/**
 * @file LeaveCalculationServiceTest.java
 * @description 請假天數計算單元測試 / Leave calculation service unit tests
 * @description_en Tests for leave hours calculation including weekends and half-day logic
 * @description_zh 驗證請假時數計算邏輯，包含週末排除與半天假
 */
package com.enterprise.leave;

import com.enterprise.leave.entity.LeaveRequest.HalfDay;
import com.enterprise.leave.service.LeaveCalculationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LeaveCalculationServiceTest {

    private final LeaveCalculationService service = new LeaveCalculationService();

    // ========================================
    // 單日整天 / Single full day
    // ========================================
    @Test
    @DisplayName("單日整天 = 8 小時")
    void singleFullDay() {
        // 2026-05-11 是週一
        BigDecimal hours = service.calculateLeaveHours(
                LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 11), HalfDay.FULL, HalfDay.FULL);
        assertThat(hours).isEqualByComparingTo("8");
    }

    // ========================================
    // 單日半天（下午開始）/ Single half day afternoon
    // ========================================
    @Test
    @DisplayName("單日下午半天 = 4 小時")
    void singleAfternoonHalfDay() {
        BigDecimal hours = service.calculateLeaveHours(
                LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 11), HalfDay.AFTERNOON, HalfDay.FULL);
        assertThat(hours).isEqualByComparingTo("4");
    }

    // ========================================
    // 跨週末計算（週一到週五）/ Mon to Fri = 5 days
    // ========================================
    @Test
    @DisplayName("週一到週五 = 40 小時")
    void monToFriFullWeek() {
        BigDecimal hours = service.calculateLeaveHours(
                LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 15), HalfDay.FULL, HalfDay.FULL);
        assertThat(hours).isEqualByComparingTo("40");
    }

    // ========================================
    // 跨週末（週五到下週一）= 2 天 / Fri to Mon = 2 working days
    // ========================================
    @Test
    @DisplayName("週五到下週一（跨週末）= 16 小時")
    void fridayToMonday() {
        BigDecimal hours = service.calculateLeaveHours(
                LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 18), HalfDay.FULL, HalfDay.FULL);
        assertThat(hours).isEqualByComparingTo("16");
    }

    // ========================================
    // 純週末 = 0 小時 / Weekend only = 0
    // ========================================
    @Test
    @DisplayName("純週末 = 0 小時")
    void weekendOnly() {
        BigDecimal hours = service.calculateLeaveHours(
                LocalDate.of(2026, 5, 16), LocalDate.of(2026, 5, 17), HalfDay.FULL, HalfDay.FULL);
        assertThat(hours).isEqualByComparingTo("0");
    }

    // ========================================
    // 時數轉天數 / Hours to days
    // ========================================
    @Test
    @DisplayName("8 小時 = 1 天，4 小時 = 0.5 天")
    void hoursToDays() {
        assertThat(service.hoursToDays(new BigDecimal("8"))).isEqualByComparingTo("1.0");
        assertThat(service.hoursToDays(new BigDecimal("4"))).isEqualByComparingTo("0.5");
        assertThat(service.hoursToDays(new BigDecimal("40"))).isEqualByComparingTo("5.0");
    }
}
