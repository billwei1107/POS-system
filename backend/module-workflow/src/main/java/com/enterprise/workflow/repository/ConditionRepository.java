package com.enterprise.workflow.repository;

import com.enterprise.workflow.entity.WorkflowCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConditionRepository extends JpaRepository<WorkflowCondition, UUID> {
    List<WorkflowCondition> findBySourceNodeId(UUID sourceNodeId);
}
