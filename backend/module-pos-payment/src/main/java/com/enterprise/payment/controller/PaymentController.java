/**
 * @file PaymentController.java
 * @description 支付管理 Controller / Payment management controller
 * @description_en REST endpoints for processing payments, querying transactions, and managing pay methods
 * @description_zh 支付處理、交易查詢與支付方式管理的 REST 端點
 */
package com.enterprise.payment.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.payment.dto.request.CreateGatewayConfigRequest;
import com.enterprise.payment.dto.request.CreatePayMethodRequest;
import com.enterprise.payment.dto.request.ProcessPaymentRequest;
import com.enterprise.payment.dto.request.UpdateGatewayConfigRequest;
import com.enterprise.payment.dto.response.GatewayConfigResponse;
import com.enterprise.payment.dto.response.PaymentTransactionResponse;
import com.enterprise.payment.entity.PayMethod;
import com.enterprise.payment.service.GatewayConfigService;
import com.enterprise.payment.service.PayMethodService;
import com.enterprise.payment.service.PaymentService;
import com.enterprise.core.service.OrderService;
import com.enterprise.organization.service.StoreAccessService;
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
    private final GatewayConfigService gatewayConfigService;
    private final OrderService orderService;
    private final StoreAccessService storeAccessService;

    // ========================================
    // 發起支付 / Process payment
    // ========================================
    @PostMapping
    @RequirePermission("pos:payment:process")
    @Auditable(module = "pos-payment", action = "process")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> process(
            @Valid @RequestBody ProcessPaymentRequest req) {
        storeAccessService.requireAllOperableStores(req.storeId(), payMethodService.findStoreId(req.payMethodId()));
        return ResponseEntity.ok(ApiResponse.success(paymentService.processPayment(req)));
    }

    // ========================================
    // 查詢訂單付款記錄 / Get transactions by order
    // ========================================
    @GetMapping("/orders/{orderId}")
    @RequirePermission("pos:payment:read")
    public ResponseEntity<ApiResponse<List<PaymentTransactionResponse>>> getByOrder(
            @PathVariable UUID orderId) {
        storeAccessService.requireReadableStore(orderService.findStoreId(orderId));
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByOrder(orderId)));
    }

    // ========================================
    // 查詢門店支付方式 / List pay methods by store
    // ========================================
    @GetMapping("/methods")
    @RequirePermission("pos:payment:read")
    public ResponseEntity<ApiResponse<List<PayMethod>>> listMethods(@RequestParam UUID storeId) {
        storeAccessService.requireReadableStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(payMethodService.listByStore(storeId)));
    }

    // ========================================
    // 建立支付方式 / Create pay method
    // ========================================
    @PostMapping("/methods")
    @RequirePermission("pos:payment:method-manage")
    @Auditable(module = "pos-pay-method", action = "create")
    public ResponseEntity<ApiResponse<PayMethod>> createMethod(
            @Valid @RequestBody CreatePayMethodRequest req) {
        storeAccessService.requireOperableStore(req.storeId());
        return ResponseEntity.ok(ApiResponse.success(payMethodService.create(req)));
    }

    // ========================================
    // 停用支付方式 / Deactivate pay method
    // ========================================
    @DeleteMapping("/methods/{id}")
    @RequirePermission("pos:payment:method-manage")
    @Auditable(module = "pos-pay-method", action = "deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateMethod(@PathVariable UUID id) {
        storeAccessService.requireOperableStore(payMethodService.findStoreId(id));
        payMethodService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ========================================
    // 查詢門店閘道設定 / List gateway configs by store
    // ========================================
    @GetMapping("/gateways")
    @RequirePermission("pos:payment:read")
    public ResponseEntity<ApiResponse<List<GatewayConfigResponse>>> listGateways(@RequestParam UUID storeId) {
        storeAccessService.requireReadableStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(gatewayConfigService.listByStore(storeId)));
    }

    // ========================================
    // 建立閘道設定 / Create gateway config
    // ========================================
    @PostMapping("/gateways")
    @RequirePermission("pos:payment:method-manage")
    @Auditable(module = "pos-gateway-config", action = "create")
    public ResponseEntity<ApiResponse<GatewayConfigResponse>> createGateway(
            @Valid @RequestBody CreateGatewayConfigRequest req) {
        storeAccessService.requireOperableStore(req.storeId());
        return ResponseEntity.ok(ApiResponse.success(gatewayConfigService.create(req)));
    }

    // ========================================
    // 更新閘道設定 / Update gateway config
    // ========================================
    @PutMapping("/gateways/{id}")
    @RequirePermission("pos:payment:method-manage")
    @Auditable(module = "pos-gateway-config", action = "update")
    public ResponseEntity<ApiResponse<GatewayConfigResponse>> updateGateway(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGatewayConfigRequest req) {
        storeAccessService.requireOperableStore(gatewayConfigService.findStoreId(id));
        return ResponseEntity.ok(ApiResponse.success(gatewayConfigService.update(id, req)));
    }

    // ========================================
    // 停用閘道設定 / Deactivate gateway config
    // ========================================
    @DeleteMapping("/gateways/{id}")
    @RequirePermission("pos:payment:method-manage")
    @Auditable(module = "pos-gateway-config", action = "deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateGateway(@PathVariable UUID id) {
        storeAccessService.requireOperableStore(gatewayConfigService.findStoreId(id));
        gatewayConfigService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
