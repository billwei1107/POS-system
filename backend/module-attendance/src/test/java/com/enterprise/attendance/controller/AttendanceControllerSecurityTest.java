/**
 * @file AttendanceControllerSecurityTest.java
 * @description 出勤控制器權限註解測試 / Attendance controller permission annotation tests
 * @description_en Verifies attendance operation and setting endpoints declare permission and audit annotations
 * @description_zh 驗證打卡、補卡、班表、假日與地理圍欄端點具備權限與稽核註解
 */
package com.enterprise.attendance.controller;

import com.enterprise.attendance.dto.ClockInRequest;
import com.enterprise.attendance.dto.CorrectionRequest;
import com.enterprise.attendance.entity.Geofence;
import com.enterprise.attendance.entity.Holiday;
import com.enterprise.attendance.entity.ShiftSchedule;
import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AttendanceControllerSecurityTest {

    @Test
    void attendanceRecordEndpointsUseAttendancePermissions() throws NoSuchMethodException {
        assertMutation(AttendanceController.class.getDeclaredMethod("clockIn", ClockInRequest.class),
                "system:attendance:operate", "attendance-record", "clock-in");
        assertMutation(AttendanceController.class.getDeclaredMethod("clockOut"),
                "system:attendance:operate", "attendance-record", "clock-out");
        assertPermission(AttendanceController.class.getDeclaredMethod("getTodayRecord"),
                "system:attendance:read");
        assertPermission(AttendanceController.class.getDeclaredMethod("getMonthlyRecords",
                String.class, int.class, int.class), "system:attendance:read");
        assertMutation(AttendanceController.class.getDeclaredMethod("submitCorrection", CorrectionRequest.class),
                "system:attendance:operate", "attendance-correction", "submit");
    }

    @Test
    void geofenceEndpointsUseAttendancePermissions() throws NoSuchMethodException {
        assertPermission(GeofenceController.class.getDeclaredMethod("getAllGeofences"),
                "system:attendance:read");
        assertMutation(GeofenceController.class.getDeclaredMethod("createGeofence", Geofence.class),
                "system:attendance:manage", "attendance-geofence", "create");
        assertMutation(GeofenceController.class.getDeclaredMethod("updateGeofence", UUID.class, Geofence.class),
                "system:attendance:manage", "attendance-geofence", "update");
        assertMutation(GeofenceController.class.getDeclaredMethod("deleteGeofence", UUID.class),
                "system:attendance:manage", "attendance-geofence", "delete");
    }

    @Test
    void holidayEndpointsUseAttendancePermissions() throws NoSuchMethodException {
        assertPermission(HolidayController.class.getDeclaredMethod("getHolidays", int.class),
                "system:attendance:read");
        assertMutation(HolidayController.class.getDeclaredMethod("createHoliday", Holiday.class),
                "system:attendance:manage", "attendance-holiday", "create");
        assertMutation(HolidayController.class.getDeclaredMethod("updateHoliday", UUID.class, Holiday.class),
                "system:attendance:manage", "attendance-holiday", "update");
        assertMutation(HolidayController.class.getDeclaredMethod("deleteHoliday", UUID.class),
                "system:attendance:manage", "attendance-holiday", "delete");
    }

    @Test
    void shiftEndpointsUseAttendancePermissions() throws NoSuchMethodException {
        assertPermission(ShiftController.class.getDeclaredMethod("getAllShifts"),
                "system:attendance:read");
        assertPermission(ShiftController.class.getDeclaredMethod("getShiftById", UUID.class),
                "system:attendance:read");
        assertMutation(ShiftController.class.getDeclaredMethod("createShift", ShiftSchedule.class),
                "system:attendance:manage", "attendance-shift", "create");
        assertMutation(ShiftController.class.getDeclaredMethod("updateShift", UUID.class, ShiftSchedule.class),
                "system:attendance:manage", "attendance-shift", "update");
        assertMutation(ShiftController.class.getDeclaredMethod("deleteShift", UUID.class),
                "system:attendance:manage", "attendance-shift", "delete");
        assertMutation(ShiftController.class.getDeclaredMethod("assignShift",
                        UUID.class, String.class, String.class, String.class),
                "system:attendance:manage", "attendance-shift", "assign");
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
