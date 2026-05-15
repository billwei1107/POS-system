/**
 * @file index.ts
 * @description 後台管理型別 / Admin management types
 * @description_en Type definitions for admin management features
 * @description_zh 定義後台管理功能使用的資料型別
 */

export interface AdminPageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export type AuditStatus = 'SUCCESS' | 'FAILED';

export interface AuditLog {
  id: string;
  module: string;
  action: string;
  methodName: string;
  userId: string | null;
  roleCode: string | null;
  status: AuditStatus;
  errorMessage: string | null;
  durationMs: number;
  occurredAt: string;
}

export interface AuditLogQuery {
  module?: string;
  action?: string;
  status?: AuditStatus | '';
  page?: number;
  size?: number;
}
