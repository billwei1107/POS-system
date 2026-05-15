/**
 * @file StoreAccessServiceTest.java
 * @description 門店資料範圍守衛測試 / Store data-scope guard tests
 * @description_en Verifies store-scoped data access for assigned employees and global roles
 * @description_zh 驗證員工門店指派與全域角色的門店資料範圍控管
 */
package com.enterprise.organization.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.organization.entity.Employee;
import com.enterprise.organization.entity.StoreEmployee;
import com.enterprise.organization.repository.EmployeeRepository;
import com.enterprise.organization.repository.StoreEmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreAccessServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private StoreEmployeeRepository storeEmployeeRepository;

    private StoreAccessService storeAccessService;
    private UUID currentUserId;
    private UUID currentEmployeeId;
    private UUID assignedStoreId;
    private UUID otherStoreId;

    @BeforeEach
    void setUp() {
        storeAccessService = new StoreAccessService(employeeRepository, storeEmployeeRepository);
        currentUserId = UUID.randomUUID();
        currentEmployeeId = UUID.randomUUID();
        assignedStoreId = UUID.randomUUID();
        otherStoreId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cashierCanReadAssignedStore() {
        authenticate("CASHIER");
        mockCurrentEmployee();
        mockAssignedStore(assignedStoreId, true);

        storeAccessService.requireReadableStore(assignedStoreId);
    }

    @Test
    void cashierCannotReadOtherStore() {
        authenticate("CASHIER");
        mockCurrentEmployee();
        when(storeEmployeeRepository.findByStoreIdAndEmployeeId(otherStoreId, currentEmployeeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeAccessService.requireReadableStore(otherStoreId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Store data scope denied");
    }

    @Test
    void inactiveAssignmentCannotReadStore() {
        authenticate("CASHIER");
        mockCurrentEmployee();
        mockAssignedStore(assignedStoreId, false);

        assertThatThrownBy(() -> storeAccessService.requireReadableStore(assignedStoreId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Store data scope denied");
    }

    @Test
    void areaManagerCanReadAnyStoreWithoutEmployeeProfile() {
        authenticate("AREA_MANAGER");

        storeAccessService.requireReadableStore(otherStoreId);
    }

    private void authenticate(String role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                currentUserId,
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void mockCurrentEmployee() {
        Employee employee = new Employee();
        employee.setId(currentEmployeeId);
        employee.setUserId(currentUserId);
        when(employeeRepository.findByUserId(currentUserId)).thenReturn(Optional.of(employee));
    }

    private void mockAssignedStore(UUID storeId, boolean active) {
        StoreEmployee assignment = new StoreEmployee();
        assignment.setStoreId(storeId);
        assignment.setEmployeeId(currentEmployeeId);
        assignment.setActive(active);
        when(storeEmployeeRepository.findByStoreIdAndEmployeeId(storeId, currentEmployeeId))
                .thenReturn(Optional.of(assignment));
    }
}
