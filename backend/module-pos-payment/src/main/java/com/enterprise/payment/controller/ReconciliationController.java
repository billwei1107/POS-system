/**
 * @file ReconciliationController.java
 * @description 對帳 Controller / Reconciliation controller
 * @description_en REST endpoints for generating daily reconciliation and confirming settlements
 * @description_zh 產生每日對帳記錄與確認結算的 REST 端點
 */
package com.enterprise.payment.controller;

import com.enterprise.common.dto.ApiResponse;
import com.enterprise.payment.entity.Reconciliation;
import com.enterprise.payment.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    // ========================================
    // 產生每日對帳 / Generate daily reconciliation
    // ========================================
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<Reconciliation>>> generate(
            @RequestParam UUID storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(
            reconciliationService.generateDaily(storeId, date)
        ));
    }

    // ========================================
    // 查詢對帳記錄 / Query reconciliation
    // ========================================
    @GetMapping
    public ResponseEntity<ApiResponse<List<Reconciliation>>> list(
            @RequestParam UUID storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(
            reconciliationService.listByStoreAndDate(storeId, date)
        ));
    }

    // ========================================
    // 確認對帳 / Confirm reconciliation
    // ========================================
    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<Reconciliation>> confirm(
            @PathVariable UUID id,
            @RequestParam BigDecimal gatewayAmount,
            @RequestParam UUID reconciledBy) {
        return ResponseEntity.ok(ApiResponse.success(
            reconciliationService.confirm(id, gatewayAmount, reconciledBy)
        ));
    }
}
