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
@Table(name = "wf_conditions")
public class WorkflowCondition extends BaseEntity {

    @Column(name = "source_node_id", nullable = false)
    private UUID sourceNodeId;

    @Column(name = "target_node_id", nullable = false)
    private UUID targetNodeId;

    @Column(length = 50)
    private String field;

    @Column(length = 20)
    private String operator;

    @Column(length = 255)
    private String value;
}
