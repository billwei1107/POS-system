/**
 * @file ProductControllerSecurityTest.java
 * @description 商品控制器權限註解測試 / Product controller permission annotation tests
 * @description_en Verifies product catalog endpoints declare permission and audit annotations
 * @description_zh 驗證商品與分類端點具備權限與稽核註解，避免正式後台敏感操作裸露
 */
package com.enterprise.product.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.product.dto.CategoryRequest;
import com.enterprise.product.dto.PriceRuleRequest;
import com.enterprise.product.dto.ProductItemRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductControllerSecurityTest {

    @Test
    void productItemEndpointsUseProductPermissions() throws NoSuchMethodException {
        assertPermission(ProductItemController.class.getDeclaredMethod("listAll",
                int.class, int.class, UUID.class, String.class), "pos:product:read");
        assertPermission(ProductItemController.class.getDeclaredMethod("getById", UUID.class),
                "pos:product:read");
        assertPermission(ProductItemController.class.getDeclaredMethod("getBySku", String.class),
                "pos:product:read");
        assertMutation(ProductItemController.class.getDeclaredMethod("create", ProductItemRequest.class),
                "pos-product", "create");
        assertMutation(ProductItemController.class.getDeclaredMethod("update", UUID.class, ProductItemRequest.class),
                "pos-product", "update");
        assertMutation(ProductItemController.class.getDeclaredMethod("delete", UUID.class),
                "pos-product", "delete");
    }

    @Test
    void categoryEndpointsUseProductPermissions() throws NoSuchMethodException {
        assertPermission(ProductCategoryController.class.getDeclaredMethod("listAll"),
                "pos:product:read");
        assertPermission(ProductCategoryController.class.getDeclaredMethod("listRoots"),
                "pos:product:read");
        assertPermission(ProductCategoryController.class.getDeclaredMethod("listChildren", UUID.class),
                "pos:product:read");
        assertPermission(ProductCategoryController.class.getDeclaredMethod("getById", UUID.class),
                "pos:product:read");
        assertMutation(ProductCategoryController.class.getDeclaredMethod("create", CategoryRequest.class),
                "pos-product-category", "create");
        assertMutation(ProductCategoryController.class.getDeclaredMethod("update", UUID.class, CategoryRequest.class),
                "pos-product-category", "update");
        assertMutation(ProductCategoryController.class.getDeclaredMethod("delete", UUID.class),
                "pos-product-category", "delete");
    }

    @Test
    void priceRuleEndpointsUseProductPermissions() throws NoSuchMethodException {
        assertPermission(PriceRuleController.class.getDeclaredMethod("list",
                UUID.class, UUID.class, boolean.class), "pos:product:read");
        assertMutation(PriceRuleController.class.getDeclaredMethod("create", PriceRuleRequest.class),
                "pos-price-rule", "create");
        assertMutation(PriceRuleController.class.getDeclaredMethod("update", UUID.class, PriceRuleRequest.class),
                "pos-price-rule", "update");
        assertMutation(PriceRuleController.class.getDeclaredMethod("deactivate", UUID.class),
                "pos-price-rule", "deactivate");
    }

    private void assertMutation(Method method, String module, String action) {
        assertPermission(method, "pos:product:manage");

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
