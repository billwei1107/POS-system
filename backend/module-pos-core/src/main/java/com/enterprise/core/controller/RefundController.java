/**
 * @file RefundController.java
 * @description 退款管理 Controller / Refund management controller
 * @description_en REST endpoints for initiating and completing order refunds
 * @description_zh 退款申請與完成的 REST 端點
 */
package com.enterprise.core.controller;

import com.enterprise.common.dto.ApiResponse;
import com.enterprise.core.dto.request.CreateRefundRequest;
import com.enterprise.core.entity.OrderRefund;
import com.enterprise.core.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    // ========================================
    // 建立退款申請 / Create refund
    // ========================================
    @PostMapping
    public ResponseEntity<ApiResponse<OrderRefund>> createRefund(
            @Valid @RequestBody CreateRefundRequest req) {
        return ResponseEntity.ok(ApiResponse.success(refundService.createRefund(req)));
    }

    // ========================================
    // 完成退款 / Complete refund
    // ========================================
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<OrderRefund>> completeRefund(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(refundService.completeRefund(id)));
    }
}
