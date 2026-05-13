/**
 * @file LeaveBalanceService.java
 * @description 員工餘假管理服務 / Leave balance management service
 * @description_en Initializes annual balances by service years, deducts and restores leave days
 * @description_zh 依年資初始化年度假別額度，並處理餘假扣除與退回
 */
package com.enterprise.leave.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.leave.entity.LeaveBalance;
import com.enterprise.leave.entity.LeavePolicy;
import com.enterprise.leave.entity.LeaveType;
import com.enterprise.leave.repository.LeaveBalanceRepository;
import com.enterprise.leave.repository.LeavePolicyRepository;
import com.enterprise.leave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveBalanceService {

    private final LeaveBalanceRepository balanceRepository;
    private final LeavePolicyRepository  policyRepository;
    private final LeaveTypeRepository    leaveTypeRepository;

    // ========================================
    // 依到職日初始化年度假別額度 / Init annual balance by hire date
    // ========================================
    @Transactional
    public void initAnnualBalance(UUID employeeId, LocalDate hireDate) {
        int currentYear   = LocalDate.now().getYear();
        int serviceYears  = LocalDate.now().getYear() - hireDate.getYear();

        List<LeaveType> types = leaveTypeRepository.findAllActive();
        for (LeaveType type : types) {
            List<LeavePolicy> policies = policyRepository.findByLeaveTypeId(type.getId());
            BigDecimal quota = resolveQuota(policies, serviceYears);
            if (quota.compareTo(BigDecimal.ZERO) <= 0) continue;

            boolean exists = balanceRepository
                    .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, type.getId(), currentYear)
                    .isPresent();
            if (exists) continue;

            LeaveBalance balance = new LeaveBalance();
            balance.setEmployeeId(employeeId);
            balance.setLeaveTypeId(type.getId());
            balance.setYear(currentYear);
            balance.setTotalDays(quota);
            balance.setUsedDays(BigDecimal.ZERO);
            balance.setRemainingDays(quota);
            balanceRepository.save(balance);
            log.info("Initialized {} days of {} for employee {}", quota, type.getCode(), employeeId);
        }
    }

    // ========================================
    // 扣除餘假 / Deduct leave balance
    // ========================================
    @Transactional
    public void deductBalance(UUID employeeId, UUID leaveTypeId, BigDecimal days) {
        int year = LocalDate.now().getYear();
        LeaveBalance balance = balanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseThrow(() -> new BusinessException("BALANCE_NOT_FOUND: No leave balance found for employee " + employeeId));

        if (balance.getRemainingDays().compareTo(days) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE: Insufficient leave balance. Remaining: " + balance.getRemainingDays() + ", requested: " + days);
        }

        balance.setUsedDays(balance.getUsedDays().add(days));
        balance.setRemainingDays(balance.getRemainingDays().subtract(days));
        balanceRepository.save(balance);
        log.info("Deducted {} days for employee {}, remaining: {}", days, employeeId, balance.getRemainingDays());
    }

    // ========================================
    // 銷假退回餘假 / Restore leave balance on cancellation
    // ========================================
    @Transactional
    public void restoreBalance(UUID employeeId, UUID leaveTypeId, BigDecimal days) {
        int year = LocalDate.now().getYear();
        LeaveBalance balance = balanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseThrow(() -> new BusinessException("BALANCE_NOT_FOUND: No leave balance found for employee " + employeeId));

        balance.setUsedDays(balance.getUsedDays().subtract(days).max(BigDecimal.ZERO));
        balance.setRemainingDays(balance.getTotalDays().subtract(balance.getUsedDays()));
        balanceRepository.save(balance);
        log.info("Restored {} days for employee {}, remaining: {}", days, employeeId, balance.getRemainingDays());
    }

    // ========================================
    // 查詢員工當年餘假 / List employee balances for current year
    // ========================================
    @Transactional(readOnly = true)
    public List<LeaveBalance> listByEmployee(UUID employeeId, int year) {
        return balanceRepository.findByEmployeeIdAndYear(employeeId, year);
    }

    // ========================================
    // 依年資找對應額度 / Resolve quota by service years
    // ========================================
    private BigDecimal resolveQuota(List<LeavePolicy> policies, int serviceYears) {
        return policies.stream()
                .filter(p -> p.getMinServiceYears() <= serviceYears
                        && (p.getMaxServiceYears() == null || p.getMaxServiceYears() >= serviceYears))
                .findFirst()
                .map(LeavePolicy::getAnnualQuota)
                .orElse(BigDecimal.ZERO);
    }
}
