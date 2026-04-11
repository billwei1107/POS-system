package com.enterprise.workflow.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ApprovalRequest {
    private UUID operatorId;
    private String comment;
}
