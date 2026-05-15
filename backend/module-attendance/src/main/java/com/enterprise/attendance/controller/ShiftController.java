package com.enterprise.attendance.controller;

import com.enterprise.attendance.entity.ShiftSchedule;
import com.enterprise.attendance.service.ShiftService;
import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @file ShiftController.java
 * @description 班表管理控制器 / Shift management controller
 * @description_zh 班表 CRUD 與員工指派
 */
@RestController
@RequestMapping("/api/v1/attendance/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping
    @RequirePermission("system:attendance:read")
    public ApiResponse<List<ShiftSchedule>> getAllShifts() {
        return ApiResponse.success(shiftService.getAllShifts());
    }

    @GetMapping("/{id}")
    @RequirePermission("system:attendance:read")
    public ApiResponse<ShiftSchedule> getShiftById(@PathVariable UUID id) {
        return ApiResponse.success(shiftService.getShiftById(id));
    }

    @PostMapping
    @RequirePermission("system:attendance:manage")
    @Auditable(module = "attendance-shift", action = "create")
    public ApiResponse<ShiftSchedule> createShift(@RequestBody ShiftSchedule shift) {
        return ApiResponse.success(shiftService.createShift(shift));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:attendance:manage")
    @Auditable(module = "attendance-shift", action = "update")
    public ApiResponse<ShiftSchedule> updateShift(@PathVariable UUID id, @RequestBody ShiftSchedule shift) {
        return ApiResponse.success(shiftService.updateShift(id, shift));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:attendance:manage")
    @Auditable(module = "attendance-shift", action = "delete")
    public ApiResponse<Void> deleteShift(@PathVariable UUID id) {
        shiftService.deleteShift(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{shiftId}/assign")
    @RequirePermission("system:attendance:manage")
    @Auditable(module = "attendance-shift", action = "assign")
    public ApiResponse<Void> assignShift(
            @PathVariable UUID shiftId,
            @RequestParam String employeeId,
            @RequestParam String effectiveDate,
            @RequestParam(required = false) String endDate) {
        shiftService.assignShift(employeeId, shiftId, effectiveDate, endDate);
        return ApiResponse.success(null);
    }
}
