/**
 * @file StockTakeController.java
 * @description 盤點 Controller / Stock take REST controller
 * @description_en REST endpoints for managing inventory stock take sessions
 * @description_zh 提供盤點工作階段管理的 REST API
 */
package com.enterprise.inventory.controller;

import com.enterprise.common.dto.ApiResponse;
import com.enterprise.inventory.entity.StockTake;
import com.enterprise.inventory.service.StockTakeService;
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

    // ========================================
    // 開始盤點 / Start stock take
    // ========================================
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<StockTake>> start(
            @RequestParam UUID storeId,
            @RequestParam(required = false) UUID createdBy) {
        return ResponseEntity.ok(ApiResponse.success(stockTakeService.start(storeId, createdBy)));
    }

    // ========================================
    // 查詢門店盤點紀錄 / List stock takes for store
    // ========================================
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponse<List<StockTake>>> listByStore(@PathVariable UUID storeId) {
        return ResponseEntity.ok(ApiResponse.success(stockTakeService.listByStore(storeId)));
    }

    // ========================================
    // 查詢單次盤點 / Get stock take by ID
    // ========================================
    @GetMapping("/{stockTakeId}")
    public ResponseEntity<ApiResponse<StockTake>> getById(@PathVariable UUID stockTakeId) {
        return ResponseEntity.ok(ApiResponse.success(stockTakeService.findById(stockTakeId)));
    }

    // ========================================
    // 登記盤點數量 / Submit count for item
    // ========================================
    @PostMapping("/{stockTakeId}/items/{itemId}/count")
    public ResponseEntity<ApiResponse<StockTake>> submitCount(
            @PathVariable UUID stockTakeId,
            @PathVariable UUID itemId,
            @RequestParam BigDecimal countedQty) {
        return ResponseEntity.ok(ApiResponse.success(stockTakeService.submitCount(stockTakeId, itemId, countedQty)));
    }

    // ========================================
    // 完成盤點 / Complete stock take
    // ========================================
    @PostMapping("/{stockTakeId}/complete")
    public ResponseEntity<ApiResponse<StockTake>> complete(@PathVariable UUID stockTakeId) {
        return ResponseEntity.ok(ApiResponse.success(stockTakeService.complete(stockTakeId)));
    }

    // ========================================
    // 取消盤點 / Cancel stock take
    // ========================================
    @PostMapping("/{stockTakeId}/cancel")
    public ResponseEntity<ApiResponse<StockTake>> cancel(@PathVariable UUID stockTakeId) {
        return ResponseEntity.ok(ApiResponse.success(stockTakeService.cancel(stockTakeId)));
    }
}
