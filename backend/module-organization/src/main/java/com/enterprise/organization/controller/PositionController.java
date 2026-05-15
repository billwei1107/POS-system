package com.enterprise.organization.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.entity.Position;
import com.enterprise.organization.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-position", action = "create")
    public ApiResponse<Position> create(@RequestBody Position position) {
        return ApiResponse.success(positionService.create(position));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-position", action = "update")
    public ApiResponse<Position> update(@PathVariable UUID id, @RequestBody Position position) {
        return ApiResponse.success(positionService.update(id, position));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:organization:read")
    public ApiResponse<Position> getById(@PathVariable UUID id) {
        return ApiResponse.success(positionService.getById(id));
    }

    @GetMapping
    @RequirePermission("system:organization:read")
    public ApiResponse<List<Position>> listAll() {
        return ApiResponse.success(positionService.listAll());
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-position", action = "delete")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        positionService.delete(id);
        return ApiResponse.success(null);
    }
}
