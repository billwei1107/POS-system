/**
 * @file ZReportResponse.java
 * @description Z Report 回應 DTO / Z Report response DTO
 * @description_en Response DTO for Z Report data including hash verification status
 * @description_zh Z Report 查詢回應，含 Hash 驗證狀態
 */
package com.enterprise.staff.dto.response;

import com.enterprise.staff.entity.ZReport;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ZReportResponse(
        UUID id,
        UUID storeId,
        LocalDate reportDate,
        String reportNo,
        Instant generatedAt,
        BigDecimal totalSales,
        BigDecimal totalRefunds,
        BigDecimal totalDiscounts,
        BigDecimal totalTax,
        BigDecimal netSales,
        BigDecimal grossSales,
        BigDecimal cashInDrawer,
        BigDecimal expectedCash,
        BigDecimal cashVariance,
        int transactionCount,
        int shiftCount,
        String contentHash,
        boolean hashValid
) {
    public static ZReportResponse from(ZReport report, boolean hashValid) {
        return new ZReportResponse(
                report.getId(),
                report.getStoreId(),
                report.getReportDate(),
                report.getReportNo(),
                report.getGeneratedAt(),
                report.getTotalSales(),
                report.getTotalRefunds(),
                report.getTotalDiscounts(),
                report.getTotalTax(),
                report.getNetSales(),
                report.getGrossSales(),
                report.getCashInDrawer(),
                report.getExpectedCash(),
                report.getCashVariance(),
                report.getTransactionCount(),
                report.getShiftCount(),
                report.getContentHash(),
                hashValid
        );
    }
}
