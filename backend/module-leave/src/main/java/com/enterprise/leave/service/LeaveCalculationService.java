/**
 * @file LeaveCalculationService.java
 * @description 請假天數計算服務 / Leave days calculation service
 * @description_en Calculates actual leave hours excluding weekends, supports half-day logic
 * @description_zh 計算實際請假時數，排除週末，支援半天假邏輯
 */
package com.enterprise.leave.service;

import org.springframework.stereotype.Service;
import com.enterprise.leave.entity.LeaveRequest.HalfDay;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.DayOfWeek;

@Service
public class LeaveCalculationService {

    private static final BigDecimal HOURS_PER_DAY  = new BigDecimal("8");
    private static final BigDecimal HOURS_HALF_DAY = new BigDecimal("4");

    // ========================================
    // 計算請假時數（排除週末）/ Calculate leave hours excluding weekends
    // ========================================
    public BigDecimal calculateLeaveHours(LocalDate startDate, LocalDate endDate, HalfDay startHalf, HalfDay endHalf) {
        if (startDate.isAfter(endDate)) {
            return BigDecimal.ZERO;
        }

        if (startDate.isEqual(endDate)) {
            if (!isWeekend(startDate)) {
                return resolveHoursForSingleDay(startHalf, endHalf);
            }
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;

        // ========================================
        // 首日時數 / First day hours
        // ========================================
        if (!isWeekend(startDate)) {
            total = total.add(startHalf == HalfDay.AFTERNOON ? HOURS_HALF_DAY : HOURS_PER_DAY);
        }

        // ========================================
        // 中間整日 / Middle full days
        // ========================================
        LocalDate current = startDate.plusDays(1);
        while (current.isBefore(endDate)) {
            if (!isWeekend(current)) {
                total = total.add(HOURS_PER_DAY);
            }
            current = current.plusDays(1);
        }

        // ========================================
        // 末日時數 / Last day hours
        // ========================================
        if (!isWeekend(endDate)) {
            total = total.add(endHalf == HalfDay.MORNING ? HOURS_HALF_DAY : HOURS_PER_DAY);
        }

        return total;
    }

    // ========================================
    // 時數轉換為天數 / Convert hours to days
    // ========================================
    public BigDecimal hoursToDays(BigDecimal hours) {
        return hours.divide(HOURS_PER_DAY, 1, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal resolveHoursForSingleDay(HalfDay startHalf, HalfDay endHalf) {
        if (startHalf == HalfDay.AFTERNOON || endHalf == HalfDay.MORNING) {
            return HOURS_HALF_DAY;
        }
        if (startHalf == HalfDay.MORNING && endHalf == HalfDay.AFTERNOON) {
            return HOURS_HALF_DAY;
        }
        return HOURS_PER_DAY;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }
}
