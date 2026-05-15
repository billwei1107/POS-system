/**
 * @file HeldOrderController.java
 * @description 掛單管理 Controller / Held order management controller
 * @description_en REST endpoints for holding, listing and removing parked POS carts
 * @description_zh POS 掛單建立、查詢與刪除 REST 端點
 */
package com.enterprise.core.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.core.dto.request.CreateHeldOrderRequest;
import com.enterprise.core.dto.response.HeldOrderResponse;
import com.enterprise.core.service.HeldOrderService;
import com.enterprise.organization.service.StoreAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/held-orders")
@RequiredArgsConstructor
public class HeldOrderController {

    private final HeldOrderService heldOrderService;
    private final StoreAccessService storeAccessService;

    // ========================================
    // 建立掛單 / Create held order
    // ========================================
    @PostMapping
    @RequirePermission("pos:order:create")
    @Auditable(module = "pos-held-order", action = "create")
    public ResponseEntity<ApiResponse<HeldOrderResponse>> create(
            @Valid @RequestBody CreateHeldOrderRequest request) {
        storeAccessService.requireOperableStore(request.storeId());
        return ResponseEntity.ok(ApiResponse.success(heldOrderService.create(request)));
    }

    // ========================================
    // 查詢掛單 / List held orders
    // ========================================
    @GetMapping
    @RequirePermission("pos:order:read")
    public ResponseEntity<ApiResponse<List<HeldOrderResponse>>> list(
            @RequestParam UUID storeId,
            @RequestParam(required = false) UUID terminalId) {
        storeAccessService.requireReadableStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(heldOrderService.list(storeId, terminalId)));
    }

    // ========================================
    // 刪除掛單 / Delete held order
    // ========================================
    @DeleteMapping("/{id}")
    @RequirePermission("pos:order:create")
    @Auditable(module = "pos-held-order", action = "delete")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        storeAccessService.requireOperableStore(heldOrderService.findStoreId(id));
        heldOrderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
