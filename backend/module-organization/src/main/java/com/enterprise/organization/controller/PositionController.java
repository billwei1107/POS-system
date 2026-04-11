package com.enterprise.organization.controller;

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
    public ApiResponse<Position> create(@RequestBody Position position) {
        return ApiResponse.success(positionService.create(position));
    }

    @PutMapping("/{id}")
    public ApiResponse<Position> update(@PathVariable UUID id, @RequestBody Position position) {
        return ApiResponse.success(positionService.update(id, position));
    }

    @GetMapping("/{id}")
    public ApiResponse<Position> getById(@PathVariable UUID id) {
        return ApiResponse.success(positionService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<Position>> listAll() {
        return ApiResponse.success(positionService.listAll());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        positionService.delete(id);
        return ApiResponse.success(null);
    }
}
