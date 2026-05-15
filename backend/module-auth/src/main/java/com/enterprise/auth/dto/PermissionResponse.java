package com.enterprise.auth.dto;

import com.enterprise.auth.entity.Permission;

import java.util.UUID;

/**
 * @file PermissionResponse.java
 * @description 權限回應 DTO / Permission response DTO
 * @description_en Serializes permission metadata for RBAC management screens
 * @description_zh 將權限資料轉為 RBAC 管理頁使用的回應格式
 */
public record PermissionResponse(
        UUID id,
        String name,
        String code,
        String type,
        String resource,
        String action
) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getCode(),
                permission.getType(),
                permission.getResource(),
                permission.getAction()
        );
    }
}
