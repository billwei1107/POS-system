/**
 * @file LeaveBalanceController.java
 * @description 員工餘假 API 控制器 / Leave balance REST controller
 * @description_en REST endpoint for querying employee leave balances by year
 * @description_zh 查詢員工年度餘假的 API 端點
 */
package com.enterprise.leave.controller;

import com.enterprise.common.dto.ApiResponse;
import com.enterprise.leave.entity.LeaveBalance;
import com.enterprise.leave.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leave/balances")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    @GetMapping
    public ApiResponse<List<LeaveBalance>> list(
            @RequestParam UUID employeeId,
            @RequestParam(required = false) Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        return ApiResponse.success(leaveBalanceService.listByEmployee(employeeId, targetYear));
    }
}
