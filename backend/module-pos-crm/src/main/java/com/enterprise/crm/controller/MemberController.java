/**
 * @file MemberController.java
 * @description POS 會員 API 控制器 / POS member REST controller
 * @description_en Exposes member lookup, quick registration and point ledger endpoints
 * @description_zh 提供會員查詢、快速註冊與點數帳查詢 API
 */
package com.enterprise.crm.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.crm.dto.MemberRequest;
import com.enterprise.crm.dto.MemberResponse;
import com.enterprise.crm.dto.PointAdjustmentRequest;
import com.enterprise.crm.dto.PointLedgerResponse;
import com.enterprise.crm.dto.PointRedemptionRequest;
import com.enterprise.crm.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // ========================================
    // 查詢 / Query
    // ========================================
    @GetMapping("/search")
    @RequirePermission("pos:member:read")
    public ApiResponse<List<MemberResponse>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(memberService.search(query, limit));
    }

    @GetMapping("/{id}")
    @RequirePermission("pos:member:read")
    public ApiResponse<MemberResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(memberService.findById(id));
    }

    @GetMapping("/{id}/points")
    @RequirePermission("pos:member:read")
    public ApiResponse<List<PointLedgerResponse>> listPointLedgers(@PathVariable UUID id) {
        return ApiResponse.success(memberService.listPointLedgers(id));
    }

    // ========================================
    // 快速註冊 / Quick registration
    // ========================================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission("pos:member:operate")
    @Auditable(module = "pos-member", action = "create")
    public ApiResponse<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        return ApiResponse.success(memberService.create(request));
    }

    // ========================================
    // 點數異動 / Point mutations
    // ========================================
    @PostMapping("/{id}/points/redemptions")
    @RequirePermission("pos:member:operate")
    @Auditable(module = "pos-member", action = "redeem-points")
    public ApiResponse<PointLedgerResponse> redeemPoints(
            @PathVariable UUID id,
            @Valid @RequestBody PointRedemptionRequest request) {
        return ApiResponse.success(memberService.redeemPoints(id, request));
    }

    @PostMapping("/{id}/points/adjustments")
    @RequirePermission("pos:member:manage")
    @Auditable(module = "pos-member", action = "adjust-points")
    public ApiResponse<PointLedgerResponse> adjustPoints(
            @PathVariable UUID id,
            @Valid @RequestBody PointAdjustmentRequest request) {
        return ApiResponse.success(memberService.adjustPoints(id, request));
    }
}
