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
@Table(name = "org_employees")
@Data
@EqualsAndHashCode(callSuper = true)
public class Employee extends BaseEntity {
    
    @Column(name = "employee_no", unique = true, nullable = false)
    private String employeeNo;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    
    @Column(name = "department_id")
    private UUID departmentId;
    
    @Column(name = "position_id")
    private UUID positionId;
    
    @Column(nullable = false)
    private String name;
    
    private String phone;
    private String email;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(name = "resign_date")
    private LocalDate resignDate;
    
    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, RESIGNED, ON_LEAVE
    
    @Column(name = "emergency_contact")
    private String emergencyContact;
    
    @Column(name = "emergency_phone")
    private String emergencyPhone;
}
