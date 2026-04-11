package com.enterprise.workflow.repository;

import com.enterprise.workflow.entity.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InstanceRepository extends JpaRepository<WorkflowInstance, UUID> {
}
