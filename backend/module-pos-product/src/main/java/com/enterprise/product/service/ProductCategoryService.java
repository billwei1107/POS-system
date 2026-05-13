/**
 * @file ProductCategoryService.java
 * @description 商品分類業務邏輯層 / Product category service
 * @description_en Handles category CRUD with hierarchical tree support
 * @description_zh 商品分類 CRUD，支援父子樹狀結構
 */
package com.enterprise.product.service;

import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.product.dto.CategoryRequest;
import com.enterprise.product.dto.CategoryResponse;
import com.enterprise.product.entity.ProductCategory;
import com.enterprise.product.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    // ========================================
    // 查詢 / Query
    // ========================================

    public List<CategoryResponse> findAllActive() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream().map(CategoryResponse::from).collect(Collectors.toList());
    }

    public List<CategoryResponse> findRootCategories() {
        return categoryRepository.findByParentIdIsNullAndActiveTrueOrderBySortOrderAsc()
                .stream().map(CategoryResponse::from).collect(Collectors.toList());
    }

    public List<CategoryResponse> findChildren(UUID parentId) {
        return categoryRepository.findByParentIdAndActiveTrueOrderBySortOrderAsc(parentId)
                .stream().map(CategoryResponse::from).collect(Collectors.toList());
    }

    public CategoryResponse findById(UUID id) {
        return CategoryResponse.from(getOrThrow(id));
    }

    // ========================================
    // 新增/修改/刪除 / Create / Update / Delete
    // ========================================

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        ProductCategory entity = new ProductCategory();
        applyRequest(entity, request);
        return CategoryResponse.from(categoryRepository.save(entity));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        ProductCategory entity = getOrThrow(id);
        applyRequest(entity, request);
        return CategoryResponse.from(categoryRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ProductCategory entity = getOrThrow(id);
        categoryRepository.delete(entity);
    }

    // ========================================
    // 私有工具 / Private helpers
    // ========================================

    private ProductCategory getOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory not found: " + id));
    }

    private void applyRequest(ProductCategory entity, CategoryRequest request) {
        entity.setName(request.getName());
        entity.setParentId(request.getParentId());
        entity.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        entity.setImageUrl(request.getImageUrl());
        entity.setDisplayColor(request.getDisplayColor());
        entity.setActive(request.getActive() != null ? request.getActive() : true);
    }
}
