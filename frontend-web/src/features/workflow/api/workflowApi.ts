import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse } from '../../../shared/types';
import type { WorkflowDefinition, WorkflowTask, StartWorkflowRequest, ApprovalRequest, ForwardRequest } from '../types';

export const workflowApi = {
    getDefinitions: () =>
        axiosInstance.get<ApiResponse<WorkflowDefinition[]>>('/v1/workflow/definitions'),

    startWorkflow: (request: StartWorkflowRequest) =>
        axiosInstance.post<ApiResponse<string>>('/v1/workflow/start', request),

    getMyTasks: (operatorId: string) =>
        axiosInstance.get<ApiResponse<WorkflowTask[]>>(`/v1/workflow/tasks/my-pending?operatorId=${operatorId}`),

    approveTask: (taskId: string, request: ApprovalRequest) =>
        axiosInstance.post<ApiResponse<void>>(`/v1/workflow/tasks/${taskId}/approve`, request),

    rejectTask: (taskId: string, request: ApprovalRequest) =>
        axiosInstance.post<ApiResponse<void>>(`/v1/workflow/tasks/${taskId}/reject`, request),

    forwardTask: (taskId: string, request: ForwardRequest) =>
        axiosInstance.post<ApiResponse<void>>(`/v1/workflow/tasks/${taskId}/forward`, request)
};
