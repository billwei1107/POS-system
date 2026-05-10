/**
 * @file staffApi.ts
 * @description POS 排班管理 API / POS staff management API
 * @description_en Axios API functions for staff shifts, X/Z reports, and clock events
 * @description_zh 班次管理、X/Z Report、打卡的 Axios API 函式
 */
import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse } from '../../../shared/types';
import type {
  CloseShiftPayload,
  GenerateZReportPayload,
  OpenShiftPayload,
  StaffShift,
  XReport,
  ZReport,
} from '../types';

const SHIFT_BASE = '/v1/staff/shifts';
const REPORT_BASE = '/v1/staff/reports';

// ========================================
// 班次管理 API / Shift management API
// ========================================
export const shiftApi = {
  listOpen: (storeId: string) =>
    axiosInstance.get<unknown, ApiResponse<StaffShift[]>>(SHIFT_BASE, { params: { storeId } }),

  open: (storeId: string, payload: OpenShiftPayload) =>
    axiosInstance.post<unknown, ApiResponse<StaffShift>, OpenShiftPayload>(`${SHIFT_BASE}/open`, payload, { params: { storeId } }),

  close: (shiftId: string, payload: CloseShiftPayload) =>
    axiosInstance.post<unknown, ApiResponse<StaffShift>, CloseShiftPayload>(`${SHIFT_BASE}/${shiftId}/close`, payload),

  blindClose: (shiftId: string, notes?: string) =>
    axiosInstance.post<unknown, ApiResponse<StaffShift>>(`${SHIFT_BASE}/${shiftId}/blind-close`, null, { params: { notes } }),

  createHandover: (fromShiftId: string, params: {
    toShiftId?: string;
    cashCounted: number;
    notes?: string;
    confirmedBy: string;
  }) =>
    axiosInstance.post<unknown, ApiResponse<string>>(`${SHIFT_BASE}/${fromShiftId}/handover`, null, { params }),

  clock: (shiftId: string, clockType: string, terminalId?: string, notes?: string) =>
    axiosInstance.post<unknown, ApiResponse<string>>(`${SHIFT_BASE}/${shiftId}/clock`, null, {
      params: { clockType, terminalId, notes },
    }),
};

// ========================================
// 報表 API / Report API
// ========================================
export const reportApi = {
  generateX: (shiftId: string) =>
    axiosInstance.post<unknown, ApiResponse<XReport>>(`${REPORT_BASE}/x/${shiftId}`),

  listX: (shiftId: string) =>
    axiosInstance.get<unknown, ApiResponse<XReport[]>>(`${REPORT_BASE}/x`, { params: { shiftId } }),

  generateZ: (storeId: string, payload: GenerateZReportPayload) =>
    axiosInstance.post<unknown, ApiResponse<ZReport>, GenerateZReportPayload>(`${REPORT_BASE}/z/${storeId}`, payload),

  listZ: (storeId: string) =>
    axiosInstance.get<unknown, ApiResponse<ZReport[]>>(`${REPORT_BASE}/z/${storeId}`),
};
