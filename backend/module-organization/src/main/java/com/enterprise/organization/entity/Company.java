package com.enterprise.organization.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "org_companies")
@Data
@EqualsAndHashCode(callSuper = true)
public class Company extends BaseEntity {
    private String name;
    private String code;
    private String address;
    private String phone;
    private String email;
    private String legalPerson;
}
