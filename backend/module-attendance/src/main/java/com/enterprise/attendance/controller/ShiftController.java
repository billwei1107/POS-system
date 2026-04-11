package com.enterprise.attendance.controller;

import com.enterprise.attendance.entity.ShiftSchedule;
import com.enterprise.attendance.service.ShiftService;
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
    public ApiResponse<List<ShiftSchedule>> getAllShifts() {
        return ApiResponse.success(shiftService.getAllShifts());
    }

    @GetMapping("/{id}")
    public ApiResponse<ShiftSchedule> getShiftById(@PathVariable UUID id) {
        return ApiResponse.success(shiftService.getShiftById(id));
    }

    @PostMapping
    public ApiResponse<ShiftSchedule> createShift(@RequestBody ShiftSchedule shift) {
        return ApiResponse.success(shiftService.createShift(shift));
    }

    @PutMapping("/{id}")
    public ApiResponse<ShiftSchedule> updateShift(@PathVariable UUID id, @RequestBody ShiftSchedule shift) {
        return ApiResponse.success(shiftService.updateShift(id, shift));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteShift(@PathVariable UUID id) {
        shiftService.deleteShift(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{shiftId}/assign")
    public ApiResponse<Void> assignShift(
            @PathVariable UUID shiftId,
            @RequestParam String employeeId,
            @RequestParam String effectiveDate,
            @RequestParam(required = false) String endDate) {
        shiftService.assignShift(employeeId, shiftId, effectiveDate, endDate);
        return ApiResponse.success(null);
    }
}
