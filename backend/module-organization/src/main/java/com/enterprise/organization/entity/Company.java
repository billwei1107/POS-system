package com.enterprise.organization.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "org_companies")
@SQLDelete(sql = "UPDATE org_companies SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
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
