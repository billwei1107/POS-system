package com.enterprise.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * @file CreateRoleRequest.java
 * @description 建立角色請求 DTO / Create role request DTO
 * @description_en Carries role metadata and optional initial permission assignments
 * @description_zh 承載角色基本資料與選填的初始權限指派
 */
public record CreateRoleRequest(
        @NotBlank(message = "Role name cannot be blank")
        @Size(max = 50, message = "Role name must be 50 characters or fewer")
        String name,
        @NotBlank(message = "Role code cannot be blank")
        @Size(max = 50, message = "Role code must be 50 characters or fewer")
        @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "Role code must use uppercase letters, numbers, and underscores")
        String code,
        @Size(max = 255, message = "Role description must be 255 characters or fewer")
        String description,
        List<UUID> permissionIds
) {
}
