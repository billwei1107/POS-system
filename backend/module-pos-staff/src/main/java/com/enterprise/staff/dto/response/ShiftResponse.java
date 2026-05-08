/**
 * @file ShiftResponse.java
 * @description 班次回應 DTO / Shift response DTO
 * @description_en Response DTO for staff shift data
 * @description_zh 班次查詢回應資料傳輸物件
 */
package com.enterprise.staff.dto.response;

import com.enterprise.staff.entity.StaffShift;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ShiftResponse(
        UUID id,
        UUID storeId,
        UUID employeeId,
        UUID terminalId,
        String shiftNo,
        String status,
        Instant openedAt,
        Instant closedAt,
        BigDecimal openingCash,
        BigDecimal closingCash,
        BigDecimal expectedCash,
        BigDecimal cashVariance,
        BigDecimal totalSales,
        BigDecimal totalRefunds,
        BigDecimal totalDiscounts,
        BigDecimal totalTax,
        BigDecimal netSales,
        int transactionCount,
        String notes
) {
    public static ShiftResponse from(StaffShift shift) {
        return new ShiftResponse(
                shift.getId(),
                shift.getStoreId(),
                shift.getEmployeeId(),
                shift.getTerminalId(),
                shift.getShiftNo(),
                shift.getStatus().name(),
                shift.getOpenedAt(),
                shift.getClosedAt(),
                shift.getOpeningCash(),
                shift.getClosingCash(),
                shift.getExpectedCash(),
                shift.getCashVariance(),
                shift.getTotalSales(),
                shift.getTotalRefunds(),
                shift.getTotalDiscounts(),
                shift.getTotalTax(),
                shift.getNetSales(),
                shift.getTransactionCount(),
                shift.getNotes()
        );
    }
}
