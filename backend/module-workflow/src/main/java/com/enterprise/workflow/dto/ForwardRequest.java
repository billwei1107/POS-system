package com.enterprise.workflow.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ForwardRequest {
    private UUID operatorId;
    private UUID newAssigneeId;
    private String comment;
}
