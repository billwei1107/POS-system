package com.enterprise.auth.controller;

import com.enterprise.auth.dto.CreateUserRequest;
import com.enterprise.auth.dto.ResetUserPasswordRequest;
import com.enterprise.auth.dto.UpdateUserRolesRequest;
import com.enterprise.auth.dto.UpdateUserStatusRequest;
import com.enterprise.auth.dto.UserRoleSummaryResponse;
import com.enterprise.auth.service.UserService;
import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @RequirePermission("system:user:manage")
    @Auditable(module = "system-user", action = "create-user")
    public ApiResponse<UserRoleSummaryResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(userService.createManagedUser(request));
    }

    @GetMapping
    @RequirePermission("system:user:read")
    public ApiResponse<PageResponse<UserRoleSummaryResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(userService.searchUserRoleSummaries(
                keyword,
                status,
                roleId,
                PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by("username").ascending())
        ));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user:read")
    public ApiResponse<UserRoleSummaryResponse> getUser(@PathVariable UUID id) {
        return ApiResponse.success(userService.getUserRoleSummary(id));
    }

    @PutMapping("/{id}/roles")
    @RequirePermission("system:user:manage")
    @Auditable(module = "system-user", action = "update-user-roles")
    public ApiResponse<UserRoleSummaryResponse> updateUserRoles(
            @PathVariable UUID id,
            @RequestBody UpdateUserRolesRequest request
    ) {
        return ApiResponse.success(userService.updateUserRoles(id, request));
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:user:manage")
    @Auditable(module = "system-user", action = "update-user-status")
    public ApiResponse<UserRoleSummaryResponse> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ApiResponse.success(userService.updateUserStatus(id, request));
    }

    @PutMapping("/{id}/password")
    @RequirePermission("system:user:manage")
    @Auditable(module = "system-user", action = "reset-user-password")
    public ApiResponse<UserRoleSummaryResponse> resetPassword(
            @PathVariable UUID id,
            @Valid @RequestBody ResetUserPasswordRequest request
    ) {
        return ApiResponse.success(userService.resetPassword(id, request));
    }
}
