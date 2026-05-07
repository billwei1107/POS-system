/**
 * @file CategoryRequest.java
 * @description 商品分類新增/修改請求 DTO / Category create/update request DTO
 */
package com.enterprise.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoryRequest {

    @NotBlank(message = "分類名稱不得為空")
    @Size(max = 100)
    private String name;

    private UUID parentId;

    private Integer sortOrder = 0;

    @Size(max = 500)
    private String imageUrl;

    @Size(max = 20)
    private String displayColor;

    private Boolean active = true;
}
