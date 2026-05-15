/**
 * @file CashDrawerController.java
 * @description 現金抽屜 Controller / Cash drawer controller
 * @description_en REST endpoints for opening/closing cash drawers and querying drawer status
 * @description_zh 現金抽屜開啟、關閉與狀態查詢的 REST 端點
 */
package com.enterprise.payment.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.payment.dto.request.OpenDrawerRequest;
import com.enterprise.payment.entity.CashDrawer;
import com.enterprise.payment.service.CashDrawerService;
import com.enterprise.organization.service.StoreAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/cash-drawers")
@RequiredArgsConstructor
public class CashDrawerController {

    private final CashDrawerService cashDrawerService;
    private final StoreAccessService storeAccessService;

    // ========================================
    // 開啟現金抽屜 / Open cash drawer
    // ========================================
    @PostMapping("/open")
    @RequirePermission("pos:cash-drawer:manage")
    @Auditable(module = "pos-cash-drawer", action = "open")
    public ResponseEntity<ApiResponse<CashDrawer>> open(@Valid @RequestBody OpenDrawerRequest req) {
        storeAccessService.requireOperableStore(req.storeId());
        return ResponseEntity.ok(ApiResponse.success(cashDrawerService.open(req)));
    }

    // ========================================
    // 關閉現金抽屜 / Close cash drawer
    // ========================================
    @PostMapping("/{id}/close")
    @RequirePermission("pos:cash-drawer:manage")
    @Auditable(module = "pos-cash-drawer", action = "close")
    public ResponseEntity<ApiResponse<CashDrawer>> close(
            @PathVariable UUID id,
            @RequestParam UUID closedBy,
            @RequestParam(required = false) BigDecimal closingAmount,
            @RequestParam(required = false) String note) {
        storeAccessService.requireOperableStore(cashDrawerService.findStoreId(id));
        return ResponseEntity.ok(ApiResponse.success(
            cashDrawerService.close(id, closedBy, closingAmount, note)
        ));
    }

    // ========================================
    // 查詢終端機開啟中的抽屜 / Get open drawer for terminal
    // ========================================
    @GetMapping("/terminal/{terminalId}/open")
    @RequirePermission("pos:cash-drawer:read")
    public ResponseEntity<ApiResponse<CashDrawer>> getOpen(@PathVariable UUID terminalId) {
        CashDrawer drawer = cashDrawerService.getOpen(terminalId);
        storeAccessService.requireReadableStore(drawer.getStoreId());
        return ResponseEntity.ok(ApiResponse.success(drawer));
    }
}
