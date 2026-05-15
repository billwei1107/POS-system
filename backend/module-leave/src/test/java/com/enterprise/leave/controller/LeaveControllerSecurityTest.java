/**
 * @file LeaveControllerSecurityTest.java
 * @description 請假控制器權限註解測試 / Leave controller permission annotation tests
 * @description_en Verifies leave request, balance, and type endpoints are protected by permission and audit annotations
 * @description_zh 驗證請假申請、餘假與假別端點具備權限與稽核註解，避免人事資料裸露
 */
package com.enterprise.leave.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.leave.dto.LeaveRequestDTO;
import com.enterprise.leave.entity.LeaveType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LeaveControllerSecurityTest {

    @Test
    void leaveRequestEndpointsUseLeavePermissions() throws NoSuchMethodException {
        assertMutation(LeaveRequestController.class.getDeclaredMethod("submit", LeaveRequestDTO.class),
                "system:leave:request", "leave-request", "submit");
        assertPermission(LeaveRequestController.class.getDeclaredMethod("list", UUID.class),
                "system:leave:read");
        assertMutation(LeaveRequestController.class.getDeclaredMethod("cancel", UUID.class),
                "system:leave:request", "leave-request", "cancel");
        assertPermission(LeaveRequestController.class.getDeclaredMethod("calendar", LocalDate.class, LocalDate.class),
                "system:leave:read");
    }

    @Test
    void leaveTypeEndpointsUseLeavePermissions() throws NoSuchMethodException {
        assertPermission(LeaveTypeController.class.getDeclaredMethod("list"),
                "system:leave:read");
        assertMutation(LeaveTypeController.class.getDeclaredMethod("create", LeaveType.class),
                "system:leave:manage", "leave-type", "create");
        assertMutation(LeaveTypeController.class.getDeclaredMethod("update", UUID.class, LeaveType.class),
                "system:leave:manage", "leave-type", "update");
        assertMutation(LeaveTypeController.class.getDeclaredMethod("delete", UUID.class),
                "system:leave:manage", "leave-type", "delete");
    }

    @Test
    void leaveBalanceEndpointUsesLeaveReadPermission() throws NoSuchMethodException {
        assertPermission(LeaveBalanceController.class.getDeclaredMethod("list", UUID.class, Integer.class),
                "system:leave:read");
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
