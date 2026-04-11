package com.enterprise.organization.repository;

import com.enterprise.organization.entity.EmployeeContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeContractRepository extends JpaRepository<EmployeeContract, UUID> {
    List<EmployeeContract> findByEmployeeId(UUID employeeId);
}
