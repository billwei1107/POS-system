package com.enterprise.workflow.engine;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.workflow.dto.StartWorkflowRequest;
import com.enterprise.workflow.entity.WorkflowDefinition;
import com.enterprise.workflow.entity.WorkflowInstance;
import com.enterprise.workflow.entity.WorkflowNode;
import com.enterprise.workflow.entity.WorkflowTask;
import com.enterprise.workflow.event.ApprovalCompletedEvent;
import com.enterprise.workflow.event.ApprovalPendingEvent;
import com.enterprise.workflow.repository.DefinitionRepository;
import com.enterprise.workflow.repository.InstanceRepository;
import com.enterprise.workflow.repository.NodeRepository;
import com.enterprise.workflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowEngine {

    private final DefinitionRepository definitionRepository;
    private final NodeRepository nodeRepository;
    private final InstanceRepository instanceRepository;
    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UUID startWorkflow(StartWorkflowRequest request) {
        WorkflowDefinition def = definitionRepository.findByCode(request.getDefinitionCode())
                .orElseThrow(() -> new BusinessException(404, "Workflow definition not found"));

        if (!"PUBLISHED".equals(def.getStatus())) {
            throw new BusinessException(400, "Workflow definition is not published");
        }

        WorkflowInstance instance = new WorkflowInstance();
        instance.setDefinitionId(def.getId());
        instance.setBusinessType(request.getBusinessType());
        instance.setBusinessId(request.getBusinessId());
        instance.setInitiatorId(request.getInitiatorId());
        instance.setStatus("PENDING");
        instance = instanceRepository.save(instance);

        List<WorkflowNode> nodes = nodeRepository.findByDefinitionIdOrderBySortOrderAsc(def.getId());
        if (nodes.isEmpty()) {
            throw new BusinessException(500, "No nodes found for workflow definition");
        }

        WorkflowNode firstApprovalNode = nodes.stream()
                .filter(n -> "APPROVAL".equals(n.getType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(500, "No approval node found"));

        instance.setCurrentNodeId(firstApprovalNode.getId());
        instanceRepository.save(instance);

        UUID assigneeId = resolveApprover(firstApprovalNode, request.getInitiatorId());

        WorkflowTask task = new WorkflowTask();
        task.setInstanceId(instance.getId());
        task.setNodeId(firstApprovalNode.getId());
        task.setAssigneeId(assigneeId);
        task.setStatus("PENDING");
        task = taskRepository.save(task);

        eventPublisher.publishEvent(new ApprovalPendingEvent(this, instance.getId(), task.getId(), assigneeId));

        return instance.getId();
    }

    private UUID resolveApprover(WorkflowNode node, UUID initiatorId) {
        if ("DEPARTMENT_MANAGER".equals(node.getApproverType())) {
            // Placeholder: 假定尋找主管並回傳 UUID
            return initiatorId;
        }
        if ("SPECIFIC_USER".equals(node.getApproverType())) {
            return UUID.fromString(node.getApproverId());
        }
        return initiatorId;
    }

    @Transactional
    public void approve(UUID taskId, String comment, UUID operatorId) {
        WorkflowTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(404, "Task not found"));

        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException(400, "Task is not pending");
        }
        if (!task.getAssigneeId().equals(operatorId)) {
            throw new BusinessException(403, "Not authorized to approve this task");
        }

        task.setStatus("APPROVED");
        task.setComment(comment);
        task.setOperatedAt(LocalDateTime.now());
        taskRepository.save(task);

        WorkflowInstance instance = instanceRepository.findById(task.getInstanceId())
                .orElseThrow(() -> new BusinessException(404, "Instance not found"));

        advanceToNextNode(instance);
    }

    private void advanceToNextNode(WorkflowInstance instance) {
        List<WorkflowNode> nodes = nodeRepository.findByDefinitionIdOrderBySortOrderAsc(instance.getDefinitionId());

        boolean foundCurrent = false;
        WorkflowNode nextNode = null;
        for (WorkflowNode node : nodes) {
            if (foundCurrent && !"CONDITION".equals(node.getType())) {
                nextNode = node;
                break;
            }
            if (node.getId().equals(instance.getCurrentNodeId())) {
                foundCurrent = true;
            }
        }

        if (nextNode == null || "END".equals(nextNode.getType())) {
            instance.setStatus("APPROVED");
            instance.setCurrentNodeId(null);
            instanceRepository.save(instance);
            eventPublisher.publishEvent(new ApprovalCompletedEvent(this, instance.getId(), instance.getBusinessType(),
                    instance.getBusinessId()));
        } else {
            instance.setCurrentNodeId(nextNode.getId());
            instanceRepository.save(instance);

            UUID nextAssignee = resolveApprover(nextNode, instance.getInitiatorId());
            WorkflowTask task = new WorkflowTask();
            task.setInstanceId(instance.getId());
            task.setNodeId(nextNode.getId());
            task.setAssigneeId(nextAssignee);
            task.setStatus("PENDING");
            task = taskRepository.save(task);

            eventPublisher.publishEvent(new ApprovalPendingEvent(this, instance.getId(), task.getId(), nextAssignee));
        }
    }

    @Transactional
    public void reject(UUID taskId, String comment, UUID operatorId) {
        WorkflowTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(404, "Task not found"));

        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException(400, "Task is not pending");
        }
        if (!task.getAssigneeId().equals(operatorId)) {
            throw new BusinessException(403, "Not authorized to reject this task");
        }

        task.setStatus("REJECTED");
        task.setComment(comment);
        task.setOperatedAt(LocalDateTime.now());
        taskRepository.save(task);

        WorkflowInstance instance = instanceRepository.findById(task.getInstanceId())
                .orElseThrow(() -> new BusinessException(404, "Instance not found"));

        instance.setStatus("REJECTED");
        instance.setCurrentNodeId(null);
        instanceRepository.save(instance);

        eventPublisher.publishEvent(new com.enterprise.workflow.event.ApprovalRejectedEvent(this, instance.getId(),
                instance.getBusinessType(), instance.getBusinessId(), comment));
    }

    @Transactional
    public void forward(UUID taskId, UUID newAssigneeId, String comment, UUID operatorId) {
        WorkflowTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(404, "Task not found"));

        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException(400, "Task is not pending");
        }
        if (!task.getAssigneeId().equals(operatorId)) {
            throw new BusinessException(403, "Not authorized to forward this task");
        }

        task.setStatus("FORWARDED");
        task.setComment(comment);
        task.setOperatedAt(LocalDateTime.now());
        taskRepository.save(task);

        WorkflowTask newTask = new WorkflowTask();
        newTask.setInstanceId(task.getInstanceId());
        newTask.setNodeId(task.getNodeId());
        newTask.setAssigneeId(newAssigneeId);
        newTask.setStatus("PENDING");
        newTask = taskRepository.save(newTask);

        eventPublisher
                .publishEvent(new ApprovalPendingEvent(this, task.getInstanceId(), newTask.getId(), newAssigneeId));
    }
}
