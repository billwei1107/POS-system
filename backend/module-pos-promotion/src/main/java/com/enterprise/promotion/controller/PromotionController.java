/**
 * @file PromotionController.java
 * @description POS 促銷 API 控制器 / POS promotion REST controller
 * @description_en Exposes promotion rule management and evaluation endpoints
 * @description_zh 提供促銷規則管理與試算 API
 */
package com.enterprise.promotion.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.service.StoreAccessService;
import com.enterprise.promotion.dto.PromotionEvaluationRequest;
import com.enterprise.promotion.dto.PromotionEvaluationResponse;
import com.enterprise.promotion.dto.PromotionRuleRequest;
import com.enterprise.promotion.dto.PromotionRuleResponse;
import com.enterprise.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
    private final StoreAccessService storeAccessService;

    @GetMapping
    @RequirePermission("pos:promotion:read")
    public ApiResponse<List<PromotionRuleResponse>> list(@RequestParam UUID storeId) {
        storeAccessService.requireReadableStore(storeId);
        return ApiResponse.success(promotionService.list(storeId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission("pos:promotion:manage")
    @Auditable(module = "pos-promotion", action = "create")
    public ApiResponse<PromotionRuleResponse> create(@Valid @RequestBody PromotionRuleRequest request) {
        requireRequestStoreOperable(request.storeId());
        return ApiResponse.success(promotionService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("pos:promotion:manage")
    @Auditable(module = "pos-promotion", action = "update")
    public ApiResponse<PromotionRuleResponse> update(@PathVariable UUID id,
                                                     @Valid @RequestBody PromotionRuleRequest request) {
        requireRequestStoreOperable(promotionService.findStoreId(id));
        requireRequestStoreOperable(request.storeId());
        return ApiResponse.success(promotionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("pos:promotion:manage")
    @Auditable(module = "pos-promotion", action = "deactivate")
    public ApiResponse<Void> deactivate(@PathVariable UUID id) {
        requireRequestStoreOperable(promotionService.findStoreId(id));
        promotionService.deactivate(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/evaluate")
    @RequirePermission("pos:promotion:read")
    public ApiResponse<PromotionEvaluationResponse> evaluate(@Valid @RequestBody PromotionEvaluationRequest request) {
        storeAccessService.requireReadableStore(request.storeId());
        return ApiResponse.success(promotionService.evaluate(request));
    }

    private void requireRequestStoreOperable(UUID storeId) {
        if (storeId != null) {
            storeAccessService.requireOperableStore(storeId);
        }
    }
}
