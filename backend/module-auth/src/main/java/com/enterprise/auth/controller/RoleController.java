package com.enterprise.auth.controller;

import com.enterprise.auth.entity.Role;
import com.enterprise.auth.service.RoleService;
import com.enterprise.common.dto.ApiResponse;
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
    public ApiResponse<List<Role>> getAllRoles() {
        return ApiResponse.success(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public ApiResponse<Role> getRole(@PathVariable UUID id) {
        return ApiResponse.success(roleService.getRoleById(id));
    }
}
