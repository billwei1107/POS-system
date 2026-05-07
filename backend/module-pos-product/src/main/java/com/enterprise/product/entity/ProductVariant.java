/**
 * @file ProductVariant.java
 * @description 商品變體實體 / Product variant entity
 * @description_en Represents product variations such as size or color with optional price override
 * @description_zh 商品變體（如尺寸、顏色），可覆寫售價與成本
 */
package com.enterprise.product.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pos_prod_variants")
@SQLDelete(sql = "UPDATE pos_prod_variants SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
public class ProductVariant extends BaseEntity {

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "variant_name", nullable = false, length = 100)
    private String variantName;

    @Column(nullable = false, length = 100, unique = true)
    private String sku;

    @Column(length = 100)
    private String barcode;

    @Column(name = "price_override", precision = 12, scale = 2)
    private BigDecimal priceOverride;

    @Column(name = "cost_override", precision = 12, scale = 2)
    private BigDecimal costOverride;

    @Column(nullable = false)
    private Boolean active = true;
}
