package com.enterprise.organization.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.dto.DepartmentTreeDTO;
import com.enterprise.organization.entity.Department;
import com.enterprise.organization.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-department", action = "create")
    public ApiResponse<Department> create(@RequestBody Department department) {
        return ApiResponse.success(departmentService.create(department));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-department", action = "update")
    public ApiResponse<Department> update(@PathVariable UUID id, @RequestBody Department department) {
        return ApiResponse.success(departmentService.update(id, department));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:organization:read")
    public ApiResponse<Department> getById(@PathVariable UUID id) {
        return ApiResponse.success(departmentService.getById(id));
    }

    @GetMapping
    @RequirePermission("system:organization:read")
    public ApiResponse<List<Department>> listByCompany(@RequestParam UUID companyId) {
        return ApiResponse.success(departmentService.listByCompany(companyId));
    }

    @GetMapping("/tree")
    @RequirePermission("system:organization:read")
    public ApiResponse<List<DepartmentTreeDTO>> getDepartmentTree(@RequestParam UUID companyId) {
        return ApiResponse.success(departmentService.getDepartmentTree(companyId));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-department", action = "delete")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        departmentService.delete(id);
        return ApiResponse.success(null);
    }
}
