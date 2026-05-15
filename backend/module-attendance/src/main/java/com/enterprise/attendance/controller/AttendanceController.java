package com.enterprise.attendance.controller;

import com.enterprise.attendance.dto.AttendanceRecordDTO;
import com.enterprise.attendance.dto.ClockInRequest;
import com.enterprise.attendance.dto.CorrectionRequest;
import com.enterprise.attendance.service.AttendanceService;
import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.service.EmployeeAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @file AttendanceController.java
 * @description 打卡考勤控制器 / Attendance controller
 * @description_zh 處理打卡上下班、出勤記錄查詢與補卡申請
 */
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeAccessService employeeAccessService;

    // ========================================
    // 打卡上班 / Clock In
    // ========================================
    @PostMapping("/clock-in")
    @RequirePermission("system:attendance:operate")
    @Auditable(module = "attendance-record", action = "clock-in")
    public ApiResponse<AttendanceRecordDTO> clockIn(@RequestBody ClockInRequest request) {
        if (request.getEmployeeId() == null) {
            request.setEmployeeId(employeeAccessService.requireCurrentEmployeeIdString());
        } else {
            employeeAccessService.requireOperableEmployee(request.getEmployeeId());
        }
        return ApiResponse.success(attendanceService.clockIn(request));
    }

    // ========================================
    // 打卡下班 / Clock Out
    // ========================================
    @PostMapping("/clock-out")
    @RequirePermission("system:attendance:operate")
    @Auditable(module = "attendance-record", action = "clock-out")
    public ApiResponse<AttendanceRecordDTO> clockOut() {
        String employeeId = employeeAccessService.requireCurrentEmployeeIdString();
        return ApiResponse.success(attendanceService.clockOut(employeeId));
    }

    // ========================================
    // 查詢當日記錄 / Get Today's Record
    // ========================================
    @GetMapping("/today")
    @RequirePermission("system:attendance:read")
    public ApiResponse<AttendanceRecordDTO> getTodayRecord() {
        String employeeId = employeeAccessService.requireCurrentEmployeeIdString();
        return ApiResponse.success(attendanceService.getDailyRecord(employeeId, LocalDate.now()));
    }

    // ========================================
    // 查詢月度記錄 / Get Monthly Records
    // ========================================
    @GetMapping("/records")
    @RequirePermission("system:attendance:read")
    public ApiResponse<List<AttendanceRecordDTO>> getMonthlyRecords(
            @RequestParam(required = false) String employeeId,
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "4") int month) {
        String targetId = employeeId != null ? employeeId : employeeAccessService.requireCurrentEmployeeIdString();
        employeeAccessService.requireReadableEmployee(targetId);
        return ApiResponse.success(attendanceService.getMonthlyRecords(targetId, year, month));
    }

    // ========================================
    // 補卡申請 / Submit Correction
    // ========================================
    @PostMapping("/corrections")
    @RequirePermission("system:attendance:operate")
    @Auditable(module = "attendance-correction", action = "submit")
    public ApiResponse<Void> submitCorrection(@RequestBody CorrectionRequest request) {
        if (request.getEmployeeId() == null) {
            request.setEmployeeId(employeeAccessService.requireCurrentEmployeeIdString());
        } else {
            employeeAccessService.requireOperableEmployee(request.getEmployeeId());
        }
        attendanceService.submitCorrection(request);
        return ApiResponse.success(null);
    }
}
