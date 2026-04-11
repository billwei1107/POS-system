package com.enterprise.organization.controller;

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
    public ApiResponse<StoreEmployee> assign(@RequestBody StoreEmployee assignment) {
        return ApiResponse.success(storeEmployeeService.assign(assignment));
    }

    @DeleteMapping
    public ApiResponse<Void> unassign(@RequestParam UUID storeId, @RequestParam UUID employeeId) {
        storeEmployeeService.unassign(storeId, employeeId);
        return ApiResponse.success(null);
    }

    @GetMapping("/store/{storeId}")
    public ApiResponse<List<StoreEmployee>> listByStore(@PathVariable UUID storeId) {
        return ApiResponse.success(storeEmployeeService.listByStore(storeId));
    }

    @GetMapping("/employee/{employeeId}")
    public ApiResponse<List<StoreEmployee>> listByEmployee(@PathVariable UUID employeeId) {
        return ApiResponse.success(storeEmployeeService.listByEmployee(employeeId));
    }

    @GetMapping("/employee/{employeeId}/primary")
    public ApiResponse<StoreEmployee> getPrimaryStore(@PathVariable UUID employeeId) {
        return ApiResponse.success(storeEmployeeService.getPrimaryStore(employeeId));
    }

    @PutMapping("/employee/{employeeId}/primary")
    public ApiResponse<Void> setPrimaryStore(@PathVariable UUID employeeId, @RequestParam UUID storeId) {
        storeEmployeeService.setPrimaryStore(employeeId, storeId);
        return ApiResponse.success(null);
    }
}
