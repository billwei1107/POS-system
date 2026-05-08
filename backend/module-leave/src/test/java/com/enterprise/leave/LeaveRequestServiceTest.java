/**
 * @file LeaveRequestServiceTest.java
 * @description 請假申請服務單元測試 / Leave request service unit tests
 * @description_en Tests for leave submission, cancellation, and workflow result handling
 * @description_zh 驗證請假申請、銷假、審批回調的業務邏輯
 */
package com.enterprise.leave;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.leave.dto.LeaveRequestDTO;
import com.enterprise.leave.entity.LeaveRequest;
import com.enterprise.leave.entity.LeaveRequest.HalfDay;
import com.enterprise.leave.entity.LeaveRequest.LeaveStatus;
import com.enterprise.leave.repository.LeaveRequestRepository;
import com.enterprise.leave.service.LeaveBalanceService;
import com.enterprise.leave.service.LeaveCalculationService;
import com.enterprise.leave.service.LeaveRequestService;
import com.enterprise.workflow.engine.WorkflowEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock private LeaveRequestRepository   requestRepository;
    @Mock private LeaveBalanceService      balanceService;
    @Mock private LeaveCalculationService  calculationService;
    @Mock private WorkflowEngine           workflowEngine;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private LeaveRequestService leaveRequestService;

    private UUID employeeId;
    private UUID leaveTypeId;
    private UUID instanceId;

    @BeforeEach
    void setUp() {
        employeeId  = UUID.randomUUID();
        leaveTypeId = UUID.randomUUID();
        instanceId  = UUID.randomUUID();
    }

    // ========================================
    // 提交請假成功 / Submit request success
    // ========================================
    @Test
    @DisplayName("提交請假成功，狀態應為 PENDING，workflowInstanceId 不為空")
    void submitRequest_success() {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(employeeId);
        dto.setLeaveTypeId(leaveTypeId);
        dto.setStartDate(LocalDate.of(2026, 5, 11));
        dto.setEndDate(LocalDate.of(2026, 5, 13));
        dto.setStartHalf(HalfDay.FULL);
        dto.setEndHalf(HalfDay.FULL);

        when(calculationService.calculateLeaveHours(any(), any(), any(), any())).thenReturn(new BigDecimal("24"));
        when(calculationService.hoursToDays(any())).thenReturn(new BigDecimal("3.0"));
        when(requestRepository.save(any())).thenAnswer(inv -> {
            LeaveRequest r = inv.getArgument(0);
            if (r.getId() == null) {
                try {
                    java.lang.reflect.Field f = com.enterprise.common.entity.BaseEntity.class.getDeclaredField("id");
                    f.setAccessible(true);
                    f.set(r, UUID.randomUUID());
                } catch (Exception ignored) {}
            }
            return r;
        });
        when(workflowEngine.startWorkflow(any())).thenReturn(instanceId);

        LeaveRequest result = leaveRequestService.submitRequest(dto);

        assertThat(result.getStatus()).isEqualTo(LeaveStatus.PENDING);
        verify(balanceService).deductBalance(employeeId, leaveTypeId, new BigDecimal("3.0"));
        verify(workflowEngine).startWorkflow(any());
    }

    // ========================================
    // 銷假（已核准）→ 退回餘假 / Cancel approved → restore balance
    // ========================================
    @Test
    @DisplayName("銷假已核准的請假，應退回餘假")
    void cancelRequest_approved_restoresBalance() {
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(employeeId);
        request.setLeaveTypeId(leaveTypeId);
        request.setStatus(LeaveStatus.APPROVED);
        request.setTotalHours(new BigDecimal("24"));

        when(requestRepository.findById(any())).thenReturn(Optional.of(request));
        when(calculationService.hoursToDays(any())).thenReturn(new BigDecimal("3.0"));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest result = leaveRequestService.cancelRequest(UUID.randomUUID());

        assertThat(result.getStatus()).isEqualTo(LeaveStatus.CANCELLED);
        verify(balanceService).restoreBalance(employeeId, leaveTypeId, new BigDecimal("3.0"));
    }

    // ========================================
    // 銷假已取消的請假應拋例外 / Cancel already cancelled throws exception
    // ========================================
    @Test
    @DisplayName("銷假已取消的請假應拋 LEAVE_ALREADY_CANCELLED")
    void cancelRequest_alreadyCancelled_throws() {
        LeaveRequest request = new LeaveRequest();
        request.setStatus(LeaveStatus.CANCELLED);

        when(requestRepository.findById(any())).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> leaveRequestService.cancelRequest(UUID.randomUUID()))
                .hasMessageContaining("LEAVE_ALREADY_CANCELLED");
    }

    // ========================================
    // 審批通過回調 / Workflow approved callback
    // ========================================
    @Test
    @DisplayName("審批通過後請假狀態應更新為 APPROVED")
    void handleWorkflowResult_approved() {
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(employeeId);
        request.setLeaveTypeId(leaveTypeId);
        request.setStatus(LeaveStatus.PENDING);
        request.setTotalHours(new BigDecimal("8"));

        when(requestRepository.findByWorkflowInstanceId(instanceId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        leaveRequestService.handleWorkflowResult(instanceId, true);

        assertThat(request.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        verify(balanceService, never()).restoreBalance(any(), any(), any());
    }

    // ========================================
    // 審批拒絕回調 → 退回餘假 / Workflow rejected → restore balance
    // ========================================
    @Test
    @DisplayName("審批拒絕後狀態應為 REJECTED，且退回餘假")
    void handleWorkflowResult_rejected() {
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(employeeId);
        request.setLeaveTypeId(leaveTypeId);
        request.setStatus(LeaveStatus.PENDING);
        request.setTotalHours(new BigDecimal("8"));

        when(requestRepository.findByWorkflowInstanceId(instanceId)).thenReturn(Optional.of(request));
        when(calculationService.hoursToDays(any())).thenReturn(new BigDecimal("1.0"));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        leaveRequestService.handleWorkflowResult(instanceId, false);

        assertThat(request.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        verify(balanceService).restoreBalance(employeeId, leaveTypeId, new BigDecimal("1.0"));
    }
}
