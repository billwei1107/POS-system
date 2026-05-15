package com.enterprise.auth.dto;

import java.util.List;
import java.util.UUID;

/**
 * @file UpdateUserRolesRequest.java
 * @description 更新使用者角色請求 DTO / Update user roles request DTO
 * @description_en Carries the complete role id set that should be assigned to a user
 * @description_zh 承載使用者應保留的完整角色 ID 清單
 */
public record UpdateUserRolesRequest(
        List<UUID> roleIds
) {
}
