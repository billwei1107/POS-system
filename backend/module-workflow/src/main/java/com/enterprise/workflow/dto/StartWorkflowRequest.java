package com.enterprise.workflow.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class StartWorkflowRequest {
    private String definitionCode;
    private String businessType;
    private String businessId;
    private UUID initiatorId;
}
