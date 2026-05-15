/**
 * @file WorkflowController.java
 * @description 工作流啟動 API 控制器 / Workflow start REST controller
 * @description_en REST endpoint for starting workflow instances
 * @description_zh 提供工作流實例啟動 API 端點
 */
package com.enterprise.workflow.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.service.EmployeeAccessService;
import com.enterprise.workflow.dto.StartWorkflowRequest;
import com.enterprise.workflow.engine.WorkflowEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowEngine workflowEngine;
    private final EmployeeAccessService employeeAccessService;

    @PostMapping("/start")
    @RequirePermission("system:workflow:start")
    @Auditable(module = "workflow-instance", action = "start")
    public ApiResponse<UUID> startWorkflow(@RequestBody StartWorkflowRequest request) {
        employeeAccessService.requireOperableEmployee(request.getInitiatorId());
        UUID instanceId = workflowEngine.startWorkflow(request);
        return ApiResponse.success(instanceId);
    }
}
