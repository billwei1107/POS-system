package com.enterprise.auth.controller;

import com.enterprise.auth.dto.CreateRoleRequest;
import com.enterprise.auth.dto.RolePermissionSummaryResponse;
import com.enterprise.auth.dto.UpdateRolePermissionsRequest;
import com.enterprise.auth.entity.Role;
import com.enterprise.auth.service.RoleService;
import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @RequirePermission("system:rbac:read")
    public ApiResponse<List<Role>> getAllRoles() {
        return ApiResponse.success(roleService.getAllRoles());
    }

    @GetMapping("/permission-summaries")
    @RequirePermission("system:rbac:read")
    public ApiResponse<List<RolePermissionSummaryResponse>> getRolePermissionSummaries() {
        return ApiResponse.success(roleService.getRolePermissionSummaries());
    }

    @PostMapping
    @RequirePermission("system:rbac:manage")
    @Auditable(module = "system-rbac", action = "create-role")
    public ApiResponse<RolePermissionSummaryResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.success(roleService.createRole(request));
    }

    @PutMapping("/{id}/permissions")
    @RequirePermission("system:rbac:manage")
    @Auditable(module = "system-rbac", action = "update-role-permissions")
    public ApiResponse<RolePermissionSummaryResponse> updateRolePermissions(
            @PathVariable UUID id,
            @RequestBody UpdateRolePermissionsRequest request
    ) {
        return ApiResponse.success(roleService.updateRolePermissions(id, request));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:rbac:read")
    public ApiResponse<Role> getRole(@PathVariable UUID id) {
        return ApiResponse.success(roleService.getRoleById(id));
    }
}
