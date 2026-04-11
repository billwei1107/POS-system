package com.enterprise.workflow.repository;

import com.enterprise.workflow.entity.WorkflowHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryRepository extends JpaRepository<WorkflowHistory, UUID> {
    List<WorkflowHistory> findByInstanceIdOrderByOperatedAtDesc(UUID instanceId);
}
