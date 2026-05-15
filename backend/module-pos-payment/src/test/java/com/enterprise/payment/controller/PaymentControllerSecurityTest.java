/**
 * @file PaymentControllerSecurityTest.java
 * @description 金流控制器權限註解測試 / Payment controller permission annotation tests
 * @description_en Verifies payment, cash drawer, and reconciliation endpoints declare permission and audit annotations
 * @description_zh 驗證付款、錢櫃與對帳端點具備權限與稽核註解，避免正式金流操作裸露
 */
package com.enterprise.payment.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.payment.dto.request.CreateGatewayConfigRequest;
import com.enterprise.payment.dto.request.CreatePayMethodRequest;
import com.enterprise.payment.dto.request.OpenDrawerRequest;
import com.enterprise.payment.dto.request.ProcessPaymentRequest;
import com.enterprise.payment.dto.request.UpdateGatewayConfigRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentControllerSecurityTest {

    @Test
    void paymentEndpointsUsePaymentPermissions() throws NoSuchMethodException {
        assertMutation(PaymentController.class.getDeclaredMethod("process", ProcessPaymentRequest.class),
                "pos:payment:process", "pos-payment", "process");
        assertPermission(PaymentController.class.getDeclaredMethod("getByOrder", UUID.class),
                "pos:payment:read");
        assertPermission(PaymentController.class.getDeclaredMethod("listMethods", UUID.class),
                "pos:payment:read");
        assertMutation(PaymentController.class.getDeclaredMethod("createMethod", CreatePayMethodRequest.class),
                "pos:payment:method-manage", "pos-pay-method", "create");
        assertMutation(PaymentController.class.getDeclaredMethod("deactivateMethod", UUID.class),
                "pos:payment:method-manage", "pos-pay-method", "deactivate");
        assertPermission(PaymentController.class.getDeclaredMethod("listGateways", UUID.class),
                "pos:payment:read");
        assertMutation(PaymentController.class.getDeclaredMethod("createGateway", CreateGatewayConfigRequest.class),
                "pos:payment:method-manage", "pos-gateway-config", "create");
        assertMutation(PaymentController.class.getDeclaredMethod("updateGateway", UUID.class, UpdateGatewayConfigRequest.class),
                "pos:payment:method-manage", "pos-gateway-config", "update");
        assertMutation(PaymentController.class.getDeclaredMethod("deactivateGateway", UUID.class),
                "pos:payment:method-manage", "pos-gateway-config", "deactivate");
    }

    @Test
    void cashDrawerEndpointsUseCashDrawerPermissions() throws NoSuchMethodException {
        assertMutation(CashDrawerController.class.getDeclaredMethod("open", OpenDrawerRequest.class),
                "pos:cash-drawer:manage", "pos-cash-drawer", "open");
        assertMutation(CashDrawerController.class.getDeclaredMethod("close",
                        UUID.class, UUID.class, BigDecimal.class, String.class),
                "pos:cash-drawer:manage", "pos-cash-drawer", "close");
        assertPermission(CashDrawerController.class.getDeclaredMethod("getOpen", UUID.class),
                "pos:cash-drawer:read");
    }

    @Test
    void reconciliationEndpointsUseReconciliationPermissions() throws NoSuchMethodException {
        assertMutation(ReconciliationController.class.getDeclaredMethod("generate", UUID.class, LocalDate.class),
                "pos:reconciliation:manage", "pos-reconciliation", "generate");
        assertPermission(ReconciliationController.class.getDeclaredMethod("list", UUID.class, LocalDate.class),
                "pos:reconciliation:read");
        assertMutation(ReconciliationController.class.getDeclaredMethod("confirm",
                        UUID.class, BigDecimal.class, UUID.class),
                "pos:reconciliation:manage", "pos-reconciliation", "confirm");
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
