/**
 * @file DefinitionController.java
 * @description 工作流定義 API 控制器 / Workflow definition REST controller
 * @description_en REST endpoint for listing workflow definitions
 * @description_zh 提供工作流定義列表查詢 API 端點
 */
package com.enterprise.workflow.controller;

import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.workflow.entity.WorkflowDefinition;
import com.enterprise.workflow.repository.DefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/workflow/definitions")
@RequiredArgsConstructor
public class DefinitionController {

    private final DefinitionRepository definitionRepository;

    @GetMapping
    @RequirePermission("system:workflow:read")
    public ApiResponse<List<WorkflowDefinition>> listDefinitions() {
        return ApiResponse.success(definitionRepository.findAll());
    }
}
