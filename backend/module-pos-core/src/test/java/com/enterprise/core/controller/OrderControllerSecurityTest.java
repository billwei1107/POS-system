/**
 * @file OrderControllerSecurityTest.java
 * @description 訂單 Controller 權限與稽核註解測試 / Order controller permission and audit annotation tests
 * @description_en Verifies order, held order and refund endpoints keep required permission and audit annotations
 * @description_zh 驗證訂單、掛單與退款端點保留必要權限與稽核註解
 */
package com.enterprise.core.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.core.dto.request.CreateHeldOrderRequest;
import com.enterprise.core.dto.request.CreateOrderRequest;
import com.enterprise.core.dto.request.CreateRefundRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderControllerSecurityTest {

    @Test
    void orderEndpointsShouldDeclarePermissionsAndAudit() throws NoSuchMethodException {
        var create = OrderController.class.getMethod("createOrder", CreateOrderRequest.class);
        assertPermission(create.getAnnotation(RequirePermission.class), "pos:order:create");
        assertAudit(create.getAnnotation(Auditable.class), "pos-order", "create");

        var getById = OrderController.class.getMethod("getById", UUID.class);
        assertPermission(getById.getAnnotation(RequirePermission.class), "pos:order:read");

        var list = OrderController.class.getMethod(
                "listByStore",
                UUID.class,
                com.enterprise.core.entity.Order.OrderStatus.class,
                java.time.LocalDateTime.class,
                java.time.LocalDateTime.class,
                int.class,
                int.class
        );
        assertPermission(list.getAnnotation(RequirePermission.class), "pos:order:read");

        var complete = OrderController.class.getMethod(
                "completeOrder",
                UUID.class,
                String.class,
                BigDecimal.class
        );
        assertPermission(complete.getAnnotation(RequirePermission.class), "pos:payment:process");
        assertAudit(complete.getAnnotation(Auditable.class), "pos-order", "complete");

        var voidOrder = OrderController.class.getMethod("voidOrder", UUID.class, UUID.class, String.class);
        assertPermission(voidOrder.getAnnotation(RequirePermission.class), "pos:order:void");
        assertAudit(voidOrder.getAnnotation(Auditable.class), "pos-order", "void");
    }

    @Test
    void heldOrderEndpointsShouldDeclarePermissionsAndAudit() throws NoSuchMethodException {
        var create = HeldOrderController.class.getMethod("create", CreateHeldOrderRequest.class);
        assertPermission(create.getAnnotation(RequirePermission.class), "pos:order:create");
        assertAudit(create.getAnnotation(Auditable.class), "pos-held-order", "create");

        var list = HeldOrderController.class.getMethod("list", UUID.class, UUID.class);
        assertPermission(list.getAnnotation(RequirePermission.class), "pos:order:read");

        var delete = HeldOrderController.class.getMethod("delete", UUID.class);
        assertPermission(delete.getAnnotation(RequirePermission.class), "pos:order:create");
        assertAudit(delete.getAnnotation(Auditable.class), "pos-held-order", "delete");
    }

    @Test
    void refundEndpointsShouldKeepPermissionsAndAudit() throws NoSuchMethodException {
        var listRefunds = RefundController.class.getMethod(
                "listRefunds",
                UUID.class,
                UUID.class,
                com.enterprise.core.entity.OrderRefund.RefundStatus.class,
                java.time.LocalDateTime.class,
                java.time.LocalDateTime.class,
                int.class,
                int.class
        );
        assertPermission(listRefunds.getAnnotation(RequirePermission.class), "pos:order:read");

        var getRefund = RefundController.class.getMethod("getById", UUID.class);
        assertPermission(getRefund.getAnnotation(RequirePermission.class), "pos:order:read");

        var createRefund = RefundController.class.getMethod("createRefund", CreateRefundRequest.class);
        assertPermission(createRefund.getAnnotation(RequirePermission.class), "pos:order:refund");
        assertAudit(createRefund.getAnnotation(Auditable.class), "pos-refund", "create");

        var completeRefund = RefundController.class.getMethod("completeRefund", UUID.class);
        assertPermission(completeRefund.getAnnotation(RequirePermission.class), "pos:order:refund");
        assertAudit(completeRefund.getAnnotation(Auditable.class), "pos-refund", "complete");
    }

    private void assertPermission(RequirePermission permission, String expectedValue) {
        assertThat(permission).isNotNull();
        assertThat(permission.value()).isEqualTo(expectedValue);
    }

    private void assertAudit(Auditable auditable, String expectedModule, String expectedAction) {
        assertThat(auditable).isNotNull();
        assertThat(auditable.module()).isEqualTo(expectedModule);
        assertThat(auditable.action()).isEqualTo(expectedAction);
    }
}
