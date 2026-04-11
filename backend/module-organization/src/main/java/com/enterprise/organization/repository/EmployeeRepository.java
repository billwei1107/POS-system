package com.enterprise.organization.repository;

import com.enterprise.organization.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    List<Employee> findByCompanyId(UUID companyId);
    List<Employee> findByDepartmentId(UUID departmentId);
    Optional<Employee> findByEmployeeNo(String employeeNo);
    Optional<Employee> findByUserId(UUID userId);
}
