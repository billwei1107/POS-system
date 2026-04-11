package com.enterprise.organization.repository;

import com.enterprise.organization.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findByCompanyId(UUID companyId);
    List<Department> findByCompanyIdAndParentId(UUID companyId, UUID parentId);
}
