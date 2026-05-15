/**
 * @file ProductCategoryController.java
 * @description 商品分類 API 控制器 / Product category REST controller
 * @description_en Exposes CRUD endpoints for product category management
 * @description_zh 提供商品分類管理的 RESTful API
 */
package com.enterprise.product.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.product.dto.CategoryRequest;
import com.enterprise.product.dto.CategoryResponse;
import com.enterprise.product.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    // ========================================
    // 查詢 / Query
    // ========================================

    @GetMapping
    @RequirePermission("pos:product:read")
    public ApiResponse<List<CategoryResponse>> listAll() {
        return ApiResponse.success(categoryService.findAllActive());
    }

    @GetMapping("/roots")
    @RequirePermission("pos:product:read")
    public ApiResponse<List<CategoryResponse>> listRoots() {
        return ApiResponse.success(categoryService.findRootCategories());
    }

    @GetMapping("/{id}/children")
    @RequirePermission("pos:product:read")
    public ApiResponse<List<CategoryResponse>> listChildren(@PathVariable UUID id) {
        return ApiResponse.success(categoryService.findChildren(id));
    }

    @GetMapping("/{id}")
    @RequirePermission("pos:product:read")
    public ApiResponse<CategoryResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(categoryService.findById(id));
    }

    // ========================================
    // 新增/修改/刪除 / Create / Update / Delete
    // ========================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission("pos:product:manage")
    @Auditable(module = "pos-product-category", action = "create")
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success(categoryService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("pos:product:manage")
    @Auditable(module = "pos-product-category", action = "update")
    public ApiResponse<CategoryResponse> update(@PathVariable UUID id,
                                                @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("pos:product:manage")
    @Auditable(module = "pos-product-category", action = "delete")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ApiResponse.success(null);
    }
}
