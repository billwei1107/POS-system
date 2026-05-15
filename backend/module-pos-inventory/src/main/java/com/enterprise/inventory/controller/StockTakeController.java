/**
 * @file StockTakeController.java
 * @description 盤點 Controller / Stock take REST controller
 * @description_en REST endpoints for managing inventory stock take sessions
 * @description_zh 提供盤點工作階段管理的 REST API
 */
package com.enterprise.inventory.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.inventory.entity.StockTake;
import com.enterprise.inventory.service.StockTakeService;
import com.enterprise.organization.service.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/stock-takes")
@RequiredArgsConstructor
public class StockTakeController {

    private final StockTakeService stockTakeService;
    private final StoreAccessService storeAccessService;

    // ========================================
    // 開始盤點 / Start stock take
    // ========================================
    @PostMapping("/start")
    @RequirePermission("pos:inventory:stock-take")
    @Auditable(module = "inventory-stock-take", action = "start")
    public ResponseEntity<ApiResponse<StockTake>> start(
            @RequestParam UUID storeId,
            @RequestParam(required = false) UUID createdBy) {
        storeAccessService.requireOperableStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(stockTakeService.start(storeId, createdBy)));
    }

    // ========================================
    // 查詢門店盤點紀錄 / List stock takes for store
    // ========================================
    @GetMapping("/stores/{storeId}")
    @RequirePermission("pos:inventory:read")
    public ResponseEntity<ApiResponse<List<StockTake>>> listByStore(@PathVariable UUID storeId) {
        storeAccessService.requireReadableStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(stockTakeService.listByStore(storeId)));
    }

    // ========================================
    // 查詢單次盤點 / Get stock take by ID
    // ========================================
    @GetMapping("/{stockTakeId}")
    @RequirePermission("pos:inventory:read")
    public ResponseEntity<ApiResponse<StockTake>> getById(@PathVariable UUID stockTakeId) {
        StockTake stockTake = stockTakeService.findById(stockTakeId);
        storeAccessService.requireReadableStore(stockTake.getStoreId());
        return ResponseEntity.ok(ApiResponse.success(stockTake));
    }

    // ========================================
    // 登記盤點數量 / Submit count for item
    // ========================================
    @PostMapping("/{stockTakeId}/items/{itemId}/count")
    @RequirePermission("pos:inventory:stock-take")
    @Auditable(module = "inventory-stock-take", action = "submit-count")
    public ResponseEntity<ApiResponse<StockTake>> submitCount(
            @PathVariable UUID stockTakeId,
            @PathVariable UUID itemId,
            @RequestParam BigDecimal countedQty) {
        StockTake stockTake = stockTakeService.findById(stockTakeId);
        storeAccessService.requireOperableStore(stockTake.getStoreId());
        return ResponseEntity.ok(ApiResponse.success(stockTakeService.submitCount(stockTakeId, itemId, countedQty)));
    }

    // ========================================
    // 完成盤點 / Complete stock take
    // ========================================
    @PostMapping("/{stockTakeId}/complete")
    @RequirePermission("pos:inventory:stock-take")
    @Auditable(module = "inventory-stock-take", action = "complete")
    public ResponseEntity<ApiResponse<StockTake>> complete(@PathVariable UUID stockTakeId) {
        StockTake stockTake = stockTakeService.findById(stockTakeId);
        storeAccessService.requireOperableStore(stockTake.getStoreId());
        return ResponseEntity.ok(ApiResponse.success(stockTakeService.complete(stockTakeId)));
    }

    // ========================================
    // 取消盤點 / Cancel stock take
    // ========================================
    @PostMapping("/{stockTakeId}/cancel")
    @RequirePermission("pos:inventory:stock-take")
    @Auditable(module = "inventory-stock-take", action = "cancel")
    public ResponseEntity<ApiResponse<StockTake>> cancel(@PathVariable UUID stockTakeId) {
        StockTake stockTake = stockTakeService.findById(stockTakeId);
        storeAccessService.requireOperableStore(stockTake.getStoreId());
        return ResponseEntity.ok(ApiResponse.success(stockTakeService.cancel(stockTakeId)));
    }
}
