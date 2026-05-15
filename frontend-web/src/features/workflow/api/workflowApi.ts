import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse } from '../../../shared/types';
import type { WorkflowDefinition, WorkflowTask, StartWorkflowRequest, ApprovalRequest, ForwardRequest } from '../types';

export const workflowApi = {
    getDefinitions: () =>
        axiosInstance.get<unknown, ApiResponse<WorkflowDefinition[]>>('/v1/workflow/definitions'),

    startWorkflow: (request: StartWorkflowRequest) =>
        axiosInstance.post<unknown, ApiResponse<string>>('/v1/workflow/start', request),

    getMyTasks: (operatorId: string) =>
        axiosInstance.get<unknown, ApiResponse<WorkflowTask[]>>('/v1/workflow/tasks/my-pending', {
            params: { operatorId },
        }),

    approveTask: (taskId: string, request: ApprovalRequest) =>
        axiosInstance.post<unknown, ApiResponse<void>>(`/v1/workflow/tasks/${taskId}/approve`, request),

    rejectTask: (taskId: string, request: ApprovalRequest) =>
        axiosInstance.post<unknown, ApiResponse<void>>(`/v1/workflow/tasks/${taskId}/reject`, request),

    forwardTask: (taskId: string, request: ForwardRequest) =>
        axiosInstance.post<unknown, ApiResponse<void>>(`/v1/workflow/tasks/${taskId}/forward`, request)
};
