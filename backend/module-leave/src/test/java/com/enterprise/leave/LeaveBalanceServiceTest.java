/**
 * @file LeaveBalanceServiceTest.java
 * @description 員工餘假服務單元測試 / Leave balance service unit tests
 * @description_en Tests for balance deduction, restoration, and insufficient balance exception
 * @description_zh 驗證餘假扣除、退回與餘假不足例外的邏輯
 */
package com.enterprise.leave;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.leave.entity.LeaveBalance;
import com.enterprise.leave.entity.LeaveType;
import com.enterprise.leave.repository.LeaveBalanceRepository;
import com.enterprise.leave.repository.LeavePolicyRepository;
import com.enterprise.leave.repository.LeaveTypeRepository;
import com.enterprise.leave.service.LeaveBalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveBalanceServiceTest {

    @Mock private LeaveBalanceRepository balanceRepository;
    @Mock private LeavePolicyRepository  policyRepository;
    @Mock private LeaveTypeRepository    leaveTypeRepository;

    @InjectMocks private LeaveBalanceService balanceService;

    private UUID employeeId;
    private UUID leaveTypeId;
    private LeaveBalance balance;

    @BeforeEach
    void setUp() {
        employeeId  = UUID.randomUUID();
        leaveTypeId = UUID.randomUUID();

        balance = new LeaveBalance();
        balance.setEmployeeId(employeeId);
        balance.setLeaveTypeId(leaveTypeId);
        balance.setYear(2026);
        balance.setTotalDays(new BigDecimal("14.0"));
        balance.setUsedDays(BigDecimal.ZERO);
        balance.setRemainingDays(new BigDecimal("14.0"));
    }

    // ========================================
    // 扣除餘假成功 / Deduct balance success
    // ========================================
    @Test
    @DisplayName("扣除 3 天後 remainingDays = 11")
    void deductBalance_success() {
        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, 2026))
                .thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        balanceService.deductBalance(employeeId, leaveTypeId, new BigDecimal("3.0"));

        assertThat(balance.getUsedDays()).isEqualByComparingTo("3.0");
        assertThat(balance.getRemainingDays()).isEqualByComparingTo("11.0");
    }

    // ========================================
    // 餘假不足應拋例外 / Insufficient balance throws exception
    // ========================================
    @Test
    @DisplayName("請假 15 天但餘假只有 14 天，應拋 INSUFFICIENT_BALANCE")
    void deductBalance_insufficient() {
        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, 2026))
                .thenReturn(Optional.of(balance));

        assertThatThrownBy(() -> balanceService.deductBalance(employeeId, leaveTypeId, new BigDecimal("15.0")))
                .hasMessageContaining("INSUFFICIENT_BALANCE");
    }

    // ========================================
    // 銷假退回餘假 / Restore balance on cancellation
    // ========================================
    @Test
    @DisplayName("已扣 3 天，銷假後 remainingDays 恢復為 14")
    void restoreBalance_success() {
        balance.setUsedDays(new BigDecimal("3.0"));
        balance.setRemainingDays(new BigDecimal("11.0"));

        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, 2026))
                .thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        balanceService.restoreBalance(employeeId, leaveTypeId, new BigDecimal("3.0"));

        assertThat(balance.getRemainingDays()).isEqualByComparingTo("14.0");
    }

    // ========================================
    // 查無餘假記錄應拋例外 / Balance not found throws exception
    // ========================================
    @Test
    @DisplayName("查無餘假記錄應拋 BALANCE_NOT_FOUND")
    void deductBalance_notFound() {
        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(any(), any(), anyInt()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> balanceService.deductBalance(employeeId, leaveTypeId, new BigDecimal("1.0")))
                .hasMessageContaining("BALANCE_NOT_FOUND");
    }

    // ========================================
    // 初始化年度假別（無現有記錄，應建立）/ Init annual balance
    // ========================================
    @Test
    @DisplayName("initAnnualBalance 應依 LeavePolicy 建立正確額度")
    void initAnnualBalance_createsBalance() {
        LeaveType type = new LeaveType();
        type.setCode("ANNUAL");

        com.enterprise.leave.entity.LeavePolicy policy = new com.enterprise.leave.entity.LeavePolicy();
        policy.setLeaveTypeId(type.getId());
        policy.setMinServiceYears(0);
        policy.setMaxServiceYears(null);
        policy.setAnnualQuota(new BigDecimal("7.0"));

        when(leaveTypeRepository.findAllActive()).thenReturn(List.of(type));
        when(policyRepository.findByLeaveTypeId(any())).thenReturn(List.of(policy));
        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(any(), any(), anyInt()))
                .thenReturn(Optional.empty());
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        balanceService.initAnnualBalance(employeeId, java.time.LocalDate.of(2025, 1, 1));

        verify(balanceRepository, times(1)).save(any(LeaveBalance.class));
    }
}
