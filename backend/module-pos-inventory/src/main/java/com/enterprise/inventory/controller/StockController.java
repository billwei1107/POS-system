/**
 * @file StockController.java
 * @description 庫存管理 Controller / Stock management REST controller
 * @description_en REST endpoints for querying and adjusting store-level inventory
 * @description_zh 提供門店庫存查詢與手動調整的 REST API
 */
package com.enterprise.inventory.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.inventory.dto.request.AdjustStockRequest;
import com.enterprise.inventory.dto.request.ReceiveStockRequest;
import com.enterprise.inventory.dto.response.StoreStockResponse;
import com.enterprise.inventory.entity.StockAlert;
import com.enterprise.inventory.entity.StockMovement;
import com.enterprise.inventory.entity.StoreStock;
import com.enterprise.inventory.repository.StockMovementRepository;
import com.enterprise.inventory.repository.StoreStockRepository;
import com.enterprise.inventory.service.StockAlertService;
import com.enterprise.inventory.service.StockDeductionService;
import com.enterprise.organization.service.StoreAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class StockController {

    private final StoreStockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final StockDeductionService deductionService;
    private final StockAlertService alertService;
    private final StoreAccessService storeAccessService;

    // ========================================
    // 查詢門店庫存列表 / List store inventory
    // ========================================
    @GetMapping("/stores/{storeId}/stock")
    @RequirePermission("pos:inventory:read")
    public ResponseEntity<ApiResponse<List<StoreStockResponse>>> listStock(@PathVariable UUID storeId) {
        storeAccessService.requireReadableStore(storeId);
        List<StoreStockResponse> data = stockRepository.findAllByStoreId(storeId)
                .stream().map(StoreStockResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ========================================
    // 查詢單一商品庫存 / Get stock for specific item
    // ========================================
    @GetMapping("/stores/{storeId}/stock/{itemId}")
    @RequirePermission("pos:inventory:read")
    public ResponseEntity<ApiResponse<StoreStockResponse>> getStock(
            @PathVariable UUID storeId, @PathVariable UUID itemId) {
        storeAccessService.requireReadableStore(storeId);
        StoreStock stock = stockRepository.findByStoreIdAndItemId(storeId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));
        return ResponseEntity.ok(ApiResponse.success(StoreStockResponse.from(stock)));
    }

    // ========================================
    // 手動調整庫存 / Manual stock adjustment
    // ========================================
    @PostMapping("/stock/adjust")
    @RequirePermission("pos:inventory:adjust")
    @Auditable(module = "inventory-stock", action = "adjust")
    public ResponseEntity<ApiResponse<String>> adjust(@Valid @RequestBody AdjustStockRequest req) {
        storeAccessService.requireOperableStore(req.storeId());
        deductionService.adjust(req.storeId(), req.itemId(), req.adjustQty(),
                req.operatedBy(), req.notes());
        return ResponseEntity.ok(ApiResponse.success("庫存調整成功"));
    }

    // ========================================
    // 進貨驗收入庫 / Receive counted inbound goods
    // ========================================
    @PostMapping("/stock/receive")
    @RequirePermission("pos:inventory:receive")
    @Auditable(module = "inventory-stock", action = "receive")
    public ResponseEntity<ApiResponse<String>> receive(@Valid @RequestBody ReceiveStockRequest req) {
        storeAccessService.requireOperableStore(req.storeId());
        UUID receiptId = UUID.randomUUID();
        List<StockDeductionService.ReceivingLine> lines = req.items().stream()
                .map(item -> new StockDeductionService.ReceivingLine(item.itemId(), item.receivedQty()))
                .toList();
        deductionService.receiveBatch(
                req.storeId(),
                lines,
                receiptId,
                "purchase_receiving",
                req.operatedBy(),
                req.notes()
        );
        return ResponseEntity.ok(ApiResponse.success("進貨驗收入庫成功：" + receiptId));
    }

    // ========================================
    // 查詢庫存異動紀錄 / Query stock movements
    // ========================================
    @GetMapping("/stores/{storeId}/items/{itemId}/movements")
    @RequirePermission("pos:inventory:read")
    public ResponseEntity<ApiResponse<List<StockMovement>>> listMovements(
            @PathVariable UUID storeId, @PathVariable UUID itemId) {
        storeAccessService.requireReadableStore(storeId);
        List<StockMovement> data = movementRepository
                .findAllByStoreIdAndItemIdOrderByCreatedAtDesc(storeId, itemId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // ========================================
    // 查詢未確認警示 / List unacknowledged alerts
    // ========================================
    @GetMapping("/stores/{storeId}/alerts")
    @RequirePermission("pos:inventory:read")
    public ResponseEntity<ApiResponse<List<StockAlert>>> listAlerts(@PathVariable UUID storeId) {
        storeAccessService.requireReadableStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(alertService.listUnacknowledged(storeId)));
    }

    // ========================================
    // 確認警示 / Acknowledge alert
    // ========================================
    @PostMapping("/alerts/{alertId}/acknowledge")
    @RequirePermission("pos:inventory:adjust")
    @Auditable(module = "inventory-alert", action = "acknowledge")
    public ResponseEntity<ApiResponse<StockAlert>> acknowledgeAlert(
            @PathVariable UUID alertId,
            @RequestParam(required = false) UUID acknowledgedBy) {
        StockAlert alert = alertService.findById(alertId);
        storeAccessService.requireOperableStore(alert.getStoreId());
        return ResponseEntity.ok(ApiResponse.success(alertService.acknowledge(alertId, acknowledgedBy)));
    }
}
