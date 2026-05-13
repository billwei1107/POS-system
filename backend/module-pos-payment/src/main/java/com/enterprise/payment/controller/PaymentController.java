/**
 * @file PaymentController.java
 * @description 支付管理 Controller / Payment management controller
 * @description_en REST endpoints for processing payments, querying transactions, and managing pay methods
 * @description_zh 支付處理、交易查詢與支付方式管理的 REST 端點
 */
package com.enterprise.payment.controller;

import com.enterprise.common.dto.ApiResponse;
import com.enterprise.payment.dto.request.CreatePayMethodRequest;
import com.enterprise.payment.dto.request.ProcessPaymentRequest;
import com.enterprise.payment.dto.response.PaymentTransactionResponse;
import com.enterprise.payment.entity.PayMethod;
import com.enterprise.payment.service.PayMethodService;
import com.enterprise.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PayMethodService payMethodService;

    // ========================================
    // 發起支付 / Process payment
    // ========================================
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> process(
            @Valid @RequestBody ProcessPaymentRequest req) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.processPayment(req)));
    }

    // ========================================
    // 查詢訂單付款記錄 / Get transactions by order
    // ========================================
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<List<PaymentTransactionResponse>>> getByOrder(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByOrder(orderId)));
    }

    // ========================================
    // 查詢門店支付方式 / List pay methods by store
    // ========================================
    @GetMapping("/methods")
    public ResponseEntity<ApiResponse<List<PayMethod>>> listMethods(@RequestParam UUID storeId) {
        return ResponseEntity.ok(ApiResponse.success(payMethodService.listByStore(storeId)));
    }

    // ========================================
    // 建立支付方式 / Create pay method
    // ========================================
    @PostMapping("/methods")
    public ResponseEntity<ApiResponse<PayMethod>> createMethod(
            @Valid @RequestBody CreatePayMethodRequest req) {
        return ResponseEntity.ok(ApiResponse.success(payMethodService.create(req)));
    }

    // ========================================
    // 停用支付方式 / Deactivate pay method
    // ========================================
    @DeleteMapping("/methods/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateMethod(@PathVariable UUID id) {
        payMethodService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
