/**
 * @file PriceRuleController.java
 * @description 價格規則 API 控制器 / Price rule REST controller
 * @description_en Exposes CRUD endpoints for store-aware product price rules
 * @description_zh 提供支援門店範圍的商品價格規則 CRUD API
 */
package com.enterprise.product.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.service.StoreAccessService;
import com.enterprise.product.dto.PriceRuleRequest;
import com.enterprise.product.dto.PriceRuleResponse;
import com.enterprise.product.service.PriceRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/price-rules")
@RequiredArgsConstructor
public class PriceRuleController {

    private final PriceRuleService priceRuleService;
    private final StoreAccessService storeAccessService;

    // ========================================
    // 查詢 / Query
    // ========================================
    @GetMapping
    @RequirePermission("pos:product:read")
    public ApiResponse<List<PriceRuleResponse>> list(
            @RequestParam UUID itemId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(defaultValue = "false") boolean globalOnly) {
        if (storeId != null) {
            storeAccessService.requireReadableStore(storeId);
        }
        List<PriceRuleResponse> rules = globalOnly
                ? priceRuleService.listGlobalByItem(itemId)
                : priceRuleService.listByItem(itemId, storeId);
        return ApiResponse.success(rules);
    }

    // ========================================
    // 新增/修改/停用 / Create / Update / Deactivate
    // ========================================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission("pos:product:manage")
    @Auditable(module = "pos-price-rule", action = "create")
    public ApiResponse<PriceRuleResponse> create(@Valid @RequestBody PriceRuleRequest request) {
        requireRequestStoreOperable(request.storeId());
        return ApiResponse.success(priceRuleService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("pos:product:manage")
    @Auditable(module = "pos-price-rule", action = "update")
    public ApiResponse<PriceRuleResponse> update(@PathVariable UUID id,
                                                 @Valid @RequestBody PriceRuleRequest request) {
        requireExistingAndRequestedStoresOperable(id, request.storeId());
        return ApiResponse.success(priceRuleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("pos:product:manage")
    @Auditable(module = "pos-price-rule", action = "deactivate")
    public ApiResponse<Void> deactivate(@PathVariable UUID id) {
        UUID storeId = priceRuleService.findStoreId(id);
        requireRequestStoreOperable(storeId);
        priceRuleService.deactivate(id);
        return ApiResponse.success(null);
    }

    private void requireExistingAndRequestedStoresOperable(UUID id, UUID requestedStoreId) {
        UUID existingStoreId = priceRuleService.findStoreId(id);
        requireRequestStoreOperable(existingStoreId);
        if (requestedStoreId != null && !requestedStoreId.equals(existingStoreId)) {
            storeAccessService.requireOperableStore(requestedStoreId);
        }
    }

    private void requireRequestStoreOperable(UUID storeId) {
        if (storeId != null) {
            storeAccessService.requireOperableStore(storeId);
        }
    }
}
