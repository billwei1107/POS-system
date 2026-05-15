/**
 * @file InvoiceController.java
 * @description 電子發票 REST 控制器 / E-invoice REST controller
 * @description_en Endpoints for issuing, querying, and voiding e-invoices
 * @description_zh 電子發票的開立、查詢、作廢 REST 端點
 */
package com.enterprise.tax.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.service.StoreAccessService;
import com.enterprise.tax.dto.request.IssueInvoiceRequest;
import com.enterprise.tax.dto.response.InvoiceResponse;
import com.enterprise.tax.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final StoreAccessService storeAccessService;

    // ========================================
    // 手動開立發票 / Manual invoice issuance
    // ========================================
    @PostMapping
    @RequirePermission("pos:invoice:issue")
    @Auditable(module = "pos-invoice", action = "issue")
    public ResponseEntity<ApiResponse<InvoiceResponse>> issue(@Valid @RequestBody IssueInvoiceRequest req) {
        storeAccessService.requireOperableStore(req.storeId());
        return ResponseEntity.ok(ApiResponse.success(invoiceService.issue(req)));
    }

    // ========================================
    // 依訂單查詢 / Query by order
    // ========================================
    @GetMapping("/orders/{orderId}")
    @RequirePermission("pos:invoice:read")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getByOrder(@PathVariable UUID orderId) {
        return invoiceService.getByOrder(orderId)
                .map(inv -> {
                    storeAccessService.requireReadableStore(inv.storeId());
                    return ResponseEntity.ok(ApiResponse.success(inv));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ========================================
    // 門店發票列表 / Store invoice list
    // ========================================
    @GetMapping
    @RequirePermission("pos:invoice:read")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> listByStore(
            @RequestParam UUID storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        storeAccessService.requireReadableStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(invoiceService.listByStore(storeId, from, to)));
    }

    // ========================================
    // 作廢發票 / Void invoice
    // ========================================
    @PostMapping("/{id}/void")
    @RequirePermission("pos:invoice:void")
    @Auditable(module = "pos-invoice", action = "void")
    public ResponseEntity<ApiResponse<InvoiceResponse>> voidInvoice(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "手動作廢") String reason) {
        storeAccessService.requireOperableStore(invoiceService.findStoreId(id));
        return ResponseEntity.ok(ApiResponse.success(invoiceService.voidInvoice(id, reason)));
    }
}
