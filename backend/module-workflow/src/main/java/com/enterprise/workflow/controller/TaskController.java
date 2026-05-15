/**
 * @file TaskController.java
 * @description 工作流任務 API 控制器 / Workflow task REST controller
 * @description_en REST endpoints for pending tasks, approval, rejection, and forwarding
 * @description_zh 提供待辦任務查詢、核准、拒絕與轉派 API 端點
 */
package com.enterprise.workflow.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.service.EmployeeAccessService;
import com.enterprise.workflow.dto.ApprovalRequest;
import com.enterprise.workflow.dto.ForwardRequest;
import com.enterprise.workflow.engine.WorkflowEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.enterprise.workflow.entity.WorkflowTask;
import com.enterprise.workflow.repository.TaskRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflow/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final WorkflowEngine workflowEngine;
    private final TaskRepository taskRepository;
    private final EmployeeAccessService employeeAccessService;

    @GetMapping("/my-pending")
    @RequirePermission("system:workflow:read")
    public ApiResponse<List<WorkflowTask>> getMyPendingTasks(@RequestParam UUID operatorId) {
        employeeAccessService.requireOperableEmployee(operatorId);
        return ApiResponse.success(taskRepository.findByAssigneeIdAndStatus(operatorId, "PENDING"));
    }

    @PostMapping("/{taskId}/approve")
    @RequirePermission("system:workflow:approve")
    @Auditable(module = "workflow-task", action = "approve")
    public ApiResponse<Void> approve(@PathVariable UUID taskId, @RequestBody ApprovalRequest request) {
        employeeAccessService.requireOperableEmployee(request.getOperatorId());
        workflowEngine.approve(taskId, request.getComment(), request.getOperatorId());
        return ApiResponse.success(null);
    }

    @PostMapping("/{taskId}/reject")
    @RequirePermission("system:workflow:approve")
    @Auditable(module = "workflow-task", action = "reject")
    public ApiResponse<Void> reject(@PathVariable UUID taskId, @RequestBody ApprovalRequest request) {
        employeeAccessService.requireOperableEmployee(request.getOperatorId());
        workflowEngine.reject(taskId, request.getComment(), request.getOperatorId());
        return ApiResponse.success(null);
    }

    @PostMapping("/{taskId}/forward")
    @RequirePermission("system:workflow:approve")
    @Auditable(module = "workflow-task", action = "forward")
    public ApiResponse<Void> forward(@PathVariable UUID taskId, @RequestBody ForwardRequest request) {
        employeeAccessService.requireOperableEmployee(request.getOperatorId());
        workflowEngine.forward(taskId, request.getNewAssigneeId(), request.getComment(), request.getOperatorId());
        return ApiResponse.success(null);
    }
}
