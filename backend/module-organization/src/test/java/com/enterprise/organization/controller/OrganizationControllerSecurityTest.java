package com.enterprise.organization.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.organization.entity.Company;
import com.enterprise.organization.entity.Department;
import com.enterprise.organization.entity.Employee;
import com.enterprise.organization.entity.Position;
import com.enterprise.organization.entity.Region;
import com.enterprise.organization.entity.Store;
import com.enterprise.organization.entity.StoreEmployee;
import com.enterprise.organization.entity.Terminal;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @file OrganizationControllerSecurityTest.java
 * @description 組織控制器權限註解測試 / Organization controller permission annotation tests
 * @description_en Verifies organization management endpoints are protected by permission and audit annotations
 * @description_zh 驗證組織管理端點具備權限與稽核註解，避免正式後台敏感操作裸露
 */
class OrganizationControllerSecurityTest {

    @Test
    void companyEndpointsUseOrganizationPermissions() throws NoSuchMethodException {
        assertPermission(CompanyController.class.getDeclaredMethod("listAll"), "system:organization:read");
        assertPermission(CompanyController.class.getDeclaredMethod("getById", UUID.class), "system:organization:read");
        assertMutation(CompanyController.class.getDeclaredMethod("create", Company.class),
                "organization-company", "create");
        assertMutation(CompanyController.class.getDeclaredMethod("update", UUID.class, Company.class),
                "organization-company", "update");
        assertMutation(CompanyController.class.getDeclaredMethod("delete", UUID.class),
                "organization-company", "delete");
    }

    @Test
    void departmentEndpointsUseOrganizationPermissions() throws NoSuchMethodException {
        assertPermission(DepartmentController.class.getDeclaredMethod("listByCompany", UUID.class), "system:organization:read");
        assertPermission(DepartmentController.class.getDeclaredMethod("getDepartmentTree", UUID.class), "system:organization:read");
        assertMutation(DepartmentController.class.getDeclaredMethod("create", Department.class),
                "organization-department", "create");
        assertMutation(DepartmentController.class.getDeclaredMethod("update", UUID.class, Department.class),
                "organization-department", "update");
        assertMutation(DepartmentController.class.getDeclaredMethod("delete", UUID.class),
                "organization-department", "delete");
    }

    @Test
    void positionEndpointsUseOrganizationPermissions() throws NoSuchMethodException {
        assertPermission(PositionController.class.getDeclaredMethod("listAll"), "system:organization:read");
        assertMutation(PositionController.class.getDeclaredMethod("create", Position.class),
                "organization-position", "create");
        assertMutation(PositionController.class.getDeclaredMethod("update", UUID.class, Position.class),
                "organization-position", "update");
        assertMutation(PositionController.class.getDeclaredMethod("delete", UUID.class),
                "organization-position", "delete");
    }

    @Test
    void employeeEndpointsUseOrganizationPermissions() throws NoSuchMethodException {
        assertPermission(EmployeeController.class.getDeclaredMethod("listByCompany", UUID.class, UUID.class),
                "system:organization:read");
        assertPermission(EmployeeController.class.getDeclaredMethod("getById", UUID.class), "system:organization:read");
        assertMutation(EmployeeController.class.getDeclaredMethod("create", Employee.class),
                "organization-employee", "create");
        assertMutation(EmployeeController.class.getDeclaredMethod("update", UUID.class, Employee.class),
                "organization-employee", "update");
        assertMutation(EmployeeController.class.getDeclaredMethod("resignEmployee", UUID.class),
                "organization-employee", "resign");
        assertMutation(EmployeeController.class.getDeclaredMethod("delete", UUID.class),
                "organization-employee", "delete");
    }

    @Test
    void storeEndpointsUseOrganizationPermissions() throws NoSuchMethodException {
        assertPermission(StoreController.class.getDeclaredMethod("list", UUID.class, UUID.class),
                "system:organization:read");
        assertPermission(StoreController.class.getDeclaredMethod("getById", UUID.class),
                "system:organization:read");
        assertPermission(StoreController.class.getDeclaredMethod("getByStoreCode", String.class),
                "system:organization:read");
        assertMutation(StoreController.class.getDeclaredMethod("create", Store.class),
                "organization-store", "create");
        assertMutation(StoreController.class.getDeclaredMethod("update", UUID.class, Store.class),
                "organization-store", "update");
        assertMutation(StoreController.class.getDeclaredMethod("updateStatus", UUID.class, String.class),
                "organization-store", "update-status");
        assertMutation(StoreController.class.getDeclaredMethod("delete", UUID.class),
                "organization-store", "delete");
    }

    @Test
    void terminalEndpointsUseOrganizationPermissions() throws NoSuchMethodException {
        assertPermission(TerminalController.class.getDeclaredMethod("getById", UUID.class),
                "system:organization:read");
        assertPermission(TerminalController.class.getDeclaredMethod("getByTerminalCode", String.class),
                "system:organization:read");
        assertPermission(TerminalController.class.getDeclaredMethod("listByStore", UUID.class),
                "system:organization:read");
        assertPermission(TerminalController.class.getDeclaredMethod("findOfflineTerminals", int.class),
                "system:organization:read");
        assertPermission(TerminalController.class.getDeclaredMethod("recordHeartbeat", UUID.class, String.class, String.class),
                "system:organization:manage");
        assertMutation(TerminalController.class.getDeclaredMethod("register", Terminal.class),
                "organization-terminal", "register");
        assertMutation(TerminalController.class.getDeclaredMethod("update", UUID.class, Terminal.class),
                "organization-terminal", "update");
        assertMutation(TerminalController.class.getDeclaredMethod("updateStatus", UUID.class, String.class),
                "organization-terminal", "update-status");
        assertMutation(TerminalController.class.getDeclaredMethod("delete", UUID.class),
                "organization-terminal", "delete");
    }

    @Test
    void regionAndStoreEmployeeEndpointsUseOrganizationPermissions() throws NoSuchMethodException {
        assertPermission(RegionController.class.getDeclaredMethod("getById", UUID.class),
                "system:organization:read");
        assertPermission(RegionController.class.getDeclaredMethod("listByCompany", UUID.class),
                "system:organization:read");
        assertMutation(RegionController.class.getDeclaredMethod("create", Region.class),
                "organization-region", "create");
        assertMutation(RegionController.class.getDeclaredMethod("update", UUID.class, Region.class),
                "organization-region", "update");
        assertMutation(RegionController.class.getDeclaredMethod("delete", UUID.class),
                "organization-region", "delete");

        assertPermission(StoreEmployeeController.class.getDeclaredMethod("listByStore", UUID.class),
                "system:organization:read");
        assertPermission(StoreEmployeeController.class.getDeclaredMethod("listByEmployee", UUID.class),
                "system:organization:read");
        assertPermission(StoreEmployeeController.class.getDeclaredMethod("getPrimaryStore", UUID.class),
                "system:organization:read");
        assertMutation(StoreEmployeeController.class.getDeclaredMethod("assign", StoreEmployee.class),
                "organization-store-employee", "assign");
        assertMutation(StoreEmployeeController.class.getDeclaredMethod("unassign", UUID.class, UUID.class),
                "organization-store-employee", "unassign");
        assertMutation(StoreEmployeeController.class.getDeclaredMethod("setPrimaryStore", UUID.class, UUID.class),
                "organization-store-employee", "set-primary");
    }

    private void assertMutation(Method method, String module, String action) {
        assertPermission(method, "system:organization:manage");

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
