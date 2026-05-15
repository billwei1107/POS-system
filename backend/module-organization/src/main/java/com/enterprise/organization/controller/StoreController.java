package com.enterprise.organization.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.entity.Store;
import com.enterprise.organization.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pos.organization.multi-store", havingValue = "true")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-store", action = "create")
    public ApiResponse<Store> create(@RequestBody Store store) {
        return ApiResponse.success(storeService.create(store));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-store", action = "update")
    public ApiResponse<Store> update(@PathVariable UUID id, @RequestBody Store store) {
        return ApiResponse.success(storeService.update(id, store));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:organization:read")
    public ApiResponse<Store> getById(@PathVariable UUID id) {
        return ApiResponse.success(storeService.getById(id));
    }

    @GetMapping("/code/{storeCode}")
    @RequirePermission("system:organization:read")
    public ApiResponse<Store> getByStoreCode(@PathVariable String storeCode) {
        return ApiResponse.success(storeService.getByStoreCode(storeCode));
    }

    @GetMapping
    @RequirePermission("system:organization:read")
    public ApiResponse<List<Store>> list(@RequestParam(required = false) UUID companyId,
                                         @RequestParam(required = false) UUID regionId) {
        if (regionId != null) {
            return ApiResponse.success(storeService.listByRegion(regionId));
        }
        if (companyId == null) {
            return ApiResponse.success(storeService.listAll());
        }
        return ApiResponse.success(storeService.listByCompany(companyId));
    }

    @PatchMapping("/{id}/status")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-store", action = "update-status")
    public ApiResponse<Void> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        storeService.updateStatus(id, status);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-store", action = "delete")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        storeService.delete(id);
        return ApiResponse.success(null);
    }
}
