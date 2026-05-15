/**
 * @file RefundController.java
 * @description 退款管理 Controller / Refund management controller
 * @description_en REST endpoints for initiating and completing order refunds
 * @description_zh 退款申請與完成的 REST 端點
 */
package com.enterprise.core.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.common.dto.PageResponse;
import com.enterprise.core.dto.request.CreateRefundRequest;
import com.enterprise.core.dto.response.RefundResponse;
import com.enterprise.core.entity.OrderRefund;
import com.enterprise.core.service.RefundService;
import com.enterprise.organization.service.StoreAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final StoreAccessService storeAccessService;

    // ========================================
    // 查詢退款 / Query refunds
    // ========================================
    @GetMapping
    @RequirePermission("pos:order:read")
    public ResponseEntity<ApiResponse<PageResponse<RefundResponse>>> listRefunds(
            @RequestParam UUID storeId,
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) OrderRefund.RefundStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        storeAccessService.requireReadableStore(storeId);
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
            refundService.listByStore(storeId, orderId, status, from, to, pageable)
        ));
    }

    @GetMapping("/{id}")
    @RequirePermission("pos:order:read")
    public ResponseEntity<ApiResponse<RefundResponse>> getById(@PathVariable UUID id) {
        storeAccessService.requireReadableStore(refundService.findStoreId(id));
        return ResponseEntity.ok(ApiResponse.success(refundService.getById(id)));
    }

    // ========================================
    // 建立退款申請 / Create refund
    // ========================================
    @PostMapping
    @RequirePermission("pos:order:refund")
    @Auditable(module = "pos-refund", action = "create")
    public ResponseEntity<ApiResponse<OrderRefund>> createRefund(
            @Valid @RequestBody CreateRefundRequest req) {
        storeAccessService.requireOperableStore(refundService.findOrderStoreId(req.orderId()));
        return ResponseEntity.ok(ApiResponse.success(refundService.createRefund(req)));
    }

    // ========================================
    // 完成退款 / Complete refund
    // ========================================
    @PostMapping("/{id}/complete")
    @RequirePermission("pos:order:refund")
    @Auditable(module = "pos-refund", action = "complete")
    public ResponseEntity<ApiResponse<OrderRefund>> completeRefund(@PathVariable UUID id) {
        storeAccessService.requireOperableStore(refundService.findStoreId(id));
        return ResponseEntity.ok(ApiResponse.success(refundService.completeRefund(id)));
    }
}
