/**
 * @file XReportCalculator.java
 * @description X Report 計算引擎 / X Report calculation engine
 * @description_en Generates X Reports (non-resetting mid-shift snapshots) from shift data
 * @description_zh 產生 X Report（不重置寄存器的班中銷售快照）
 */
package com.enterprise.staff.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.staff.entity.StaffShift;
import com.enterprise.staff.entity.XReport;
import com.enterprise.staff.repository.StaffShiftRepository;
import com.enterprise.staff.repository.XReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class XReportCalculator {

    private final StaffShiftRepository shiftRepository;
    private final XReportRepository xReportRepository;

    // ========================================
    // 產生 X Report（不重置，僅快照）/ Generate X Report (no reset)
    // ========================================
    @Transactional
    public XReport generate(UUID shiftId) {
        StaffShift shift = shiftRepository.findById(shiftId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("SHIFT_NOT_FOUND: Shift not found: " + shiftId));

        XReport report = new XReport();
        report.setShiftId(shiftId);
        report.setStoreId(shift.getStoreId());
        report.setEmployeeId(shift.getEmployeeId());
        report.setGeneratedAt(Instant.now());
        report.setPeriodStart(shift.getOpenedAt());
        report.setPeriodEnd(Instant.now());

        // ========================================
        // 從班次累計資料計算報表欄位 / Calculate from shift aggregates
        // ========================================
        report.setTotalSales(shift.getTotalSales());
        report.setTotalRefunds(shift.getTotalRefunds());
        report.setTotalDiscounts(shift.getTotalDiscounts());
        report.setTotalTax(shift.getTotalTax());
        report.setNetSales(shift.getNetSales());
        report.setTransactionCount(shift.getTransactionCount());

        // ========================================
        // 支付方式明細（Phase 2 整合 pos-payment 後補充）/ Payment breakdown (defer to Phase 2)
        // ========================================
        report.setCashSales(BigDecimal.ZERO);
        report.setCardSales(BigDecimal.ZERO);
        report.setOtherSales(BigDecimal.ZERO);

        // ========================================
        // 額外報表資料（JSON 格式）/ Extra report data (JSON)
        // ========================================
        Map<String, Object> data = new HashMap<>();
        data.put("shiftNo", shift.getShiftNo());
        data.put("openingCash", shift.getOpeningCash());
        data.put("reportType", "X_REPORT");
        report.setReportData(data);

        log.info("X Report generated for shift: {}", shift.getShiftNo());
        return xReportRepository.save(report);
    }

    // ========================================
    // 查詢班次所有 X Report / List X reports for shift
    // ========================================
    @Transactional(readOnly = true)
    public List<XReport> listByShift(UUID shiftId) {
        return xReportRepository.findByShiftId(shiftId);
    }
}
