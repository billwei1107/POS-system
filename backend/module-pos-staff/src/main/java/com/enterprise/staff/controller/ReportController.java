/**
 * @file ReportController.java
 * @description 報表 Controller / X/Z Report controller
 * @description_en REST controller for generating and querying X and Z Reports
 * @description_zh X/Z Report REST 控制器
 */
package com.enterprise.staff.controller;

import com.enterprise.common.dto.ApiResponse;
import com.enterprise.staff.dto.request.GenerateZReportRequest;
import com.enterprise.staff.dto.response.ZReportResponse;
import com.enterprise.staff.entity.XReport;
import com.enterprise.staff.entity.ZReport;
import com.enterprise.staff.service.XReportCalculator;
import com.enterprise.staff.service.ZReportCalculator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff/reports")
@RequiredArgsConstructor
public class ReportController {

    private final XReportCalculator xReportCalculator;
    private final ZReportCalculator zReportCalculator;

    // ========================================
    // 產生 X Report / Generate X Report
    // ========================================
    @PostMapping("/x/{shiftId}")
    public ResponseEntity<ApiResponse<XReport>> generateXReport(@PathVariable UUID shiftId) {
        XReport report = xReportCalculator.generate(shiftId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    // ========================================
    // 查詢班次所有 X Report / List X Reports for shift
    // ========================================
    @GetMapping("/x")
    public ResponseEntity<ApiResponse<List<XReport>>> listXReports(@RequestParam UUID shiftId) {
        return ResponseEntity.ok(ApiResponse.success(xReportCalculator.listByShift(shiftId)));
    }

    // ========================================
    // 產生 Z Report（日結）/ Generate Z Report (daily close)
    // ========================================
    @PostMapping("/z/{storeId}")
    public ResponseEntity<ApiResponse<ZReportResponse>> generateZReport(
            @PathVariable UUID storeId,
            @Valid @RequestBody GenerateZReportRequest request) {
        ZReport report = zReportCalculator.generate(storeId, request.reportDate(), request.cashInDrawer(), request.generatedBy());
        boolean valid = zReportCalculator.verifyHash(report);
        return ResponseEntity.ok(ApiResponse.success(ZReportResponse.from(report, valid)));
    }

    // ========================================
    // 查詢歷史 Z Report / List recent Z Reports
    // ========================================
    @GetMapping("/z/{storeId}")
    public ResponseEntity<ApiResponse<List<ZReportResponse>>> listZReports(@PathVariable UUID storeId) {
        List<ZReportResponse> list = zReportCalculator.listRecent(storeId).stream()
                .map(r -> ZReportResponse.from(r, zReportCalculator.verifyHash(r)))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
