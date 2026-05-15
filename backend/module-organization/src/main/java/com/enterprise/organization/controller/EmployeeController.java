package com.enterprise.organization.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.entity.Employee;
import com.enterprise.organization.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-employee", action = "create")
    public ApiResponse<Employee> create(@RequestBody Employee employee) {
        return ApiResponse.success(employeeService.create(employee));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-employee", action = "update")
    public ApiResponse<Employee> update(@PathVariable UUID id, @RequestBody Employee employee) {
        return ApiResponse.success(employeeService.update(id, employee));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:organization:read")
    public ApiResponse<Employee> getById(@PathVariable UUID id) {
        return ApiResponse.success(employeeService.getById(id));
    }

    @GetMapping
    @RequirePermission("system:organization:read")
    public ApiResponse<List<Employee>> listByCompany(@RequestParam(required = false) UUID companyId, 
                                                     @RequestParam(required = false) UUID departmentId) {
        if (departmentId != null) {
            return ApiResponse.success(employeeService.listByDepartment(departmentId));
        }
        if (companyId == null) {
            return ApiResponse.success(employeeService.listAll());
        }
        return ApiResponse.success(employeeService.listByCompany(companyId));
    }

    @PostMapping("/{id}/resign")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-employee", action = "resign")
    public ApiResponse<Void> resignEmployee(@PathVariable UUID id) {
        employeeService.resignEmployee(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-employee", action = "delete")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        employeeService.delete(id);
        return ApiResponse.success(null);
    }
}
