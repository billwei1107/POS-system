/**
 * @file MemberControllerSecurityTest.java
 * @description 會員控制器權限註解測試 / Member controller permission annotation tests
 */
package com.enterprise.crm.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.crm.dto.MemberRequest;
import com.enterprise.crm.dto.PointAdjustmentRequest;
import com.enterprise.crm.dto.PointRedemptionRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MemberControllerSecurityTest {

    @Test
    void memberEndpointsUseCrmPermissions() throws NoSuchMethodException {
        assertPermission(MemberController.class.getDeclaredMethod("search", String.class, int.class),
                "pos:member:read");
        assertPermission(MemberController.class.getDeclaredMethod("getById", UUID.class),
                "pos:member:read");
        assertPermission(MemberController.class.getDeclaredMethod("listPointLedgers", UUID.class),
                "pos:member:read");

        Method create = MemberController.class.getDeclaredMethod("create", MemberRequest.class);
        assertPermission(create, "pos:member:operate");
        Auditable auditable = create.getAnnotation(Auditable.class);
        assertNotNull(auditable, "Member creation should be audited");
        assertEquals("pos-member", auditable.module());
        assertEquals("create", auditable.action());

        assertMutation(MemberController.class.getDeclaredMethod(
                "redeemPoints", UUID.class, PointRedemptionRequest.class),
                "pos:member:operate", "redeem-points");
        assertMutation(MemberController.class.getDeclaredMethod(
                "adjustPoints", UUID.class, PointAdjustmentRequest.class),
                "pos:member:manage", "adjust-points");
    }

    private void assertMutation(Method method, String permissionCode, String action) {
        assertPermission(method, permissionCode);
        Auditable auditable = method.getAnnotation(Auditable.class);
        assertNotNull(auditable, "Member mutation should be audited");
        assertEquals("pos-member", auditable.module());
        assertEquals(action, auditable.action());
    }

    private void assertPermission(Method method, String permissionCode) {
        RequirePermission permission = method.getAnnotation(RequirePermission.class);
        assertNotNull(permission, "Endpoint should declare a permission requirement");
        assertEquals(permissionCode, permission.value());
    }
}
