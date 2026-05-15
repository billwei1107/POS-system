package com.enterprise.auth.dto;

import java.util.List;
import java.util.UUID;

/**
 * @file RolePermissionSummaryResponse.java
 * @description 角色權限摘要 DTO / Role permission summary DTO
 * @description_en Combines a role with its assigned permission list
 * @description_zh 將角色與已指派權限清單整合為後台管理摘要
 */
public record RolePermissionSummaryResponse(
        UUID id,
        String name,
        String code,
        String description,
        List<PermissionResponse> permissions
) {
}
