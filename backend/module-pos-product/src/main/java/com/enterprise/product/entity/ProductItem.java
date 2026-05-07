/**
 * @file ProductItem.java
 * @description 商品主表實體 / Product item entity
 * @description_en Core product entity containing SKU, pricing, and configuration flags
 * @description_zh 商品主表，包含 SKU、價格、庫存追蹤、秤重等配置旗標
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
@Table(name = "pos_prod_items")
@SQLDelete(sql = "UPDATE pos_prod_items SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
public class ProductItem extends BaseEntity {

    @Column(nullable = false, length = 100, unique = true)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_id")
    private UUID categoryId;

    // item_name_snapshot 設計原則：金額一律 BigDecimal，禁止 double
    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "tax_class_id")
    private UUID taxClassId;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private UnitType unit = UnitType.PCS;

    @Column(name = "barcode_primary", length = 100)
    private String barcodePrimary;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "track_inventory", nullable = false)
    private Boolean trackInventory = false;

    @Column(nullable = false)
    private Boolean sellable = true;

    @Column(name = "weight_based", nullable = false)
    private Boolean weightBased = false;

    @Column(nullable = false)
    private Boolean active = true;

    public enum UnitType {
        PCS, KG, LB, ML, L
    }
}
