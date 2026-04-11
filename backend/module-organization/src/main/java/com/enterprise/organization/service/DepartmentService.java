package com.enterprise.organization.service;

import com.enterprise.organization.entity.Department;
import java.util.List;
import java.util.UUID;

public interface DepartmentService {
    Department create(Department department);
    Department update(UUID id, Department department);
    Department getById(UUID id);
    List<Department> listByCompany(UUID companyId);
    void delete(UUID id);
    
    java.util.List<com.enterprise.organization.dto.DepartmentTreeDTO> getDepartmentTree(UUID companyId);
}
