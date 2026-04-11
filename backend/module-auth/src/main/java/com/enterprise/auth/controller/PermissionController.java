package com.enterprise.auth.controller;

import com.enterprise.auth.entity.Permission;
import com.enterprise.auth.service.PermissionService;
import com.enterprise.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ApiResponse<List<Permission>> getAllPermissions() {
        return ApiResponse.success(permissionService.getAllPermissions());
    }

    @GetMapping("/{id}")
    public ApiResponse<Permission> getPermission(@PathVariable UUID id) {
        return ApiResponse.success(permissionService.getPermissionById(id));
    }
}
