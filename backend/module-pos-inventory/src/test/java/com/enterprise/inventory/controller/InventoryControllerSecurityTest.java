/**
 * @file InventoryControllerSecurityTest.java
 * @description 庫存控制器權限註解測試 / Inventory controller permission annotation tests
 * @description_en Verifies inventory read and mutation endpoints are protected by permission and audit annotations
 * @description_zh 驗證庫存查詢與異動端點具備權限與稽核註解，避免庫存資料裸露
 */
package com.enterprise.inventory.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.inventory.dto.request.AdjustStockRequest;
import com.enterprise.inventory.dto.request.CreateTransferRequest;
import com.enterprise.inventory.dto.request.ReceiveStockRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InventoryControllerSecurityTest {

    @Test
    void stockEndpointsUseInventoryPermissions() throws NoSuchMethodException {
        assertPermission(StockController.class.getDeclaredMethod("listStock", UUID.class),
                "pos:inventory:read");
        assertPermission(StockController.class.getDeclaredMethod("getStock", UUID.class, UUID.class),
                "pos:inventory:read");
        assertPermission(StockController.class.getDeclaredMethod("listMovements", UUID.class, UUID.class),
                "pos:inventory:read");
        assertPermission(StockController.class.getDeclaredMethod("listAlerts", UUID.class),
                "pos:inventory:read");
        assertMutation(StockController.class.getDeclaredMethod("adjust", AdjustStockRequest.class),
                "pos:inventory:adjust", "inventory-stock", "adjust");
        assertMutation(StockController.class.getDeclaredMethod("receive", ReceiveStockRequest.class),
                "pos:inventory:receive", "inventory-stock", "receive");
        assertMutation(StockController.class.getDeclaredMethod("acknowledgeAlert", UUID.class, UUID.class),
                "pos:inventory:adjust", "inventory-alert", "acknowledge");
    }

    @Test
    void stockTakeEndpointsUseInventoryPermissions() throws NoSuchMethodException {
        assertMutation(StockTakeController.class.getDeclaredMethod("start", UUID.class, UUID.class),
                "pos:inventory:stock-take", "inventory-stock-take", "start");
        assertPermission(StockTakeController.class.getDeclaredMethod("listByStore", UUID.class),
                "pos:inventory:read");
        assertPermission(StockTakeController.class.getDeclaredMethod("getById", UUID.class),
                "pos:inventory:read");
        assertMutation(StockTakeController.class.getDeclaredMethod("submitCount", UUID.class, UUID.class, java.math.BigDecimal.class),
                "pos:inventory:stock-take", "inventory-stock-take", "submit-count");
        assertMutation(StockTakeController.class.getDeclaredMethod("complete", UUID.class),
                "pos:inventory:stock-take", "inventory-stock-take", "complete");
        assertMutation(StockTakeController.class.getDeclaredMethod("cancel", UUID.class),
                "pos:inventory:stock-take", "inventory-stock-take", "cancel");
    }

    @Test
    void transferEndpointsUseInventoryPermissions() throws NoSuchMethodException {
        assertMutation(TransferController.class.getDeclaredMethod("create", CreateTransferRequest.class),
                "pos:inventory:transfer", "inventory-transfer", "create");
        assertPermission(TransferController.class.getDeclaredMethod("listByStore", UUID.class),
                "pos:inventory:read");
        assertPermission(TransferController.class.getDeclaredMethod("getById", UUID.class),
                "pos:inventory:read");
        assertMutation(TransferController.class.getDeclaredMethod("approve", UUID.class, UUID.class),
                "pos:inventory:transfer", "inventory-transfer", "approve");
        assertMutation(TransferController.class.getDeclaredMethod("ship", UUID.class),
                "pos:inventory:transfer", "inventory-transfer", "ship");
        assertMutation(TransferController.class.getDeclaredMethod("receive", UUID.class),
                "pos:inventory:transfer", "inventory-transfer", "receive");
        assertMutation(TransferController.class.getDeclaredMethod("cancel", UUID.class),
                "pos:inventory:transfer", "inventory-transfer", "cancel");
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
