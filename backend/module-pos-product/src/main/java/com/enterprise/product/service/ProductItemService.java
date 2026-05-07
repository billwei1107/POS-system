/**
 * @file ProductItemService.java
 * @description 商品業務邏輯層 / Product item service
 * @description_en Handles product CRUD, keyword search, and SKU uniqueness validation
 * @description_zh 商品 CRUD、關鍵字搜尋、SKU 唯一性驗證
 */
package com.enterprise.product.service;

import com.enterprise.common.dto.PageResponse;
import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.product.dto.ProductItemRequest;
import com.enterprise.product.dto.ProductItemResponse;
import com.enterprise.product.entity.ProductItem;
import com.enterprise.product.event.ProductCreatedEvent;
import com.enterprise.product.event.ProductUpdatedEvent;
import com.enterprise.product.repository.ProductItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductItemService {

    private final ProductItemRepository itemRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ========================================
    // 查詢 / Query
    // ========================================

    public PageResponse<ProductItemResponse> findAll(Pageable pageable) {
        Page<ProductItem> page = itemRepository.findByActiveTrue(pageable);
        return PageResponse.of(page.map(ProductItemResponse::from));
    }

    public PageResponse<ProductItemResponse> findByCategory(UUID categoryId, Pageable pageable) {
        Page<ProductItem> page = itemRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        return PageResponse.of(page.map(ProductItemResponse::from));
    }

    public PageResponse<ProductItemResponse> search(String keyword, Pageable pageable) {
        Page<ProductItem> page = itemRepository.searchByKeyword(keyword, pageable);
        return PageResponse.of(page.map(ProductItemResponse::from));
    }

    public ProductItemResponse findById(UUID id) {
        return ProductItemResponse.from(getOrThrow(id));
    }

    public ProductItemResponse findBySku(String sku) {
        return itemRepository.findBySku(sku)
                .map(ProductItemResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("ProductItem not found by SKU: " + sku));
    }

    // ========================================
    // 新增/修改/刪除 / Create / Update / Delete
    // ========================================

    @Transactional
    public ProductItemResponse create(ProductItemRequest request) {
        if (itemRepository.existsBySku(request.getSku())) {
            throw new BusinessException("SKU 已存在: " + request.getSku());
        }
        ProductItem entity = new ProductItem();
        applyRequest(entity, request);
        ProductItem saved = itemRepository.save(entity);
        eventPublisher.publishEvent(new ProductCreatedEvent(this, saved.getId(), saved.getSku()));
        return ProductItemResponse.from(saved);
    }

    @Transactional
    public ProductItemResponse update(UUID id, ProductItemRequest request) {
        ProductItem entity = getOrThrow(id);
        if (!entity.getSku().equals(request.getSku()) && itemRepository.existsBySku(request.getSku())) {
            throw new BusinessException("SKU 已存在: " + request.getSku());
        }
        applyRequest(entity, request);
        ProductItem saved = itemRepository.save(entity);
        eventPublisher.publishEvent(new ProductUpdatedEvent(this, saved.getId(), saved.getSku()));
        return ProductItemResponse.from(saved);
    }

    @Transactional
    public void delete(UUID id) {
        ProductItem entity = getOrThrow(id);
        itemRepository.delete(entity);
    }

    // ========================================
    // 私有工具 / Private helpers
    // ========================================

    private ProductItem getOrThrow(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductItem not found: " + id));
    }

    private void applyRequest(ProductItem entity, ProductItemRequest request) {
        entity.setSku(request.getSku());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setCategoryId(request.getCategoryId());
        entity.setBasePrice(request.getBasePrice());
        entity.setCostPrice(request.getCostPrice());
        entity.setTaxClassId(request.getTaxClassId());
        entity.setUnit(request.getUnit() != null ? request.getUnit() : ProductItem.UnitType.PCS);
        entity.setBarcodePrimary(request.getBarcodePrimary());
        entity.setImageUrl(request.getImageUrl());
        entity.setTrackInventory(request.getTrackInventory() != null ? request.getTrackInventory() : false);
        entity.setSellable(request.getSellable() != null ? request.getSellable() : true);
        entity.setWeightBased(request.getWeightBased() != null ? request.getWeightBased() : false);
        entity.setActive(request.getActive() != null ? request.getActive() : true);
    }
}
