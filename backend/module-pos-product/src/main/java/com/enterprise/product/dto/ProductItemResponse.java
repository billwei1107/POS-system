/**
 * @file ProductItemResponse.java
 * @description 商品回應 DTO / Product item response DTO
 */
package com.enterprise.product.dto;

import com.enterprise.product.entity.ProductItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductItemResponse {

    private UUID id;
    private String sku;
    private String name;
    private String description;
    private UUID categoryId;
    private BigDecimal basePrice;
    private BigDecimal costPrice;
    private UUID taxClassId;
    private String unit;
    private String barcodePrimary;
    private String imageUrl;
    private Boolean trackInventory;
    private Boolean sellable;
    private Boolean weightBased;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductItemResponse from(ProductItem entity) {
        ProductItemResponse dto = new ProductItemResponse();
        dto.setId(entity.getId());
        dto.setSku(entity.getSku());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCategoryId(entity.getCategoryId());
        dto.setBasePrice(entity.getBasePrice());
        dto.setCostPrice(entity.getCostPrice());
        dto.setTaxClassId(entity.getTaxClassId());
        dto.setUnit(entity.getUnit() != null ? entity.getUnit().name() : null);
        dto.setBarcodePrimary(entity.getBarcodePrimary());
        dto.setImageUrl(entity.getImageUrl());
        dto.setTrackInventory(entity.getTrackInventory());
        dto.setSellable(entity.getSellable());
        dto.setWeightBased(entity.getWeightBased());
        dto.setActive(entity.getActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
