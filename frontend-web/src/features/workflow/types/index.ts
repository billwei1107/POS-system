export interface WorkflowDefinition {
    id: string;
    name: string;
    code: string;
    category: string;
    version: number;
    status: string;
    description: string;
}

export interface WorkflowTask {
    id: string;
    instanceId: string;
    nodeId: string;
    assigneeId: string;
    status: string;
    comment: string;
    operatedAt: string;
}

export interface WorkflowInstance {
    id: string;
    definitionId: string;
    businessType: string;
    businessId: string;
    initiatorId: string;
    status: string;
    currentNodeId: string;
}

export interface StartWorkflowRequest {
    definitionCode: string;
    businessType: string;
    businessId: string;
    initiatorId: string;
}

export interface ApprovalRequest {
    operatorId: string;
    comment?: string;
}

export interface ForwardRequest {
    operatorId: string;
    newAssigneeId: string;
    comment?: string;
}
