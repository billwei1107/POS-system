package com.enterprise.organization.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "org_employee_contracts")
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeContract extends BaseEntity {
    
    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;
    
    @Column(nullable = false, length = 50)
    private String type; // FULL_TIME, PART_TIME, PROBATION
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "renewal_reminder")
    private Boolean renewalReminder = false;
}
