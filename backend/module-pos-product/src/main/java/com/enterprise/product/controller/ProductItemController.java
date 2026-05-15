/**
 * @file ProductItemController.java
 * @description 商品 API 控制器 / Product item REST controller
 * @description_en Exposes CRUD and search endpoints for product management
 * @description_zh 提供商品 CRUD 與搜尋的 RESTful API
 */
package com.enterprise.product.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.common.dto.PageResponse;
import com.enterprise.product.dto.ProductItemRequest;
import com.enterprise.product.dto.ProductItemResponse;
import com.enterprise.product.service.ProductItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/products")
@RequiredArgsConstructor
public class ProductItemController {

    private final ProductItemService productService;

    // ========================================
    // 查詢 / Query
    // ========================================

    @GetMapping
    @RequirePermission("pos:product:read")
    public ApiResponse<PageResponse<ProductItemResponse>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String keyword) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        if (keyword != null && !keyword.isBlank()) {
            return ApiResponse.success(productService.search(keyword.trim(), pageable));
        }
        if (categoryId != null) {
            return ApiResponse.success(productService.findByCategory(categoryId, pageable));
        }
        return ApiResponse.success(productService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @RequirePermission("pos:product:read")
    public ApiResponse<ProductItemResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(productService.findById(id));
    }

    @GetMapping("/by-sku/{sku}")
    @RequirePermission("pos:product:read")
    public ApiResponse<ProductItemResponse> getBySku(@PathVariable String sku) {
        return ApiResponse.success(productService.findBySku(sku));
    }

    // ========================================
    // 新增/修改/刪除 / Create / Update / Delete
    // ========================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission("pos:product:manage")
    @Auditable(module = "pos-product", action = "create")
    public ApiResponse<ProductItemResponse> create(@Valid @RequestBody ProductItemRequest request) {
        return ApiResponse.success(productService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("pos:product:manage")
    @Auditable(module = "pos-product", action = "update")
    public ApiResponse<ProductItemResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody ProductItemRequest request) {
        return ApiResponse.success(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("pos:product:manage")
    @Auditable(module = "pos-product", action = "delete")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ApiResponse.success(null);
    }
}
