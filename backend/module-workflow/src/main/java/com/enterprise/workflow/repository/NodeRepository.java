package com.enterprise.workflow.repository;

import com.enterprise.workflow.entity.WorkflowNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NodeRepository extends JpaRepository<WorkflowNode, UUID> {
    List<WorkflowNode> findByDefinitionIdOrderBySortOrderAsc(UUID definitionId);
}
