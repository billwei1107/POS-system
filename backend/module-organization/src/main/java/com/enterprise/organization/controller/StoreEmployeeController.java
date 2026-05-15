package com.enterprise.organization.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.entity.StoreEmployee;
import com.enterprise.organization.service.StoreEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/store-employees")
@RequiredArgsConstructor
public class StoreEmployeeController {

    private final StoreEmployeeService storeEmployeeService;

    @PostMapping
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-store-employee", action = "assign")
    public ApiResponse<StoreEmployee> assign(@RequestBody StoreEmployee assignment) {
        return ApiResponse.success(storeEmployeeService.assign(assignment));
    }

    @DeleteMapping
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-store-employee", action = "unassign")
    public ApiResponse<Void> unassign(@RequestParam UUID storeId, @RequestParam UUID employeeId) {
        storeEmployeeService.unassign(storeId, employeeId);
        return ApiResponse.success(null);
    }

    @GetMapping("/store/{storeId}")
    @RequirePermission("system:organization:read")
    public ApiResponse<List<StoreEmployee>> listByStore(@PathVariable UUID storeId) {
        return ApiResponse.success(storeEmployeeService.listByStore(storeId));
    }

    @GetMapping("/employee/{employeeId}")
    @RequirePermission("system:organization:read")
    public ApiResponse<List<StoreEmployee>> listByEmployee(@PathVariable UUID employeeId) {
        return ApiResponse.success(storeEmployeeService.listByEmployee(employeeId));
    }

    @GetMapping("/employee/{employeeId}/primary")
    @RequirePermission("system:organization:read")
    public ApiResponse<StoreEmployee> getPrimaryStore(@PathVariable UUID employeeId) {
        return ApiResponse.success(storeEmployeeService.getPrimaryStore(employeeId));
    }

    @PutMapping("/employee/{employeeId}/primary")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-store-employee", action = "set-primary")
    public ApiResponse<Void> setPrimaryStore(@PathVariable UUID employeeId, @RequestParam UUID storeId) {
        storeEmployeeService.setPrimaryStore(employeeId, storeId);
        return ApiResponse.success(null);
    }
}
