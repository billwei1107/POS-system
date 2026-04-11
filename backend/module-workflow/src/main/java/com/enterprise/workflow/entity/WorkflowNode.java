package com.enterprise.workflow.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "wf_nodes")
public class WorkflowNode extends BaseEntity {

    @Column(name = "definition_id", nullable = false)
    private UUID definitionId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "approver_type", length = 50)
    private String approverType;

    @Column(name = "approver_id", length = 50)
    private String approverId;

    @Column(name = "approval_strategy", length = 20)
    private String approvalStrategy = "ANY";

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "timeout_hours")
    private Integer timeoutHours;
}
