export interface LoginRequest {
    username: string;
    password?: string;
}

export interface LoginResponse {
    token: string;
    refreshToken: string;
    userId: string;
    username: string;
}

export interface User {
    id: string;
    username: string;
    email: string;
    phone: string;
    status: string;
    failedAttempts?: number | null;
    lockedUntil?: string | null;
    mfaEnabled?: boolean | null;
    createdAt: string;
    updatedAt?: string | null;
}

export interface Role {
    id: string;
    name: string;
    code: string;
    description: string;
}

export interface Permission {
    id: string;
    name: string;
    code: string;
    type: string;
    resource: string;
    action: string;
}

export interface RolePermissionSummary extends Role {
    permissions: Permission[];
}

export interface UpdateRolePermissionsRequest {
    permissionIds: string[];
}

export interface UserRoleSummary {
    user: User;
    roles: Role[];
}

export interface UserRoleSummaryQuery {
    keyword?: string;
    status?: string;
    roleId?: string;
    page?: number;
    size?: number;
}

export interface PageResponse<T> {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

export interface UpdateUserRolesRequest {
    roleIds: string[];
}

export interface CreateUserRequest {
    username: string;
    password: string;
    email?: string;
    phone?: string;
    roleIds: string[];
}

export interface UpdateUserStatusRequest {
    status: 'ACTIVE' | 'INACTIVE';
}

export interface ResetUserPasswordRequest {
    password: string;
}
