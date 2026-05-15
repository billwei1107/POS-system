/**
 * @file TransferController.java
 * @description 調撥申請 Controller / Transfer request REST controller
 * @description_en REST endpoints for managing inter-store stock transfer lifecycle
 * @description_zh 提供門店調撥申請完整生命週期的 REST API
 */
package com.enterprise.inventory.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.inventory.dto.request.CreateTransferRequest;
import com.enterprise.inventory.entity.TransferRequest;
import com.enterprise.inventory.service.TransferService;
import com.enterprise.organization.service.StoreAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final StoreAccessService storeAccessService;

    // ========================================
    // 建立調撥申請 / Create transfer
    // ========================================
    @PostMapping
    @RequirePermission("pos:inventory:transfer")
    @Auditable(module = "inventory-transfer", action = "create")
    public ResponseEntity<ApiResponse<TransferRequest>> create(@Valid @RequestBody CreateTransferRequest req) {
        storeAccessService.requireAllOperableStores(req.fromStoreId(), req.toStoreId());
        return ResponseEntity.ok(ApiResponse.success(transferService.create(req)));
    }

    // ========================================
    // 查詢門店調撥清單 / List transfers for store
    // ========================================
    @GetMapping("/stores/{storeId}")
    @RequirePermission("pos:inventory:read")
    public ResponseEntity<ApiResponse<List<TransferRequest>>> listByStore(@PathVariable UUID storeId) {
        storeAccessService.requireReadableStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(transferService.listByStore(storeId)));
    }

    // ========================================
    // 查詢單一調撥 / Get transfer by ID
    // ========================================
    @GetMapping("/{transferId}")
    @RequirePermission("pos:inventory:read")
    public ResponseEntity<ApiResponse<TransferRequest>> getById(@PathVariable UUID transferId) {
        TransferRequest transfer = transferService.findById(transferId);
        storeAccessService.requireAnyReadableStore(transfer.getFromStoreId(), transfer.getToStoreId());
        return ResponseEntity.ok(ApiResponse.success(transfer));
    }

    // ========================================
    // 核准 / Approve
    // ========================================
    @PostMapping("/{transferId}/approve")
    @RequirePermission("pos:inventory:transfer")
    @Auditable(module = "inventory-transfer", action = "approve")
    public ResponseEntity<ApiResponse<TransferRequest>> approve(
            @PathVariable UUID transferId,
            @RequestParam(required = false) UUID approvedBy) {
        TransferRequest transfer = transferService.findById(transferId);
        storeAccessService.requireOperableStore(transfer.getFromStoreId());
        return ResponseEntity.ok(ApiResponse.success(transferService.approve(transferId, approvedBy)));
    }

    // ========================================
    // 確認出貨 / Ship
    // ========================================
    @PostMapping("/{transferId}/ship")
    @RequirePermission("pos:inventory:transfer")
    @Auditable(module = "inventory-transfer", action = "ship")
    public ResponseEntity<ApiResponse<TransferRequest>> ship(@PathVariable UUID transferId) {
        TransferRequest transfer = transferService.findById(transferId);
        storeAccessService.requireOperableStore(transfer.getFromStoreId());
        return ResponseEntity.ok(ApiResponse.success(transferService.ship(transferId)));
    }

    // ========================================
    // 確認收貨 / Receive
    // ========================================
    @PostMapping("/{transferId}/receive")
    @RequirePermission("pos:inventory:transfer")
    @Auditable(module = "inventory-transfer", action = "receive")
    public ResponseEntity<ApiResponse<TransferRequest>> receive(@PathVariable UUID transferId) {
        TransferRequest transfer = transferService.findById(transferId);
        storeAccessService.requireOperableStore(transfer.getToStoreId());
        return ResponseEntity.ok(ApiResponse.success(transferService.receive(transferId)));
    }

    // ========================================
    // 取消調撥 / Cancel
    // ========================================
    @PostMapping("/{transferId}/cancel")
    @RequirePermission("pos:inventory:transfer")
    @Auditable(module = "inventory-transfer", action = "cancel")
    public ResponseEntity<ApiResponse<TransferRequest>> cancel(@PathVariable UUID transferId) {
        TransferRequest transfer = transferService.findById(transferId);
        storeAccessService.requireAnyReadableStore(transfer.getFromStoreId(), transfer.getToStoreId());
        return ResponseEntity.ok(ApiResponse.success(transferService.cancel(transferId)));
    }
}
