package com.enterprise.organization.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "org_positions")
@Data
@EqualsAndHashCode(callSuper = true)
public class Position extends BaseEntity {
    private String name;
    private String code;
    private Integer level = 0;
    private String description;
}
