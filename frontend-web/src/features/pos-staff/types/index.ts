/**
 * @file index.ts
 * @description POS 排班模組類型定義 / POS staff module type definitions
 * @description_en TypeScript interfaces and enums for staff shifts, schedules, clock records, and reports
 * @description_zh 班次、排班、打卡記錄與報表的 TypeScript 型別定義
 */

// ========================================
// 班次狀態列舉 / Shift status enum
// ========================================
export type ShiftStatus = 'OPEN' | 'CLOSED' | 'BLIND_CLOSED';

// ========================================
// 打卡類型列舉 / Clock type enum
// ========================================
export type ClockType = 'IN' | 'OUT' | 'BREAK_START' | 'BREAK_END';

// ========================================
// 排班狀態列舉 / Schedule status enum
// ========================================
export type ScheduleStatus = 'SCHEDULED' | 'CONFIRMED' | 'ABSENT' | 'SWAPPED';

// ========================================
// 班次 / Staff shift
// ========================================
export interface StaffShift {
  id: string;
  storeId: string;
  employeeId: string;
  terminalId: string | null;
  shiftNo: string;
  status: ShiftStatus;
  openedAt: string;
  closedAt: string | null;
  openingCash: number;
  closingCash: number | null;
  expectedCash: number | null;
  cashVariance: number | null;
  totalSales: number;
  totalRefunds: number;
  totalDiscounts: number;
  totalTax: number;
  netSales: number;
  transactionCount: number;
  notes: string | null;
}

// ========================================
// X Report / X Report snapshot
// ========================================
export interface XReport {
  id: string;
  shiftId: string;
  storeId: string;
  employeeId: string;
  generatedAt: string;
  periodStart: string;
  periodEnd: string;
  totalSales: number;
  totalRefunds: number;
  totalDiscounts: number;
  totalTax: number;
  netSales: number;
  cashSales: number;
  cardSales: number;
  otherSales: number;
  transactionCount: number;
  refundCount: number;
  voidCount: number;
}

// ========================================
// Z Report / Z Report daily close
// ========================================
export interface ZReport {
  id: string;
  storeId: string;
  reportDate: string;
  reportNo: string;
  generatedAt: string;
  totalSales: number;
  totalRefunds: number;
  totalDiscounts: number;
  totalTax: number;
  netSales: number;
  grossSales: number;
  cashInDrawer: number;
  expectedCash: number;
  cashVariance: number;
  transactionCount: number;
  shiftCount: number;
  contentHash: string;
  hashValid: boolean;
}

// ========================================
// 排班計劃 / Staff schedule
// ========================================
export interface StaffSchedule {
  id: string;
  storeId: string;
  employeeId: string;
  workDate: string;
  plannedStart: string;
  plannedEnd: string;
  actualShiftId: string | null;
  status: ScheduleStatus;
  notes: string | null;
}

// ========================================
// 請求類型 / Request types
// ========================================
export interface OpenShiftPayload {
  employeeId: string;
  terminalId?: string;
  openingCash?: number;
}

export interface CloseShiftPayload {
  closingCash: number;
  notes?: string;
}

export interface GenerateZReportPayload {
  reportDate: string;
  cashInDrawer: number;
  generatedBy: string;
}
