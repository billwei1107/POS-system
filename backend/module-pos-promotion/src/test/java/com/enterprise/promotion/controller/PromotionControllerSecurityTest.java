/**
 * @file PromotionControllerSecurityTest.java
 * @description 促銷控制器權限測試 / Promotion controller permission tests
 * @description_en Verifies promotion endpoints declare permission and audit annotations
 * @description_zh 驗證促銷端點具備權限與稽核註解
 */
package com.enterprise.promotion.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.promotion.dto.PromotionEvaluationRequest;
import com.enterprise.promotion.dto.PromotionRuleRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PromotionControllerSecurityTest {

    @Test
    void promotionEndpointsUsePromotionPermissions() throws NoSuchMethodException {
        assertPermission(PromotionController.class.getDeclaredMethod("list", UUID.class),
                "pos:promotion:read");
        assertMutation(PromotionController.class.getDeclaredMethod("create", PromotionRuleRequest.class),
                "pos:promotion:manage", "pos-promotion", "create");
        assertMutation(PromotionController.class.getDeclaredMethod("update", UUID.class, PromotionRuleRequest.class),
                "pos:promotion:manage", "pos-promotion", "update");
        assertMutation(PromotionController.class.getDeclaredMethod("deactivate", UUID.class),
                "pos:promotion:manage", "pos-promotion", "deactivate");
        assertPermission(PromotionController.class.getDeclaredMethod("evaluate", PromotionEvaluationRequest.class),
                "pos:promotion:read");
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
