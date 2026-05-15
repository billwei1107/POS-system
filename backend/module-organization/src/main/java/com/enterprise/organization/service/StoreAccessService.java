/**
 * @file StoreAccessService.java
 * @description 門店資料範圍守衛服務 / Store data-scope guard service
 * @description_en Validates whether the current authenticated user can access or operate store-scoped data
 * @description_zh 驗證目前登入者是否能讀取或操作指定門店範圍資料
 */
package com.enterprise.organization.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.security.SecurityUtils;
import com.enterprise.organization.entity.Employee;
import com.enterprise.organization.repository.EmployeeRepository;
import com.enterprise.organization.repository.StoreEmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreAccessService {

    private static final Set<String> GLOBAL_STORE_ROLES = Set.of(
            "SUPER_ADMIN",
            "AREA_MANAGER"
    );

    private final EmployeeRepository employeeRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;

    // ========================================
    // 門店讀取範圍 / Store Read Scope
    // ========================================
    @Transactional(readOnly = true)
    public void requireReadableStore(UUID storeId) {
        requireAssignedStoreOrGlobal(storeId);
    }

    // ========================================
    // 門店操作範圍 / Store Operation Scope
    // ========================================
    @Transactional(readOnly = true)
    public void requireOperableStore(UUID storeId) {
        requireAssignedStoreOrGlobal(storeId);
    }

    @Transactional(readOnly = true)
    public void requireAnyReadableStore(UUID firstStoreId, UUID secondStoreId) {
        if (hasGlobalStoreRole()) {
            return;
        }
        UUID employeeId = requireCurrentEmployeeId();
        boolean allowed = isAssignedToStore(employeeId, firstStoreId) || isAssignedToStore(employeeId, secondStoreId);
        if (!allowed) {
            throw new BusinessException(403, "Store data scope denied");
        }
    }

    @Transactional(readOnly = true)
    public void requireAllOperableStores(UUID firstStoreId, UUID secondStoreId) {
        requireOperableStore(firstStoreId);
        requireOperableStore(secondStoreId);
    }

    private void requireAssignedStoreOrGlobal(UUID storeId) {
        if (hasGlobalStoreRole()) {
            return;
        }
        UUID employeeId = requireCurrentEmployeeId();
        if (!isAssignedToStore(employeeId, storeId)) {
            throw new BusinessException(403, "Store data scope denied");
        }
    }

    private UUID requireCurrentEmployeeId() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            throw new BusinessException(401, "Authentication is required");
        }
        UUID userId = UUID.fromString(currentUserId);
        return employeeRepository.findByUserId(userId)
                .map(Employee::getId)
                .orElseThrow(() -> new BusinessException(403,
                        "Current user is not linked to an employee profile"));
    }

    private boolean isAssignedToStore(UUID employeeId, UUID storeId) {
        return storeEmployeeRepository.findByStoreIdAndEmployeeId(storeId, employeeId)
                .filter(assignment -> Boolean.TRUE.equals(assignment.getActive()))
                .isPresent();
    }

    private boolean hasGlobalStoreRole() {
        return GLOBAL_STORE_ROLES.contains(SecurityUtils.getCurrentRole());
    }
}
