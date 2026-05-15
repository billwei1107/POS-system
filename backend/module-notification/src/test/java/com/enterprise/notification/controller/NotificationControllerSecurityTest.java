/**
 * @file NotificationControllerSecurityTest.java
 * @description 通知控制器權限註解測試 / Notification controller permission annotation tests
 * @description_en Verifies notification endpoints are protected by permission and audit annotations
 * @description_zh 驗證通知查詢與已讀操作端點具備權限與稽核註解，避免通知資料裸露
 */
package com.enterprise.notification.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificationControllerSecurityTest {

    @Test
    void notificationReadEndpointsUseReadPermission() throws NoSuchMethodException {
        assertPermission(NotificationController.class.getDeclaredMethod("getUnreadNotifications", int.class, int.class),
                "system:notification:read");
        assertPermission(NotificationController.class.getDeclaredMethod("getUnreadCount"),
                "system:notification:read");
    }

    @Test
    void notificationMutationEndpointsUseOperateOrManagePermissions() throws NoSuchMethodException {
        assertMutation(NotificationController.class.getDeclaredMethod("markAsRead", String.class),
                "system:notification:operate", "notification", "mark-read");
        assertMutation(NotificationController.class.getDeclaredMethod("markAllAsRead"),
                "system:notification:operate", "notification", "mark-all-read");
        assertMutation(NotificationController.class.getDeclaredMethod("testPush"),
                "system:notification:manage", "notification", "test-push");
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
