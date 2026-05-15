package com.enterprise.auth.dto;

import java.util.List;

/**
 * @file UserRoleSummaryResponse.java
 * @description 使用者角色摘要 DTO / User role summary DTO
 * @description_en Combines safe user metadata with assigned roles for admin review
 * @description_zh 整合使用者安全資訊與已指派角色，供後台管理檢視
 */
public record UserRoleSummaryResponse(
        UserResponse user,
        List<RoleResponse> roles
) {
}
