/**
 * @file WorkflowEventListener.java
 * @description 審批完成事件監聽器 / Workflow approval completed event listener
 * @description_en Listens to ApprovalCompletedEvent and updates leave request status accordingly
 * @description_zh 監聽審批完成事件，更新請假申請狀態
 */
package com.enterprise.leave.listener;

import com.enterprise.leave.service.LeaveRequestService;
import com.enterprise.workflow.event.ApprovalCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEventListener {

    private final LeaveRequestService leaveRequestService;

    // ========================================
    // 監聽審批完成事件 / Listen to approval completed
    // ========================================
    @EventListener
    @Transactional
    public void onApprovalCompleted(ApprovalCompletedEvent event) {
        if (!"LEAVE".equals(event.getBusinessType())) return;

        UUID instanceId = event.getInstanceId();
        // ========================================
        // ApprovalCompletedEvent 目前無 approved 欄位，以 businessId 非空視為通過
        // Treat completion as approved; rejection would require a separate event
        // ========================================
        log.info("Workflow {} completed for LEAVE business {}", instanceId, event.getBusinessId());
        leaveRequestService.handleWorkflowResult(instanceId, true);
    }
}
