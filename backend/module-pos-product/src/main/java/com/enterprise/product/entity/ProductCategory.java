/**
 * @file ProductCategory.java
 * @description 商品分類實體 / Product category entity
 * @description_en Represents a hierarchical product category (supports parent-child structure)
 * @description_zh 商品分類，支援父子層級結構
 */
package com.enterprise.product.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "pos_prod_categories")
@SQLDelete(sql = "UPDATE pos_prod_categories SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class ProductCategory extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "parent_id")
    private java.util.UUID parentId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "display_color", length = 20)
    private String displayColor;

    @Column(nullable = false)
    private Boolean active = true;
}
