package com.enterprise.auth.dto;

import com.enterprise.auth.entity.Role;

import java.util.UUID;

/**
 * @file RoleResponse.java
 * @description 角色回應 DTO / Role response DTO
 * @description_en Exposes safe role fields for admin management APIs
 * @description_zh 提供後台管理 API 可安全回傳的角色欄位
 */
public record RoleResponse(
        UUID id,
        String name,
        String code,
        String description
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getCode(),
                role.getDescription()
        );
    }
}
