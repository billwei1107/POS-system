package com.enterprise.workflow.repository;

import com.enterprise.workflow.entity.WorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<WorkflowTask, UUID> {
    List<WorkflowTask> findByInstanceIdAndNodeIdAndStatus(UUID instanceId, UUID nodeId, String status);
    List<WorkflowTask> findByAssigneeIdAndStatus(UUID assigneeId, String status);
}
