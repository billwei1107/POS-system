/**
 * @file ProductItemRequest.java
 * @description 商品新增/修改請求 DTO / Product item create/update request DTO
 */
package com.enterprise.product.dto;

import com.enterprise.product.entity.ProductItem;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductItemRequest {

    @NotBlank(message = "SKU 不得為空")
    @Size(max = 100)
    private String sku;

    @NotBlank(message = "商品名稱不得為空")
    @Size(max = 200)
    private String name;

    private String description;

    private UUID categoryId;

    @NotNull(message = "售價不得為空")
    @DecimalMin(value = "0.00", message = "售價不得為負數")
    private BigDecimal basePrice;

    @DecimalMin(value = "0.00", message = "成本不得為負數")
    private BigDecimal costPrice;

    private UUID taxClassId;

    private ProductItem.UnitType unit = ProductItem.UnitType.PCS;

    @Size(max = 100)
    private String barcodePrimary;

    @Size(max = 500)
    private String imageUrl;

    private Boolean trackInventory = false;

    private Boolean sellable = true;

    private Boolean weightBased = false;

    private Boolean active = true;
}
