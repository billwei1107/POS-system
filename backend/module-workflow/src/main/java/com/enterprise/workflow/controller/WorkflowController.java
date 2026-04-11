package com.enterprise.workflow.controller;

import com.enterprise.common.dto.ApiResponse;
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

    @PostMapping("/start")
    public ApiResponse<UUID> startWorkflow(@RequestBody StartWorkflowRequest request) {
        UUID instanceId = workflowEngine.startWorkflow(request);
        return ApiResponse.success(instanceId);
    }
}
