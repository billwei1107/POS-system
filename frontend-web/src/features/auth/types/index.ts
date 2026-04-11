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
    createdAt: string;
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
    action: string;
}
