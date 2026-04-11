package com.enterprise.organization.controller;

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
    public ApiResponse<Store> create(@RequestBody Store store) {
        return ApiResponse.success(storeService.create(store));
    }

    @PutMapping("/{id}")
    public ApiResponse<Store> update(@PathVariable UUID id, @RequestBody Store store) {
        return ApiResponse.success(storeService.update(id, store));
    }

    @GetMapping("/{id}")
    public ApiResponse<Store> getById(@PathVariable UUID id) {
        return ApiResponse.success(storeService.getById(id));
    }

    @GetMapping("/code/{storeCode}")
    public ApiResponse<Store> getByStoreCode(@PathVariable String storeCode) {
        return ApiResponse.success(storeService.getByStoreCode(storeCode));
    }

    @GetMapping
    public ApiResponse<List<Store>> list(@RequestParam(required = false) UUID companyId,
                                         @RequestParam(required = false) UUID regionId) {
        if (regionId != null) {
            return ApiResponse.success(storeService.listByRegion(regionId));
        }
        return ApiResponse.success(storeService.listByCompany(companyId));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        storeService.updateStatus(id, status);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        storeService.delete(id);
        return ApiResponse.success(null);
    }
}
