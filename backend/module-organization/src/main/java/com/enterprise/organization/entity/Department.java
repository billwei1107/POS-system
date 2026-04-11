package com.enterprise.organization.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Entity
@Table(name = "org_departments")
@Data
@EqualsAndHashCode(callSuper = true)
public class Department extends BaseEntity {
    
    @Column(name = "company_id")
    private UUID companyId;
    
    private String name;
    private String code;
    
    @Column(name = "parent_id")
    private UUID parentId;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "manager_id")
    private UUID managerId;
}
