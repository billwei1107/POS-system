package com.enterprise.organization.repository;

import com.enterprise.organization.entity.StoreEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreEmployeeRepository extends JpaRepository<StoreEmployee, UUID> {

    List<StoreEmployee> findByStoreIdAndActiveTrue(UUID storeId);

    List<StoreEmployee> findByEmployeeIdAndActiveTrue(UUID employeeId);

    Optional<StoreEmployee> findByStoreIdAndEmployeeId(UUID storeId, UUID employeeId);

    Optional<StoreEmployee> findByEmployeeIdAndIsPrimaryStoreTrue(UUID employeeId);
}
