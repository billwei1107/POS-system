package com.enterprise.organization.service;

import com.enterprise.organization.entity.StoreEmployee;

import java.util.List;
import java.util.UUID;

/**
 * 門店員工指派服務介面 / Store-employee assignment service interface
 */
public interface StoreEmployeeService {
    StoreEmployee assign(StoreEmployee assignment);
    void unassign(UUID storeId, UUID employeeId);
    List<StoreEmployee> listByStore(UUID storeId);
    List<StoreEmployee> listByEmployee(UUID employeeId);
    StoreEmployee getPrimaryStore(UUID employeeId);
    void setPrimaryStore(UUID employeeId, UUID storeId);
}
