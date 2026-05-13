/**
 * @file LeaveTypeController.java
 * @description 假別類型 API 控制器 / Leave type REST controller
 * @description_en REST endpoints for leave type CRUD management
 * @description_zh 假別類型的 RESTful API 端點
 */
package com.enterprise.leave.controller;

import com.enterprise.common.dto.ApiResponse;
import com.enterprise.leave.entity.LeaveType;
import com.enterprise.leave.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leave/types")
@RequiredArgsConstructor
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @GetMapping
    public ApiResponse<List<LeaveType>> list() {
        return ApiResponse.success(leaveTypeService.listAll());
    }

    @PostMapping
    public ApiResponse<LeaveType> create(@RequestBody LeaveType type) {
        return ApiResponse.success(leaveTypeService.create(type));
    }

    @PutMapping("/{id}")
    public ApiResponse<LeaveType> update(@PathVariable UUID id, @RequestBody LeaveType patch) {
        return ApiResponse.success(leaveTypeService.update(id, patch));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        leaveTypeService.delete(id);
        return ApiResponse.success(null);
    }
}
