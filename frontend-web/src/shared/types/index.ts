export interface ApiResponse<T = unknown> {
    success: boolean;
    message: string;
    data: T;
    code: number;
}

export interface PaginatedData<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}
