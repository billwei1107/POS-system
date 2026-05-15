/**
 * @file ShiftController.java
 * @description 班次管理 Controller / Staff shift management controller
 * @description_en REST controller for opening, closing, and listing staff shifts
 * @description_zh 班次管理 REST 控制器，包含開班、關班、交接班
 */
package com.enterprise.staff.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.service.StoreAccessService;
import com.enterprise.staff.dto.request.CloseShiftRequest;
import com.enterprise.staff.dto.request.OpenShiftRequest;
import com.enterprise.staff.dto.response.ShiftResponse;
import com.enterprise.staff.entity.ShiftHandover;
import com.enterprise.staff.service.ClockService;
import com.enterprise.staff.service.StaffShiftService;
import com.enterprise.staff.entity.ClockRecord;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController("posStaffShiftController")
@RequestMapping("/api/v1/staff/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final StaffShiftService shiftService;
    private final ClockService clockService;
    private final StoreAccessService storeAccessService;

    // ========================================
    // 查詢門店開放班次 / List open shifts for store
    // ========================================
    @GetMapping
    @RequirePermission("pos:shift:read")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> listOpenShifts(@RequestParam UUID storeId) {
        storeAccessService.requireReadableStore(storeId);
        List<ShiftResponse> shifts = shiftService.listOpenShifts(storeId)
                .stream().map(ShiftResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(shifts));
    }

    // ========================================
    // 開班 / Open shift
    // ========================================
    @PostMapping("/open")
    @RequirePermission("pos:shift:operate")
    @Auditable(module = "pos-shift", action = "open")
    public ResponseEntity<ApiResponse<ShiftResponse>> openShift(
            @RequestParam UUID storeId,
            @Valid @RequestBody OpenShiftRequest request) {
        storeAccessService.requireOperableStore(storeId);
        ShiftResponse response = ShiftResponse.from(
                shiftService.openShift(storeId, request.employeeId(), request.terminalId(), request.openingCash())
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========================================
    // 一般關班 / Normal close shift
    // ========================================
    @PostMapping("/{shiftId}/close")
    @RequirePermission("pos:shift:operate")
    @Auditable(module = "pos-shift", action = "close")
    public ResponseEntity<ApiResponse<ShiftResponse>> closeShift(
            @PathVariable UUID shiftId,
            @Valid @RequestBody CloseShiftRequest request) {
        storeAccessService.requireOperableStore(shiftService.findStoreId(shiftId));
        ShiftResponse response = ShiftResponse.from(
                shiftService.closeShift(shiftId, request.closingCash(), request.notes())
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========================================
    // 盲點結算 / Blind close shift
    // ========================================
    @PostMapping("/{shiftId}/blind-close")
    @RequirePermission("pos:shift:manage")
    @Auditable(module = "pos-shift", action = "blind-close")
    public ResponseEntity<ApiResponse<ShiftResponse>> blindCloseShift(
            @PathVariable UUID shiftId,
            @RequestParam(required = false) String notes) {
        storeAccessService.requireOperableStore(shiftService.findStoreId(shiftId));
        ShiftResponse response = ShiftResponse.from(
                shiftService.blindCloseShift(shiftId, notes)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========================================
    // 建立交接班 / Create handover
    // ========================================
    @PostMapping("/{fromShiftId}/handover")
    @RequirePermission("pos:shift:manage")
    @Auditable(module = "pos-shift", action = "handover")
    public ResponseEntity<ApiResponse<UUID>> createHandover(
            @PathVariable UUID fromShiftId,
            @RequestParam(required = false) UUID toShiftId,
            @RequestParam BigDecimal cashCounted,
            @RequestParam(required = false) String notes,
            @RequestParam UUID confirmedBy) {
        storeAccessService.requireOperableStore(shiftService.findStoreId(fromShiftId));
        ShiftHandover handover = shiftService.createHandover(fromShiftId, toShiftId, cashCounted, notes, confirmedBy);
        return ResponseEntity.ok(ApiResponse.success(handover.getId()));
    }

    // ========================================
    // 打卡（休息開始/結束）/ Clock event
    // ========================================
    @PostMapping("/{shiftId}/clock")
    @RequirePermission("pos:shift:operate")
    @Auditable(module = "pos-shift", action = "clock")
    public ResponseEntity<ApiResponse<UUID>> clock(
            @PathVariable UUID shiftId,
            @RequestParam String clockType,
            @RequestParam(required = false) UUID terminalId,
            @RequestParam(required = false) String notes) {
        storeAccessService.requireOperableStore(shiftService.findStoreId(shiftId));
        ClockRecord record = clockService.clock(shiftId, ClockRecord.ClockType.valueOf(clockType), terminalId, notes);
        return ResponseEntity.ok(ApiResponse.success(record.getId()));
    }
}
