package com.enterprise.organization.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * 區域實體 / Region entity for multi-store management
 */
@Entity
@Table(name = "org_regions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"company_id", "region_code"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class Region extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "region_name", nullable = false, length = 100)
    private String regionName;

    @Column(name = "region_code", nullable = false, length = 50)
    private String regionCode;

    @Column(name = "manager_employee_id")
    private UUID managerEmployeeId;

    private String description;

    @Column(nullable = false)
    private Boolean active = true;
}
