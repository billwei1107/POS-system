/**
 * @file TaxClassController.java
 * @description 稅率類別 REST 控制器 / Tax class REST controller
 * @description_en CRUD endpoints for managing store-level tax classes
 * @description_zh 門店稅率類別的 CRUD REST 端點
 */
package com.enterprise.tax.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.service.StoreAccessService;
import com.enterprise.tax.dto.request.CreateTaxClassRequest;
import com.enterprise.tax.entity.TaxClass;
import com.enterprise.tax.service.TaxClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/tax-classes")
@RequiredArgsConstructor
public class TaxClassController {

    private final TaxClassService taxClassService;
    private final StoreAccessService storeAccessService;

    @GetMapping
    @RequirePermission("pos:tax:read")
    public ResponseEntity<ApiResponse<List<TaxClass>>> list(@RequestParam UUID storeId) {
        storeAccessService.requireReadableStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(taxClassService.listByStore(storeId)));
    }

    @PostMapping
    @RequirePermission("pos:tax:manage")
    @Auditable(module = "pos-tax-class", action = "create")
    public ResponseEntity<ApiResponse<TaxClass>> create(@Valid @RequestBody CreateTaxClassRequest req) {
        storeAccessService.requireOperableStore(req.storeId());
        return ResponseEntity.ok(ApiResponse.success(taxClassService.create(req)));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("pos:tax:manage")
    @Auditable(module = "pos-tax-class", action = "deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        storeAccessService.requireOperableStore(taxClassService.findStoreId(id));
        taxClassService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
