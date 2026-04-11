package com.enterprise.organization.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 門店員工關聯實體 / Store-employee assignment entity
 */
@Entity
@Table(name = "org_store_employees", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"store_id", "employee_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreEmployee extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "is_primary_store", nullable = false)
    private Boolean isPrimaryStore = false;

    @Column(name = "role_at_store", length = 50)
    private String roleAtStore;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "unassigned_at")
    private LocalDateTime unassignedAt;

    @Column(nullable = false)
    private Boolean active = true;
}
