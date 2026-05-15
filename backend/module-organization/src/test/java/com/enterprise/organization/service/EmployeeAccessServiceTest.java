/**
 * @file EmployeeAccessServiceTest.java
 * @description 員工資料範圍守衛測試 / Employee data-scope guard tests
 * @description_en Verifies self-only and privileged employee data access checks
 * @description_zh 驗證一般員工僅能操作自己資料，主管以上角色可讀取人事範圍資料
 */
package com.enterprise.organization.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.organization.entity.Employee;
import com.enterprise.organization.repository.EmployeeRepository;
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
class EmployeeAccessServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeAccessService employeeAccessService;
    private UUID currentUserId;
    private UUID currentEmployeeId;
    private UUID otherEmployeeId;

    @BeforeEach
    void setUp() {
        employeeAccessService = new EmployeeAccessService(employeeRepository);
        currentUserId = UUID.randomUUID();
        currentEmployeeId = UUID.randomUUID();
        otherEmployeeId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cashierCannotOperateOtherEmployeeData() {
        authenticate("CASHIER");
        mockCurrentEmployee();

        assertThatThrownBy(() -> employeeAccessService.requireOperableEmployee(otherEmployeeId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Employee data scope denied");
    }

    @Test
    void cashierCanOperateOwnEmployeeData() {
        authenticate("CASHIER");
        mockCurrentEmployee();

        employeeAccessService.requireOperableEmployee(currentEmployeeId);
    }

    @Test
    void storeManagerCanReadOtherEmployeeData() {
        authenticate("STORE_MANAGER");

        employeeAccessService.requireReadableEmployee(otherEmployeeId);
    }

    @Test
    void superAdminCanOperateWithoutEmployeeProfile() {
        authenticate("SUPER_ADMIN");

        employeeAccessService.requireOperableEmployee(otherEmployeeId);
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
}
