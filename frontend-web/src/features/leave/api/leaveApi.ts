/**
 * @file leaveApi.ts
 * @description 請假管理 API 請求層 / Leave management API request layer
 * @description_en Encapsulates REST calls for leave types, balances, and requests
 * @description_zh 封裝假別類型、餘假、請假申請的 REST 呼叫
 */

import axiosInstance from '../../../shared/api/axiosInstance';
import type { LeaveType, LeaveBalance, LeaveRequest, SubmitLeaveRequestPayload } from '../types';

// ========================================
// 假別類型 / Leave types
// ========================================
export const fetchLeaveTypes = async (): Promise<LeaveType[]> => {
  const res = await axiosInstance.get('/api/v1/leave/types');
  return res.data.data;
};

export const createLeaveType = async (data: Partial<LeaveType>): Promise<LeaveType> => {
  const res = await axiosInstance.post('/api/v1/leave/types', data);
  return res.data.data;
};

export const updateLeaveType = async (id: string, data: Partial<LeaveType>): Promise<LeaveType> => {
  const res = await axiosInstance.put(`/api/v1/leave/types/${id}`, data);
  return res.data.data;
};

export const deleteLeaveType = async (id: string): Promise<void> => {
  await axiosInstance.delete(`/api/v1/leave/types/${id}`);
};

// ========================================
// 員工餘假 / Leave balances
// ========================================
export const fetchLeaveBalances = async (employeeId: string, year?: number): Promise<LeaveBalance[]> => {
  const res = await axiosInstance.get('/api/v1/leave/balances', {
    params: { employeeId, year },
  });
  return res.data.data;
};

// ========================================
// 請假申請 / Leave requests
// ========================================
export const submitLeaveRequest = async (data: SubmitLeaveRequestPayload): Promise<LeaveRequest> => {
  const res = await axiosInstance.post('/api/v1/leave/requests', data);
  return res.data.data;
};

export const fetchLeaveRequests = async (employeeId: string): Promise<LeaveRequest[]> => {
  const res = await axiosInstance.get('/api/v1/leave/requests', { params: { employeeId } });
  return res.data.data;
};

export const cancelLeaveRequest = async (id: string): Promise<LeaveRequest> => {
  const res = await axiosInstance.post(`/api/v1/leave/requests/${id}/cancel`);
  return res.data.data;
};

// ========================================
// 部門請假日曆 / Leave calendar
// ========================================
export const fetchLeaveCalendar = async (startDate: string, endDate: string): Promise<LeaveRequest[]> => {
  const res = await axiosInstance.get('/api/v1/leave/requests/calendar', {
    params: { startDate, endDate },
  });
  return res.data.data;
};
