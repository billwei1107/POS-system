package com.enterprise.auth.dto;

import java.util.List;
import java.util.UUID;

/**
 * @file UpdateRolePermissionsRequest.java
 * @description 更新角色權限請求 DTO / Update role permissions request DTO
 * @description_en Carries the complete permission id set that should be assigned to a role
 * @description_zh 承載角色應保留的完整權限 ID 清單
 */
public record UpdateRolePermissionsRequest(
        List<UUID> permissionIds
) {
}
