/**
 * @file EmployeeAccessService.java
 * @description 員工資料範圍守衛服務 / Employee data-scope guard service
 * @description_en Validates whether the current authenticated user can access or operate employee-scoped data
 * @description_zh 驗證目前登入者是否能讀取或操作指定員工範圍資料
 */
package com.enterprise.organization.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.security.SecurityUtils;
import com.enterprise.organization.entity.Employee;
import com.enterprise.organization.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeAccessService {

    private static final Set<String> PEOPLE_SCOPE_ROLES = Set.of(
            "SUPER_ADMIN",
            "AREA_MANAGER",
            "STORE_MANAGER",
            "SHIFT_MANAGER"
    );

    private final EmployeeRepository employeeRepository;

    // ========================================
    // 目前員工解析 / Current Employee Resolution
    // ========================================
    @Transactional(readOnly = true)
    public UUID requireCurrentEmployeeId() {
        UUID currentUserId = resolveCurrentUserId();
        return employeeRepository.findByUserId(currentUserId)
                .map(Employee::getId)
                .orElseThrow(() -> new BusinessException(403,
                        "Current user is not linked to an employee profile"));
    }

    @Transactional(readOnly = true)
    public String requireCurrentEmployeeIdString() {
        return requireCurrentEmployeeId().toString();
    }

    // ========================================
    // 讀取範圍 / Read Scope
    // ========================================
    @Transactional(readOnly = true)
    public void requireReadableEmployee(UUID employeeId) {
        if (hasPeopleScopeRole()) {
            return;
        }
        requireOwnEmployee(employeeId);
    }

    @Transactional(readOnly = true)
    public void requireReadableEmployee(String employeeId) {
        requireReadableEmployee(parseEmployeeId(employeeId));
    }

    @Transactional(readOnly = true)
    public void requirePeopleScope() {
        if (!hasPeopleScopeRole()) {
            throw new BusinessException(403, "People data scope is required");
        }
    }

    // ========================================
    // 操作範圍 / Operation Scope
    // ========================================
    @Transactional(readOnly = true)
    public void requireOperableEmployee(UUID employeeId) {
        if (isSuperAdmin()) {
            return;
        }
        requireOwnEmployee(employeeId);
    }

    @Transactional(readOnly = true)
    public void requireOperableEmployee(String employeeId) {
        requireOperableEmployee(parseEmployeeId(employeeId));
    }

    @Transactional(readOnly = true)
    public void requireOwnEmployee(UUID employeeId) {
        UUID currentEmployeeId = requireCurrentEmployeeId();
        if (!currentEmployeeId.equals(employeeId)) {
            throw new BusinessException(403, "Employee data scope denied");
        }
    }

    private UUID resolveCurrentUserId() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            throw new BusinessException(401, "Authentication is required");
        }
        return UUID.fromString(currentUserId);
    }

    private UUID parseEmployeeId(String employeeId) {
        if (!StringUtils.hasText(employeeId)) {
            throw new BusinessException(400, "Employee id is required");
        }
        return UUID.fromString(employeeId);
    }

    private boolean hasPeopleScopeRole() {
        String role = SecurityUtils.getCurrentRole();
        return PEOPLE_SCOPE_ROLES.contains(role);
    }

    private boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(SecurityUtils.getCurrentRole());
    }
}
