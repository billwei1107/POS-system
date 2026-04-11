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
@Table(name = "wf_instances")
public class WorkflowInstance extends BaseEntity {

    @Column(name = "definition_id", nullable = false)
    private UUID definitionId;

    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;

    @Column(name = "business_id", nullable = false, length = 100)
    private String businessId;

    @Column(name = "initiator_id", nullable = false)
    private UUID initiatorId;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "current_node_id")
    private UUID currentNodeId;
}
