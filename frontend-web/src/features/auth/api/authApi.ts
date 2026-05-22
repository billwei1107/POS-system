import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse } from '../../../shared/types';
import type {
    LoginRequest,
    LoginResponse,
    Permission,
    CreateRoleRequest,
    CreateUserRequest,
    ResetUserPasswordRequest,
    Role,
    RolePermissionSummary,
    UpdateRolePermissionsRequest,
    UpdateUserRolesRequest,
    UpdateUserStatusRequest,
    PageResponse,
    UserRoleSummaryQuery,
    UserRoleSummary,
} from '../types';

export const loginApi = async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await axiosInstance.post<unknown, ApiResponse<LoginResponse>, LoginRequest>('/v1/auth/login', data);
    return response.data;
};

export const logoutApi = async (): Promise<void> => {
    await axiosInstance.post('/v1/auth/logout');
};

export const roleApi = {
    getRoles: async (): Promise<Role[]> => {
        const response = await axiosInstance.get<unknown, ApiResponse<Role[]>>('/v1/roles');
        return response.data ?? [];
    },
    getPermissionSummaries: async (): Promise<RolePermissionSummary[]> => {
        const response = await axiosInstance.get<unknown, ApiResponse<RolePermissionSummary[]>>('/v1/roles/permission-summaries');
        return response.data ?? [];
    },
    getPermissions: async (): Promise<Permission[]> => {
        const response = await axiosInstance.get<unknown, ApiResponse<Permission[]>>('/v1/permissions');
        return response.data ?? [];
    },
    create: async (data: CreateRoleRequest): Promise<RolePermissionSummary> => {
        const response = await axiosInstance.post<unknown, ApiResponse<RolePermissionSummary>, CreateRoleRequest>('/v1/roles', data);
        return response.data;
    },
    updatePermissions: async (roleId: string, data: UpdateRolePermissionsRequest): Promise<RolePermissionSummary> => {
        const response = await axiosInstance.put<unknown, ApiResponse<RolePermissionSummary>, UpdateRolePermissionsRequest>(
            `/v1/roles/${roleId}/permissions`,
            data
        );
        return response.data;
    },
};

export const userApi = {
    getRoleSummaries: async (query: UserRoleSummaryQuery = {}): Promise<PageResponse<UserRoleSummary>> => {
        const response = await axiosInstance.get<unknown, ApiResponse<PageResponse<UserRoleSummary>>>('/v1/users', {
            params: {
                ...query,
                keyword: query.keyword || undefined,
                status: query.status || undefined,
                roleId: query.roleId || undefined,
            },
        });
        return response.data ?? {
            content: [],
            pageNumber: query.page ?? 0,
            pageSize: query.size ?? 10,
            totalElements: 0,
            totalPages: 0,
            last: true,
        };
    },
    create: async (data: CreateUserRequest): Promise<UserRoleSummary> => {
        const response = await axiosInstance.post<unknown, ApiResponse<UserRoleSummary>, CreateUserRequest>('/v1/users', data);
        return response.data;
    },
    updateRoles: async (userId: string, data: UpdateUserRolesRequest): Promise<UserRoleSummary> => {
        const response = await axiosInstance.put<unknown, ApiResponse<UserRoleSummary>, UpdateUserRolesRequest>(
            `/v1/users/${userId}/roles`,
            data
        );
        return response.data;
    },
    updateStatus: async (userId: string, data: UpdateUserStatusRequest): Promise<UserRoleSummary> => {
        const response = await axiosInstance.put<unknown, ApiResponse<UserRoleSummary>, UpdateUserStatusRequest>(
            `/v1/users/${userId}/status`,
            data
        );
        return response.data;
    },
    resetPassword: async (userId: string, data: ResetUserPasswordRequest): Promise<UserRoleSummary> => {
        const response = await axiosInstance.put<unknown, ApiResponse<UserRoleSummary>, ResetUserPasswordRequest>(
            `/v1/users/${userId}/password`,
            data
        );
        return response.data;
    },
};
