/**
 * @file InvoiceTrackController.java
 * @description 發票字軌 REST 控制器 / Invoice track REST controller
 * @description_en Endpoints for registering and listing bi-monthly MoF-allocated character tracks
 * @description_zh 登記與查詢財政部配發的電子發票字軌號碼範圍
 */
package com.enterprise.tax.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.service.StoreAccessService;
import com.enterprise.tax.dto.request.AddInvoiceTrackRequest;
import com.enterprise.tax.entity.InvoiceTrack;
import com.enterprise.tax.service.InvoiceTrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/invoice-tracks")
@RequiredArgsConstructor
public class InvoiceTrackController {

    private final InvoiceTrackService trackService;
    private final StoreAccessService storeAccessService;

    @GetMapping
    @RequirePermission("pos:tax:read")
    public ResponseEntity<ApiResponse<List<InvoiceTrack>>> list(@RequestParam UUID storeId) {
        storeAccessService.requireReadableStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(trackService.listByStore(storeId)));
    }

    @PostMapping
    @RequirePermission("pos:invoice-track:manage")
    @Auditable(module = "pos-invoice-track", action = "add")
    public ResponseEntity<ApiResponse<InvoiceTrack>> add(@Valid @RequestBody AddInvoiceTrackRequest req) {
        storeAccessService.requireOperableStore(req.storeId());
        return ResponseEntity.ok(ApiResponse.success(trackService.addTrack(req)));
    }
}
