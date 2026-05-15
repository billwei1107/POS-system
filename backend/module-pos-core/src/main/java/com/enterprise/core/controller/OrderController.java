/**
 * @file OrderController.java
 * @description 訂單管理 Controller / Order management controller
 * @description_en REST endpoints for POS order creation, status management and listing
 * @description_zh POS 訂單建立、狀態管理與查詢 REST 端點
 */
package com.enterprise.core.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.common.dto.PageResponse;
import com.enterprise.core.dto.request.CreateOrderRequest;
import com.enterprise.core.dto.response.OrderResponse;
import com.enterprise.core.entity.Order;
import com.enterprise.core.service.OrderService;
import com.enterprise.organization.service.StoreAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final StoreAccessService storeAccessService;

    // ========================================
    // 建立訂單 / Create order
    // ========================================
    @PostMapping
    @RequirePermission("pos:order:create")
    @Auditable(module = "pos-order", action = "create")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest req) {
        storeAccessService.requireOperableStore(req.storeId());
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(req)));
    }

    // ========================================
    // 查詢單筆 / Get order by ID
    // ========================================
    @GetMapping("/{id}")
    @RequirePermission("pos:order:read")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable UUID id) {
        storeAccessService.requireReadableStore(orderService.findStoreId(id));
        return ResponseEntity.ok(ApiResponse.success(orderService.getById(id)));
    }

    // ========================================
    // 門店訂單列表（分頁）/ List orders by store
    // ========================================
    @GetMapping
    @RequirePermission("pos:order:read")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> listByStore(
            @RequestParam UUID storeId,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        storeAccessService.requireReadableStore(storeId);
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
            orderService.listByStore(storeId, status, from, to, pageable)
        ));
    }

    // ========================================
    // 完成結帳 / Complete checkout
    // ========================================
    @PostMapping("/{id}/complete")
    @RequirePermission("pos:payment:process")
    @Auditable(module = "pos-order", action = "complete")
    public ResponseEntity<ApiResponse<OrderResponse>> completeOrder(
            @PathVariable UUID id,
            @RequestParam String payMethod,
            @RequestParam(required = false) BigDecimal tendered) {
        storeAccessService.requireOperableStore(orderService.findStoreId(id));
        return ResponseEntity.ok(ApiResponse.success(
            orderService.completeOrder(id, payMethod, tendered)
        ));
    }

    // ========================================
    // 作廢訂單 / Void order
    // ========================================
    @PostMapping("/{id}/void")
    @RequirePermission("pos:order:void")
    @Auditable(module = "pos-order", action = "void")
    public ResponseEntity<ApiResponse<OrderResponse>> voidOrder(
            @PathVariable UUID id,
            @RequestParam UUID voidedBy,
            @RequestParam(required = false) String reason) {
        storeAccessService.requireOperableStore(orderService.findStoreId(id));
        return ResponseEntity.ok(ApiResponse.success(
            orderService.voidOrder(id, voidedBy, reason)
        ));
    }
}
