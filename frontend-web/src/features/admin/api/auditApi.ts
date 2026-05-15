/**
 * @file auditApi.ts
 * @description 稽核紀錄 API / Audit log API
 * @description_en Axios client for querying protected audit logs
 * @description_zh 查詢受保護稽核紀錄的 Axios API 客戶端
 */
import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse } from '../../../shared/types';
import type { AdminPageResponse, AuditLog, AuditLogQuery } from '../types';

const BASE = '/v1/audit-logs';

export const auditApi = {
  search: (query: AuditLogQuery) =>
    axiosInstance.get<unknown, ApiResponse<AdminPageResponse<AuditLog>>>(BASE, {
      params: {
        ...query,
        module: query.module || undefined,
        action: query.action || undefined,
        status: query.status || undefined,
      },
    }),
};
