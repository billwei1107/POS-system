/**
 * @file StaffControllerSecurityTest.java
 * @description 班次與報表控制器權限註解測試 / Staff controller permission annotation tests
 * @description_en Verifies POS shift and report endpoints are protected by permission and audit annotations
 * @description_zh 驗證 POS 班次與報表端點具備權限與稽核註解，避免營運資料裸露或異動無紀錄
 */
package com.enterprise.staff.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.staff.dto.request.CloseShiftRequest;
import com.enterprise.staff.dto.request.GenerateZReportRequest;
import com.enterprise.staff.dto.request.OpenShiftRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StaffControllerSecurityTest {

    @Test
    void shiftEndpointsUseStaffPermissions() throws NoSuchMethodException {
        assertPermission(ShiftController.class.getDeclaredMethod("listOpenShifts", UUID.class),
                "pos:shift:read");
        assertMutation(ShiftController.class.getDeclaredMethod("openShift", UUID.class, OpenShiftRequest.class),
                "pos:shift:operate", "pos-shift", "open");
        assertMutation(ShiftController.class.getDeclaredMethod("closeShift", UUID.class, CloseShiftRequest.class),
                "pos:shift:operate", "pos-shift", "close");
        assertMutation(ShiftController.class.getDeclaredMethod("blindCloseShift", UUID.class, String.class),
                "pos:shift:manage", "pos-shift", "blind-close");
        assertMutation(ShiftController.class.getDeclaredMethod("createHandover",
                        UUID.class, UUID.class, BigDecimal.class, String.class, UUID.class),
                "pos:shift:manage", "pos-shift", "handover");
        assertMutation(ShiftController.class.getDeclaredMethod("clock", UUID.class, String.class, UUID.class, String.class),
                "pos:shift:operate", "pos-shift", "clock");
    }

    @Test
    void reportEndpointsUseReportPermissions() throws NoSuchMethodException {
        assertMutation(ReportController.class.getDeclaredMethod("generateXReport", UUID.class),
                "pos:report:view", "pos-report-x", "generate");
        assertPermission(ReportController.class.getDeclaredMethod("listXReports", UUID.class),
                "pos:report:view");
        assertMutation(ReportController.class.getDeclaredMethod("generateZReport", UUID.class, GenerateZReportRequest.class),
                "pos:report:view-all", "pos-report-z", "generate");
        assertPermission(ReportController.class.getDeclaredMethod("listZReports", UUID.class),
                "pos:report:view-all");
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
