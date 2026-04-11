package com.enterprise.workflow.controller;

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
    public ApiResponse<List<WorkflowDefinition>> listDefinitions() {
        return ApiResponse.success(definitionRepository.findAll());
    }
}
