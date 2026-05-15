/**
 * @file LeaveRequestController.java
 * @description 請假申請 API 控制器 / Leave request REST controller
 * @description_en REST endpoints for submitting, cancelling, and querying leave requests
 * @description_zh 請假申請的提交、銷假、查詢 API 端點
 */
package com.enterprise.leave.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.leave.dto.LeaveRequestDTO;
import com.enterprise.leave.entity.LeaveRequest;
import com.enterprise.leave.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leave/requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    // ========================================
    // 提交請假 / Submit leave request
    // ========================================
    @PostMapping
    @RequirePermission("system:leave:request")
    @Auditable(module = "leave-request", action = "submit")
    public ApiResponse<LeaveRequest> submit(@Valid @RequestBody LeaveRequestDTO dto) {
        return ApiResponse.success(leaveRequestService.submitRequest(dto));
    }

    // ========================================
    // 查詢員工請假記錄 / List requests by employee
    // ========================================
    @GetMapping
    @RequirePermission("system:leave:read")
    public ApiResponse<List<LeaveRequest>> list(@RequestParam UUID employeeId) {
        return ApiResponse.success(leaveRequestService.listByEmployee(employeeId));
    }

    // ========================================
    // 銷假 / Cancel request
    // ========================================
    @PostMapping("/{id}/cancel")
    @RequirePermission("system:leave:request")
    @Auditable(module = "leave-request", action = "cancel")
    public ApiResponse<LeaveRequest> cancel(@PathVariable UUID id) {
        return ApiResponse.success(leaveRequestService.cancelRequest(id));
    }

    // ========================================
    // 部門請假日曆 / Department leave calendar
    // ========================================
    @GetMapping("/calendar")
    @RequirePermission("system:leave:read")
    public ApiResponse<List<LeaveRequest>> calendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(leaveRequestService.listCalendar(startDate, endDate));
    }
}
