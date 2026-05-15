package com.enterprise.auth.service;

import com.enterprise.auth.entity.Permission;
import com.enterprise.auth.entity.Role;
import com.enterprise.auth.entity.UserRole;
import com.enterprise.auth.repository.PermissionRepository;
import com.enterprise.auth.repository.RolePermissionRepository;
import com.enterprise.auth.repository.RoleRepository;
import com.enterprise.auth.repository.UserRoleRepository;
import com.enterprise.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @file PermissionGuardServiceTest.java
 * @description 權限守衛單元測試 / Permission guard unit tests
 * @description_en Verifies RBAC allow and deny behavior
 * @description_zh 驗證 RBAC 權限允許與拒絕行為
 */
class PermissionGuardServiceTest {

    private final UserRoleRepository userRoleRepository = mock(UserRoleRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final PermissionRepository permissionRepository = mock(PermissionRepository.class);
    private final RolePermissionRepository rolePermissionRepository = mock(RolePermissionRepository.class);
    private final PermissionGuardService guardService = new PermissionGuardService(
            userRoleRepository,
            roleRepository,
            permissionRepository,
            rolePermissionRepository
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requirePermissionAllowsAssignedRolePermission() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        Role role = role(roleId, "SHIFT_MANAGER");
        Permission permission = permission(permissionId, "pos:order:refund");
        UserRole userRole = userRole(userId, roleId);

        setAuthentication(userId);
        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of(userRole));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findByCode("pos:order:refund")).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)).thenReturn(true);

        assertDoesNotThrow(() -> guardService.requirePermission("pos:order:refund"));
    }

    @Test
    void requirePermissionRejectsMissingPermission() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        Role role = role(roleId, "CASHIER");
        Permission permission = permission(permissionId, "pos:order:refund");
        UserRole userRole = userRole(userId, roleId);

        setAuthentication(userId);
        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of(userRole));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findByCode("pos:order:refund")).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)).thenReturn(false);

        assertThrows(BusinessException.class, () -> guardService.requirePermission("pos:order:refund"));
    }

    @Test
    void requirePermissionAllowsSuperAdminWithoutPermissionLookup() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        Role role = role(roleId, "SUPER_ADMIN");
        UserRole userRole = userRole(userId, roleId);

        setAuthentication(userId);
        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of(userRole));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        assertDoesNotThrow(() -> guardService.requirePermission("pos:settings:manage"));
    }

    private void setAuthentication(UUID userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of())
        );
    }

    private Role role(UUID id, String code) {
        Role role = new Role();
        role.setId(id);
        role.setCode(code);
        role.setName(code);
        return role;
    }

    private Permission permission(UUID id, String code) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(code);
        permission.setName(code);
        permission.setType("POS");
        permission.setResource("order");
        permission.setAction("refund");
        return permission;
    }

    private UserRole userRole(UUID userId, UUID roleId) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return userRole;
    }
}
