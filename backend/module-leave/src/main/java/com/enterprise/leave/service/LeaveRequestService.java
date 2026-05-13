/**
 * @file LeaveRequestService.java
 * @description 請假申請服務 / Leave request service
 * @description_en Handles leave submission, cancellation, and workflow integration
 * @description_zh 處理請假申請提交、銷假，並整合審批流程
 */
package com.enterprise.leave.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.leave.dto.LeaveRequestDTO;
import com.enterprise.leave.entity.LeaveRequest;
import com.enterprise.leave.entity.LeaveRequest.LeaveStatus;
import com.enterprise.leave.event.LeaveCancelledEvent;
import com.enterprise.leave.repository.LeaveRequestRepository;
import com.enterprise.workflow.dto.StartWorkflowRequest;
import com.enterprise.workflow.engine.WorkflowEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository  requestRepository;
    private final LeaveBalanceService     balanceService;
    private final LeaveCalculationService calculationService;
    private final WorkflowEngine          workflowEngine;
    private final ApplicationEventPublisher eventPublisher;

    // ========================================
    // 提交請假申請 / Submit leave request
    // ========================================
    @Transactional
    public LeaveRequest submitRequest(LeaveRequestDTO dto) {
        BigDecimal hours = calculationService.calculateLeaveHours(
                dto.getStartDate(), dto.getEndDate(), dto.getStartHalf(), dto.getEndHalf());
        BigDecimal days = calculationService.hoursToDays(hours);

        // ========================================
        // 驗證餘假足夠 / Validate sufficient balance
        // ========================================
        balanceService.deductBalance(dto.getEmployeeId(), dto.getLeaveTypeId(), days);

        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(dto.getEmployeeId());
        request.setLeaveTypeId(dto.getLeaveTypeId());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setStartHalf(dto.getStartHalf());
        request.setEndHalf(dto.getEndHalf());
        request.setTotalHours(hours);
        request.setReason(dto.getReason());
        request.setAttachmentPath(dto.getAttachmentPath());
        request.setDelegateId(dto.getDelegateId());
        request.setStatus(LeaveStatus.PENDING);
        LeaveRequest saved = requestRepository.save(request);

        // ========================================
        // 發起審批流程 / Start workflow
        // ========================================
        StartWorkflowRequest wfReq = new StartWorkflowRequest();
        wfReq.setDefinitionCode("LEAVE");
        wfReq.setBusinessType("LEAVE");
        wfReq.setBusinessId(saved.getId().toString());
        wfReq.setInitiatorId(dto.getEmployeeId());
        UUID instanceId = workflowEngine.startWorkflow(wfReq);
        saved.setWorkflowInstanceId(instanceId);
        requestRepository.save(saved);

        log.info("Leave request {} submitted, workflow instance {}", saved.getId(), instanceId);
        return saved;
    }

    // ========================================
    // 銷假 / Cancel leave request
    // ========================================
    @Transactional
    public LeaveRequest cancelRequest(UUID requestId) {
        LeaveRequest request = findOrThrow(requestId);
        if (request.getStatus() == LeaveStatus.CANCELLED) {
            throw new BusinessException("LEAVE_ALREADY_CANCELLED: Leave request is already cancelled");
        }
        if (request.getStatus() == LeaveStatus.REJECTED) {
            throw new BusinessException("LEAVE_ALREADY_REJECTED: Cannot cancel a rejected leave request");
        }

        // ========================================
        // 若已核准，退回餘假 / Restore balance if approved
        // ========================================
        if (request.getStatus() == LeaveStatus.APPROVED) {
            BigDecimal days = calculationService.hoursToDays(request.getTotalHours());
            balanceService.restoreBalance(request.getEmployeeId(), request.getLeaveTypeId(), days);
        }

        request.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest saved = requestRepository.save(request);

        eventPublisher.publishEvent(new LeaveCancelledEvent(this, requestId, request.getEmployeeId()));
        log.info("Leave request {} cancelled", requestId);
        return saved;
    }

    // ========================================
    // 查詢員工請假記錄 / List requests by employee
    // ========================================
    @Transactional(readOnly = true)
    public List<LeaveRequest> listByEmployee(UUID employeeId) {
        return requestRepository.findByEmployeeId(employeeId);
    }

    // ========================================
    // 查詢月曆範圍內的已核准請假 / List approved requests in date range
    // ========================================
    @Transactional(readOnly = true)
    public List<LeaveRequest> listCalendar(LocalDate startDate, LocalDate endDate) {
        return requestRepository.findApprovedInDateRange(startDate, endDate);
    }

    // ========================================
    // 審批完成回調（由 WorkflowEventListener 呼叫）/ Approve or reject callback
    // ========================================
    @Transactional
    public void handleWorkflowResult(UUID instanceId, boolean approved) {
        LeaveRequest request = requestRepository.findByWorkflowInstanceId(instanceId)
                .orElseThrow(() -> new BusinessException("LEAVE_REQUEST_NOT_FOUND: No leave request for workflow instance " + instanceId));

        if (approved) {
            request.setStatus(LeaveStatus.APPROVED);
            log.info("Leave request {} approved", request.getId());
        } else {
            // ========================================
            // 拒絕時退回已扣除的餘假 / Restore balance on rejection
            // ========================================
            request.setStatus(LeaveStatus.REJECTED);
            BigDecimal days = calculationService.hoursToDays(request.getTotalHours());
            balanceService.restoreBalance(request.getEmployeeId(), request.getLeaveTypeId(), days);
            log.info("Leave request {} rejected, balance restored", request.getId());
        }
        requestRepository.save(request);
    }

    private LeaveRequest findOrThrow(UUID id) {
        return requestRepository.findById(id)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("LEAVE_REQUEST_NOT_FOUND: Leave request not found: " + id));
    }
}
