package com.enterprise.organization.controller;

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
    public ApiResponse<Department> create(@RequestBody Department department) {
        return ApiResponse.success(departmentService.create(department));
    }

    @PutMapping("/{id}")
    public ApiResponse<Department> update(@PathVariable UUID id, @RequestBody Department department) {
        return ApiResponse.success(departmentService.update(id, department));
    }

    @GetMapping("/{id}")
    public ApiResponse<Department> getById(@PathVariable UUID id) {
        return ApiResponse.success(departmentService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<Department>> listByCompany(@RequestParam UUID companyId) {
        return ApiResponse.success(departmentService.listByCompany(companyId));
    }

    @GetMapping("/tree")
    public ApiResponse<List<DepartmentTreeDTO>> getDepartmentTree(@RequestParam UUID companyId) {
        return ApiResponse.success(departmentService.getDepartmentTree(companyId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        departmentService.delete(id);
        return ApiResponse.success(null);
    }
}
