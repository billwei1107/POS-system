/**
 * @file TransferController.java
 * @description 調撥申請 Controller / Transfer request REST controller
 * @description_en REST endpoints for managing inter-store stock transfer lifecycle
 * @description_zh 提供門店調撥申請完整生命週期的 REST API
 */
package com.enterprise.inventory.controller;

import com.enterprise.common.dto.ApiResponse;
import com.enterprise.inventory.dto.request.CreateTransferRequest;
import com.enterprise.inventory.entity.TransferRequest;
import com.enterprise.inventory.service.TransferService;
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

    // ========================================
    // 建立調撥申請 / Create transfer
    // ========================================
    @PostMapping
    public ResponseEntity<ApiResponse<TransferRequest>> create(@Valid @RequestBody CreateTransferRequest req) {
        return ResponseEntity.ok(ApiResponse.success(transferService.create(req)));
    }

    // ========================================
    // 查詢門店調撥清單 / List transfers for store
    // ========================================
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponse<List<TransferRequest>>> listByStore(@PathVariable UUID storeId) {
        return ResponseEntity.ok(ApiResponse.success(transferService.listByStore(storeId)));
    }

    // ========================================
    // 查詢單一調撥 / Get transfer by ID
    // ========================================
    @GetMapping("/{transferId}")
    public ResponseEntity<ApiResponse<TransferRequest>> getById(@PathVariable UUID transferId) {
        return ResponseEntity.ok(ApiResponse.success(transferService.findById(transferId)));
    }

    // ========================================
    // 核准 / Approve
    // ========================================
    @PostMapping("/{transferId}/approve")
    public ResponseEntity<ApiResponse<TransferRequest>> approve(
            @PathVariable UUID transferId,
            @RequestParam(required = false) UUID approvedBy) {
        return ResponseEntity.ok(ApiResponse.success(transferService.approve(transferId, approvedBy)));
    }

    // ========================================
    // 確認出貨 / Ship
    // ========================================
    @PostMapping("/{transferId}/ship")
    public ResponseEntity<ApiResponse<TransferRequest>> ship(@PathVariable UUID transferId) {
        return ResponseEntity.ok(ApiResponse.success(transferService.ship(transferId)));
    }

    // ========================================
    // 確認收貨 / Receive
    // ========================================
    @PostMapping("/{transferId}/receive")
    public ResponseEntity<ApiResponse<TransferRequest>> receive(@PathVariable UUID transferId) {
        return ResponseEntity.ok(ApiResponse.success(transferService.receive(transferId)));
    }

    // ========================================
    // 取消調撥 / Cancel
    // ========================================
    @PostMapping("/{transferId}/cancel")
    public ResponseEntity<ApiResponse<TransferRequest>> cancel(@PathVariable UUID transferId) {
        return ResponseEntity.ok(ApiResponse.success(transferService.cancel(transferId)));
    }
}
