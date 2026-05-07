/**
 * @file ModifierGroup.java
 * @description 客製化群組實體 / Modifier group entity
 * @description_en Groups related modifier options (e.g. "Sugar Level", "Ice Level")
 * @description_zh 客製化選項群組（如糖度、冰塊選擇）
 */
package com.enterprise.product.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "pos_prod_modifier_groups")
@SQLDelete(sql = "UPDATE pos_prod_modifier_groups SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
public class ModifierGroup extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "min_select", nullable = false)
    private Integer minSelect = 0;

    @Column(name = "max_select", nullable = false)
    private Integer maxSelect = 1;

    @Column(nullable = false)
    private Boolean required = false;
}
