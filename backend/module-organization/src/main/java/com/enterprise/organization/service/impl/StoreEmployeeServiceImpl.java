package com.enterprise.organization.service.impl;

import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.organization.entity.StoreEmployee;
import com.enterprise.organization.repository.StoreEmployeeRepository;
import com.enterprise.organization.service.StoreEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 門店員工指派服務實作 / Store-employee assignment service implementation
 */
@Service
@RequiredArgsConstructor
public class StoreEmployeeServiceImpl implements StoreEmployeeService {

    private final StoreEmployeeRepository storeEmployeeRepository;

    @Override
    public StoreEmployee assign(StoreEmployee assignment) {
        assignment.setActive(true);
        assignment.setAssignedAt(LocalDateTime.now());
        return storeEmployeeRepository.save(assignment);
    }

    @Override
    @Transactional
    public void unassign(UUID storeId, UUID employeeId) {
        StoreEmployee se = storeEmployeeRepository.findByStoreIdAndEmployeeId(storeId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store-employee assignment not found for store: " + storeId + " employee: " + employeeId));
        se.setActive(false);
        se.setUnassignedAt(LocalDateTime.now());
        storeEmployeeRepository.save(se);
    }

    @Override
    public List<StoreEmployee> listByStore(UUID storeId) {
        return storeEmployeeRepository.findByStoreIdAndActiveTrue(storeId);
    }

    @Override
    public List<StoreEmployee> listByEmployee(UUID employeeId) {
        return storeEmployeeRepository.findByEmployeeIdAndActiveTrue(employeeId);
    }

    @Override
    public StoreEmployee getPrimaryStore(UUID employeeId) {
        return storeEmployeeRepository.findByEmployeeIdAndIsPrimaryStoreTrue(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Primary store not found for employee: " + employeeId));
    }

    @Override
    @Transactional
    public void setPrimaryStore(UUID employeeId, UUID storeId) {
        // 先取消現有主要門店 / Clear current primary
        storeEmployeeRepository.findByEmployeeIdAndIsPrimaryStoreTrue(employeeId)
                .ifPresent(existing -> {
                    existing.setIsPrimaryStore(false);
                    storeEmployeeRepository.save(existing);
                });

        // 設定新的主要門店 / Set new primary
        StoreEmployee se = storeEmployeeRepository.findByStoreIdAndEmployeeId(storeId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store-employee assignment not found for store: " + storeId + " employee: " + employeeId));
        se.setIsPrimaryStore(true);
        storeEmployeeRepository.save(se);
    }
}
