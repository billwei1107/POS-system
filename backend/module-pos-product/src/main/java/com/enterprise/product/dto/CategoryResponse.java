/**
 * @file CategoryResponse.java
 * @description 商品分類回應 DTO / Category response DTO
 */
package com.enterprise.product.dto;

import com.enterprise.product.entity.ProductCategory;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CategoryResponse {

    private UUID id;
    private String name;
    private UUID parentId;
    private Integer sortOrder;
    private String imageUrl;
    private String displayColor;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CategoryResponse from(ProductCategory entity) {
        CategoryResponse dto = new CategoryResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setParentId(entity.getParentId());
        dto.setSortOrder(entity.getSortOrder());
        dto.setImageUrl(entity.getImageUrl());
        dto.setDisplayColor(entity.getDisplayColor());
        dto.setActive(entity.getActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
