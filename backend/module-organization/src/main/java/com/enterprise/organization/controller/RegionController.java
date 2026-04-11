package com.enterprise.organization.controller;

import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.entity.Region;
import com.enterprise.organization.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pos.organization.multi-region", havingValue = "true")
public class RegionController {

    private final RegionService regionService;

    @PostMapping
    public ApiResponse<Region> create(@RequestBody Region region) {
        return ApiResponse.success(regionService.create(region));
    }

    @PutMapping("/{id}")
    public ApiResponse<Region> update(@PathVariable UUID id, @RequestBody Region region) {
        return ApiResponse.success(regionService.update(id, region));
    }

    @GetMapping("/{id}")
    public ApiResponse<Region> getById(@PathVariable UUID id) {
        return ApiResponse.success(regionService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<Region>> listByCompany(@RequestParam UUID companyId) {
        return ApiResponse.success(regionService.listByCompany(companyId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        regionService.delete(id);
        return ApiResponse.success(null);
    }
}
