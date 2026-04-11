package com.enterprise.workflow.engine;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.workflow.dto.StartWorkflowRequest;
import com.enterprise.workflow.entity.WorkflowDefinition;
import com.enterprise.workflow.entity.WorkflowInstance;
import com.enterprise.workflow.entity.WorkflowNode;
import com.enterprise.workflow.repository.DefinitionRepository;
import com.enterprise.workflow.repository.InstanceRepository;
import com.enterprise.workflow.repository.NodeRepository;
import com.enterprise.workflow.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkflowEngineTest {

    @Mock
    private DefinitionRepository definitionRepository;
    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private InstanceRepository instanceRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WorkflowEngine workflowEngine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testStartWorkflow_DefinitionNotFound() {
        StartWorkflowRequest request = new StartWorkflowRequest();
        request.setDefinitionCode("LEAVE_REQ");

        when(definitionRepository.findByCode("LEAVE_REQ")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> workflowEngine.startWorkflow(request));
    }

    @Test
    void testStartWorkflow_Success() {
        // Arrange
        StartWorkflowRequest request = new StartWorkflowRequest();
        request.setDefinitionCode("LEAVE_REQ");
        request.setBusinessType("LEAVE");
        request.setBusinessId("L-001");
        request.setInitiatorId(UUID.randomUUID());

        WorkflowDefinition def = new WorkflowDefinition();
        def.setId(UUID.randomUUID());
        def.setStatus("PUBLISHED");

        WorkflowNode approvalNode = new WorkflowNode();
        approvalNode.setId(UUID.randomUUID());
        approvalNode.setType("APPROVAL");
        approvalNode.setApproverType("DEPARTMENT_MANAGER");

        when(definitionRepository.findByCode("LEAVE_REQ")).thenReturn(Optional.of(def));
        when(instanceRepository.save(any())).thenAnswer(inv -> {
            WorkflowInstance inst = inv.getArgument(0);
            if (inst.getId() == null) inst.setId(UUID.randomUUID());
            return inst;
        });
        when(nodeRepository.findByDefinitionIdOrderBySortOrderAsc(def.getId()))
                .thenReturn(Collections.singletonList(approvalNode));
        when(taskRepository.save(any())).thenAnswer(inv -> {
            com.enterprise.workflow.entity.WorkflowTask task = inv.getArgument(0);
            if (task.getId() == null) task.setId(UUID.randomUUID());
            return task;
        });

        // Act
        UUID instanceId = workflowEngine.startWorkflow(request);

        // Assert
        assertNotNull(instanceId);
        verify(eventPublisher, times(1)).publishEvent(any(com.enterprise.workflow.event.ApprovalPendingEvent.class));
    }
}
