/**
 * @file RbacControllerSecurityTest.java
 * @description RBAC 控制器權限註解測試 / RBAC controller permission annotation tests
 * @description_en Verifies role and permission administration endpoints declare permission and audit annotations
 * @description_zh 驗證角色與權限管理端點具備權限與稽核註解，避免後台權限資料裸露
 */
package com.enterprise.auth.controller;

import com.enterprise.auth.dto.UpdateRolePermissionsRequest;
import com.enterprise.auth.dto.CreateRoleRequest;
import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RbacControllerSecurityTest {

    @Test
    void roleEndpointsUseRbacPermissions() throws NoSuchMethodException {
        assertPermission(RoleController.class.getDeclaredMethod("getAllRoles"), "system:rbac:read");
        assertPermission(RoleController.class.getDeclaredMethod("getRolePermissionSummaries"), "system:rbac:read");
        assertPermission(RoleController.class.getDeclaredMethod("getRole", UUID.class), "system:rbac:read");
        assertMutation(RoleController.class.getDeclaredMethod("createRole", CreateRoleRequest.class),
                "system:rbac:manage", "system-rbac", "create-role");
        assertMutation(RoleController.class.getDeclaredMethod(
                        "updateRolePermissions", UUID.class, UpdateRolePermissionsRequest.class),
                "system:rbac:manage", "system-rbac", "update-role-permissions");
    }

    @Test
    void permissionEndpointsUseRbacReadPermission() throws NoSuchMethodException {
        assertPermission(PermissionController.class.getDeclaredMethod("getAllPermissions"), "system:rbac:read");
        assertPermission(PermissionController.class.getDeclaredMethod("getPermission", UUID.class),
                "system:rbac:read");
    }

    private void assertMutation(Method method, String permissionCode, String module, String action) {
        assertPermission(method, permissionCode);

        Auditable auditable = method.getAnnotation(Auditable.class);
        assertNotNull(auditable, "Mutation endpoint should be audited");
        assertEquals(module, auditable.module());
        assertEquals(action, auditable.action());
    }

    private void assertPermission(Method method, String permissionCode) {
        RequirePermission permission = method.getAnnotation(RequirePermission.class);
        assertNotNull(permission, "Endpoint should declare a permission requirement");
        assertEquals(permissionCode, permission.value());
    }
}
