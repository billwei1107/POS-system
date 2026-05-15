package com.enterprise.auth.service;

import com.enterprise.auth.entity.Permission;
import com.enterprise.auth.entity.Role;
import com.enterprise.auth.entity.UserRole;
import com.enterprise.auth.repository.PermissionRepository;
import com.enterprise.auth.repository.RolePermissionRepository;
import com.enterprise.auth.repository.RoleRepository;
import com.enterprise.auth.repository.UserRoleRepository;
import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @file PermissionGuardService.java
 * @description RBAC 權限守衛服務 / RBAC permission guard service
 * @description_en Validates the current authenticated user's permission codes
 * @description_zh 驗證目前登入使用者是否擁有指定權限代碼
 */
@Service
@RequiredArgsConstructor
public class PermissionGuardService {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional(readOnly = true)
    public void requirePermission(String permissionCode) {
        UUID userId = resolveCurrentUserId();
        Set<Role> roles = resolveCurrentRoles(userId);

        if (roles.stream().anyMatch(role -> SUPER_ADMIN.equals(role.getCode()))) {
            return;
        }

        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new BusinessException(403, "Permission is not configured: " + permissionCode));

        boolean allowed = roles.stream()
                .anyMatch(role -> rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId()));
        if (!allowed) {
            throw new BusinessException(403, "Permission denied: " + permissionCode);
        }
    }

    private UUID resolveCurrentUserId() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            throw new BusinessException(401, "Authentication is required");
        }
        return UUID.fromString(currentUserId);
    }

    private Set<Role> resolveCurrentRoles(UUID userId) {
        Set<Role> roles = new HashSet<>();
        for (UserRole userRole : userRoleRepository.findAllByUserId(userId)) {
            roleRepository.findById(userRole.getRoleId()).ifPresent(roles::add);
        }

        String jwtRole = SecurityUtils.getCurrentRole();
        if (StringUtils.hasText(jwtRole)) {
            roleRepository.findByCode(jwtRole).ifPresent(roles::add);
        }

        return roles;
    }
}
