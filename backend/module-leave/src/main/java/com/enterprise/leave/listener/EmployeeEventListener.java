/**
 * @file EmployeeEventListener.java
 * @description 員工事件監聽器 / Employee event listener
 * @description_en Initializes leave balances when a new employee is created
 * @description_zh 新員工建立時，自動初始化年度假別額度
 */
package com.enterprise.leave.listener;

import com.enterprise.leave.service.LeaveBalanceService;
import com.enterprise.organization.event.EmployeeCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeEventListener {

    private final LeaveBalanceService balanceService;

    // ========================================
    // 新員工建立 → 初始化年度假別額度 / Init balances on employee creation
    // ========================================
    @EventListener
    @Transactional
    public void onEmployeeCreated(EmployeeCreatedEvent event) {
        LocalDate hireDate = event.getEmployee().getHireDate() != null
                ? event.getEmployee().getHireDate()
                : LocalDate.now();
        log.info("Initializing leave balances for new employee {}", event.getEmployee().getId());
        balanceService.initAnnualBalance(event.getEmployee().getId(), hireDate);
    }
}
