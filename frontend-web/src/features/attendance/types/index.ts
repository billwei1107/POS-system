/**
 * @file index.ts
 * @description 打卡考勤型別定義 / Attendance type definitions
 * @description_zh 定義打卡記錄、班表、地理圍欄等介面
 */

export interface AttendanceRecord {
    id: string;
    employeeId: string;
    date: string;
    clockInTime: string | null;
    clockOutTime: string | null;
    clockInMethod: string;
    status: 'NORMAL' | 'LATE' | 'EARLY_LEAVE' | 'ABSENT';
    overtimeMinutes: number;
}

export interface ClockInRequest {
    employeeId?: string;
    latitude: number;
    longitude: number;
    method?: string;
    wifiBssid?: string;
}

export interface ShiftSchedule {
    id: string;
    name: string;
    type: 'FIXED' | 'FLEXIBLE' | 'ROTATING';
    startTime: string;
    endTime: string;
    graceMinutes: number;
    breakStart: string | null;
    breakEnd: string | null;
}

export interface Geofence {
    id: string;
    name: string;
    latitude: number;
    longitude: number;
    radiusMeters: number;
    wifiBssids: string | null;
    active: boolean;
}

export interface Holiday {
    id: string;
    date: string;
    name: string;
    type: 'NATIONAL' | 'COMPANY';
    year: number;
}

export interface CorrectionRequest {
    employeeId?: string;
    recordId?: string;
    reason: string;
    correctedTime: string;
    correctionType: 'CLOCK_IN' | 'CLOCK_OUT';
}
