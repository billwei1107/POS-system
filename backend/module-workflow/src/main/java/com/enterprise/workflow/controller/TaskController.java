package com.enterprise.workflow.controller;

import com.enterprise.common.dto.ApiResponse;
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

    @GetMapping("/my-pending")
    public ApiResponse<List<WorkflowTask>> getMyPendingTasks(@RequestParam UUID operatorId) {
        return ApiResponse.success(taskRepository.findByAssigneeIdAndStatus(operatorId, "PENDING"));
    }

    @PostMapping("/{taskId}/approve")
    public ApiResponse<Void> approve(@PathVariable UUID taskId, @RequestBody ApprovalRequest request) {
        workflowEngine.approve(taskId, request.getComment(), request.getOperatorId());
        return ApiResponse.success(null);
    }

    @PostMapping("/{taskId}/reject")
    public ApiResponse<Void> reject(@PathVariable UUID taskId, @RequestBody ApprovalRequest request) {
        workflowEngine.reject(taskId, request.getComment(), request.getOperatorId());
        return ApiResponse.success(null);
    }

    @PostMapping("/{taskId}/forward")
    public ApiResponse<Void> forward(@PathVariable UUID taskId, @RequestBody ForwardRequest request) {
        workflowEngine.forward(taskId, request.getNewAssigneeId(), request.getComment(), request.getOperatorId());
        return ApiResponse.success(null);
    }
}
