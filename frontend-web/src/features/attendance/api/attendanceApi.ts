/**
 * @file attendanceApi.ts
 * @description 打卡考勤 API 請求層 / Attendance API request layer
 * @description_zh 封裝打卡、班表、圍欄、假日等 REST 呼叫
 */

import axiosInstance from '../../../shared/api/axiosInstance';
import type { AttendanceRecord, ClockInRequest, ShiftSchedule, Geofence, Holiday, CorrectionRequest } from '../types';

// ========================================
// 打卡相關 / Clock In/Out
// ========================================
export const clockIn = async (data: ClockInRequest): Promise<AttendanceRecord> => {
    const response = await axiosInstance.post('/v1/attendance/clock-in', data);
    return response.data;
};

export const clockOut = async (): Promise<AttendanceRecord> => {
    const response = await axiosInstance.post('/v1/attendance/clock-out');
    return response.data;
};

export const getTodayRecord = async (): Promise<AttendanceRecord | null> => {
    const response = await axiosInstance.get('/v1/attendance/today');
    return response.data;
};

export const getMonthlyRecords = async (employeeId: string, year: number, month: number): Promise<AttendanceRecord[]> => {
    const response = await axiosInstance.get('/v1/attendance/records', {
        params: { employeeId, year, month }
    });
    return response.data;
};

// ========================================
// 補卡申請 / Corrections
// ========================================
export const submitCorrection = async (data: CorrectionRequest): Promise<void> => {
    await axiosInstance.post('/v1/attendance/corrections', data);
};

// ========================================
// 班表管理 / Shift Management
// ========================================
export const getShifts = async (): Promise<ShiftSchedule[]> => {
    const response = await axiosInstance.get('/v1/attendance/shifts');
    return response.data;
};

export const createShift = async (data: Partial<ShiftSchedule>): Promise<ShiftSchedule> => {
    const response = await axiosInstance.post('/v1/attendance/shifts', data);
    return response.data;
};

export const updateShift = async (id: string, data: Partial<ShiftSchedule>): Promise<ShiftSchedule> => {
    const response = await axiosInstance.put(`/v1/attendance/shifts/${id}`, data);
    return response.data;
};

export const deleteShift = async (id: string): Promise<void> => {
    await axiosInstance.delete(`/v1/attendance/shifts/${id}`);
};

export const assignShift = async (shiftId: string, employeeId: string, effectiveDate: string, endDate?: string): Promise<void> => {
    await axiosInstance.post(`/v1/attendance/shifts/${shiftId}/assign`, null, {
        params: { employeeId, effectiveDate, endDate }
    });
};

// ========================================
// 地理圍欄 / Geofences
// ========================================
export const getGeofences = async (): Promise<Geofence[]> => {
    const response = await axiosInstance.get('/v1/attendance/geofences');
    return response.data;
};

export const createGeofence = async (data: Partial<Geofence>): Promise<Geofence> => {
    const response = await axiosInstance.post('/v1/attendance/geofences', data);
    return response.data;
};

export const updateGeofence = async (id: string, data: Partial<Geofence>): Promise<Geofence> => {
    const response = await axiosInstance.put(`/v1/attendance/geofences/${id}`, data);
    return response.data;
};

export const deleteGeofence = async (id: string): Promise<void> => {
    await axiosInstance.delete(`/v1/attendance/geofences/${id}`);
};

// ========================================
// 假日管理 / Holidays
// ========================================
export const getHolidays = async (year: number): Promise<Holiday[]> => {
    const response = await axiosInstance.get('/v1/attendance/holidays', { params: { year } });
    return response.data;
};

export const createHoliday = async (data: Partial<Holiday>): Promise<Holiday> => {
    const response = await axiosInstance.post('/v1/attendance/holidays', data);
    return response.data;
};

export const updateHoliday = async (id: string, data: Partial<Holiday>): Promise<Holiday> => {
    const response = await axiosInstance.put(`/v1/attendance/holidays/${id}`, data);
    return response.data;
};

export const deleteHoliday = async (id: string): Promise<void> => {
    await axiosInstance.delete(`/v1/attendance/holidays/${id}`);
};
