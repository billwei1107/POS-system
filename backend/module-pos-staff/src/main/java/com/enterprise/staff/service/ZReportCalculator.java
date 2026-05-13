/**
 * @file ZReportCalculator.java
 * @description Z Report 計算引擎 / Z Report calculation engine
 * @description_en Generates Z Reports (end-of-day close with SHA-256 tamper-proof hash)
 * @description_zh 產生 Z Report（日結，包含 SHA-256 防篡改 Hash）
 */
package com.enterprise.staff.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.staff.entity.StaffShift;
import com.enterprise.staff.entity.ZReport;
import com.enterprise.staff.repository.StaffShiftRepository;
import com.enterprise.staff.repository.ZReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZReportCalculator {

    private final StaffShiftRepository shiftRepository;
    private final ZReportRepository zReportRepository;

    private static final ZoneId TW_ZONE = ZoneId.of("Asia/Taipei");
    private static final DateTimeFormatter REPORT_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(TW_ZONE);

    // ========================================
    // 產生 Z Report（日結 + 防篡改 Hash）/ Generate Z Report with hash
    // ========================================
    @Transactional
    public ZReport generate(UUID storeId, LocalDate reportDate, BigDecimal cashInDrawer, UUID generatedBy) {
        if (zReportRepository.findByStoreIdAndReportDate(storeId, reportDate).isPresent()) {
            throw new BusinessException("Z_REPORT_EXISTS: Z Report already exists for date: " + reportDate);
        }

        // ========================================
        // 彙總當日所有已關閉班次 / Aggregate all closed shifts for the day
        // ========================================
        Instant dayStart = reportDate.atStartOfDay(TW_ZONE).toInstant();
        Instant dayEnd = reportDate.plusDays(1).atStartOfDay(TW_ZONE).toInstant();
        List<StaffShift> dayShifts = shiftRepository.findByStoreIdAndDateRange(storeId, dayStart, dayEnd);

        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalRefunds = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        int txCount = 0;

        for (StaffShift s : dayShifts) {
            totalSales = totalSales.add(s.getTotalSales());
            totalRefunds = totalRefunds.add(s.getTotalRefunds());
            totalDiscounts = totalDiscounts.add(s.getTotalDiscounts());
            totalTax = totalTax.add(s.getTotalTax());
            txCount += s.getTransactionCount();
        }

        BigDecimal netSales = totalSales.subtract(totalRefunds);
        BigDecimal grossSales = totalSales.add(totalTax);

        // ========================================
        // 現金差異計算 / Cash variance calculation
        // ========================================
        BigDecimal openingCashSum = dayShifts.stream()
                .map(StaffShift::getOpeningCash)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expectedCash = openingCashSum.add(netSales);
        BigDecimal cashVariance = cashInDrawer.subtract(expectedCash);

        ZReport report = new ZReport();
        report.setStoreId(storeId);
        report.setReportDate(reportDate);
        report.setReportNo("ZR" + REPORT_NO_FMT.format(Instant.now()) + storeId.toString().substring(0, 4).toUpperCase());
        report.setGeneratedAt(Instant.now());
        report.setPeriodStart(dayStart);
        report.setPeriodEnd(dayEnd);
        report.setTotalSales(totalSales);
        report.setTotalRefunds(totalRefunds);
        report.setTotalDiscounts(totalDiscounts);
        report.setTotalTax(totalTax);
        report.setNetSales(netSales);
        report.setGrossSales(grossSales);
        report.setCashSales(BigDecimal.ZERO);
        report.setCardSales(BigDecimal.ZERO);
        report.setOtherSales(BigDecimal.ZERO);
        report.setCashInDrawer(cashInDrawer);
        report.setExpectedCash(expectedCash);
        report.setCashVariance(cashVariance);
        report.setTransactionCount(txCount);
        report.setRefundCount(0);
        report.setVoidCount(0);
        report.setShiftCount(dayShifts.size());
        report.setGeneratedBy(generatedBy);
        report.setResetAt(Instant.now());

        // ========================================
        // JSON 報表資料 / Report data JSON
        // ========================================
        Map<String, Object> data = new HashMap<>();
        data.put("shiftCount", dayShifts.size());
        data.put("reportType", "Z_REPORT");
        data.put("storeId", storeId.toString());
        data.put("reportDate", reportDate.toString());
        report.setReportData(data);

        // ========================================
        // SHA-256 防篡改 Hash / SHA-256 tamper-proof hash
        // ========================================
        report.setContentHash(computeHash(report));

        log.info("Z Report generated for store {} date {}: hash={}", storeId, reportDate, report.getContentHash());
        return zReportRepository.save(report);
    }

    // ========================================
    // 查詢歷史 Z Report / List recent Z Reports
    // ========================================
    @Transactional(readOnly = true)
    public List<ZReport> listRecent(UUID storeId) {
        return zReportRepository.findRecentByStoreId(storeId);
    }

    // ========================================
    // 驗證 Hash 完整性 / Verify report hash integrity
    // ========================================
    public boolean verifyHash(ZReport report) {
        String expected = computeHash(report);
        return expected.equals(report.getContentHash());
    }

    // ========================================
    // 計算報表 Hash / Compute SHA-256 hash over key fields
    // ========================================
    String computeHash(ZReport report) {
        String content = String.join("|",
                report.getStoreId().toString(),
                report.getReportDate().toString(),
                report.getTotalSales().toPlainString(),
                report.getTotalRefunds().toPlainString(),
                report.getTotalTax().toPlainString(),
                report.getNetSales().toPlainString(),
                report.getCashInDrawer().toPlainString(),
                String.valueOf(report.getTransactionCount())
        );
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
