/**
 * @file TaxControllerSecurityTest.java
 * @description 稅務 Controller 權限與稽核註解測試 / Tax controller permission and audit annotation tests
 * @description_en Verifies invoice, invoice track and tax class endpoints keep required permission and audit annotations
 * @description_zh 驗證發票、字軌與稅別端點保留必要權限與稽核註解
 */
package com.enterprise.tax.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.tax.dto.request.AddInvoiceTrackRequest;
import com.enterprise.tax.dto.request.CreateTaxClassRequest;
import com.enterprise.tax.dto.request.IssueInvoiceRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaxControllerSecurityTest {

    @Test
    void invoiceEndpointsShouldDeclarePermissionsAndAudit() throws NoSuchMethodException {
        var issue = InvoiceController.class.getMethod("issue", IssueInvoiceRequest.class);
        assertPermission(issue.getAnnotation(RequirePermission.class), "pos:invoice:issue");
        assertAudit(issue.getAnnotation(Auditable.class), "pos-invoice", "issue");

        var getByOrder = InvoiceController.class.getMethod("getByOrder", UUID.class);
        assertPermission(getByOrder.getAnnotation(RequirePermission.class), "pos:invoice:read");

        var list = InvoiceController.class.getMethod("listByStore", UUID.class, LocalDateTime.class, LocalDateTime.class);
        assertPermission(list.getAnnotation(RequirePermission.class), "pos:invoice:read");

        var voidInvoice = InvoiceController.class.getMethod("voidInvoice", UUID.class, String.class);
        assertPermission(voidInvoice.getAnnotation(RequirePermission.class), "pos:invoice:void");
        assertAudit(voidInvoice.getAnnotation(Auditable.class), "pos-invoice", "void");
    }

    @Test
    void invoiceTrackEndpointsShouldDeclarePermissionsAndAudit() throws NoSuchMethodException {
        var list = InvoiceTrackController.class.getMethod("list", UUID.class);
        assertPermission(list.getAnnotation(RequirePermission.class), "pos:tax:read");

        var add = InvoiceTrackController.class.getMethod("add", AddInvoiceTrackRequest.class);
        assertPermission(add.getAnnotation(RequirePermission.class), "pos:invoice-track:manage");
        assertAudit(add.getAnnotation(Auditable.class), "pos-invoice-track", "add");
    }

    @Test
    void taxClassEndpointsShouldDeclarePermissionsAndAudit() throws NoSuchMethodException {
        var list = TaxClassController.class.getMethod("list", UUID.class);
        assertPermission(list.getAnnotation(RequirePermission.class), "pos:tax:read");

        var create = TaxClassController.class.getMethod("create", CreateTaxClassRequest.class);
        assertPermission(create.getAnnotation(RequirePermission.class), "pos:tax:manage");
        assertAudit(create.getAnnotation(Auditable.class), "pos-tax-class", "create");

        var deactivate = TaxClassController.class.getMethod("deactivate", UUID.class);
        assertPermission(deactivate.getAnnotation(RequirePermission.class), "pos:tax:manage");
        assertAudit(deactivate.getAnnotation(Auditable.class), "pos-tax-class", "deactivate");
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
