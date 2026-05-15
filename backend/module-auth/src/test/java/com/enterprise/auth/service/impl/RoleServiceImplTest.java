package com.enterprise.auth.service.impl;

import com.enterprise.auth.dto.RolePermissionSummaryResponse;
import com.enterprise.auth.dto.UpdateRolePermissionsRequest;
import com.enterprise.auth.entity.Permission;
import com.enterprise.auth.entity.Role;
import com.enterprise.auth.entity.RolePermission;
import com.enterprise.auth.repository.PermissionRepository;
import com.enterprise.auth.repository.RolePermissionRepository;
import com.enterprise.auth.repository.RoleRepository;
import com.enterprise.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @file RoleServiceImplTest.java
 * @description 角色服務單元測試 / Role service unit tests
 * @description_en Verifies RBAC role permission assignment updates
 * @description_zh 驗證 RBAC 角色權限指派更新流程
 */
class RoleServiceImplTest {

    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final PermissionRepository permissionRepository = mock(PermissionRepository.class);
    private final RolePermissionRepository rolePermissionRepository = mock(RolePermissionRepository.class);
    private final RoleServiceImpl roleService = new RoleServiceImpl(
            roleRepository,
            permissionRepository,
            rolePermissionRepository
    );

    @Test
    void updateRolePermissionsReplacesAssignmentsAndReturnsSummary() {
        UUID roleId = UUID.randomUUID();
        UUID refundPermissionId = UUID.randomUUID();
        UUID stockTakePermissionId = UUID.randomUUID();
        Role role = role(roleId, "SHIFT_MANAGER");
        Permission refund = permission(refundPermissionId, "pos:order:refund");
        Permission stockTake = permission(stockTakePermissionId, "pos:inventory:stock-take");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(any()))
                .thenReturn(List.of(refund, stockTake));
        when(permissionRepository.findAll()).thenReturn(List.of(refund, stockTake));
        when(rolePermissionRepository.findAllByRoleId(roleId))
                .thenReturn(List.of(rolePermission(roleId, refundPermissionId), rolePermission(roleId, stockTakePermissionId)));

        RolePermissionSummaryResponse summary = roleService.updateRolePermissions(
                roleId,
                new UpdateRolePermissionsRequest(List.of(refundPermissionId, stockTakePermissionId))
        );

        verify(rolePermissionRepository).deleteAllByRoleId(roleId);
        ArgumentCaptor<RolePermission> captor = ArgumentCaptor.forClass(RolePermission.class);
        verify(rolePermissionRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(2, summary.permissions().size());
        assertEquals("SHIFT_MANAGER", summary.code());
        assertEquals("pos:inventory:stock-take", summary.permissions().get(0).code());
        assertEquals("pos:order:refund", summary.permissions().get(1).code());
    }

    @Test
    void updateRolePermissionsRejectsMissingPermissionIds() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        Role role = role(roleId, "SHIFT_MANAGER");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(any())).thenReturn(List.of());

        assertThrows(BusinessException.class, () -> roleService.updateRolePermissions(
                roleId,
                new UpdateRolePermissionsRequest(List.of(permissionId))
        ));
        verify(rolePermissionRepository, never()).deleteAllByRoleId(any());
    }

    @Test
    void updateRolePermissionsRejectsSuperAdminPolicyRole() {
        UUID roleId = UUID.randomUUID();
        Role role = role(roleId, "SUPER_ADMIN");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        assertThrows(BusinessException.class, () -> roleService.updateRolePermissions(
                roleId,
                new UpdateRolePermissionsRequest(List.of(UUID.randomUUID()))
        ));
        verify(rolePermissionRepository, never()).deleteAllByRoleId(any());
    }

    private Role role(UUID id, String code) {
        Role role = new Role();
        role.setId(id);
        role.setCode(code);
        role.setName(code);
        role.setDescription(code + " description");
        return role;
    }

    private Permission permission(UUID id, String code) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(code);
        permission.setName(code);
        permission.setType(code.startsWith("system") ? "SYSTEM" : "POS");
        permission.setResource("resource");
        permission.setAction("action");
        return permission;
    }

    private RolePermission rolePermission(UUID roleId, UUID permissionId) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        return rolePermission;
    }
}
