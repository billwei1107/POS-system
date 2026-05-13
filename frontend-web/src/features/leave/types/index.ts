/**
 * @file index.ts
 * @description 請假管理模塊型別定義 / Leave management module type definitions
 * @description_en TypeScript types for leave types, balances, requests
 * @description_zh 請假類型、餘假、申請的 TypeScript 型別定義
 */

// ========================================
// 假別類型 / Leave type
// ========================================
export type PaidType = 'PAID' | 'UNPAID' | 'HALF_PAY';

export interface LeaveType {
  id: string;
  name: string;
  code: string;
  paidType: PaidType;
  requireAttachment: boolean;
  maxDaysPerYear: number | null;
  createdAt: string;
}

// ========================================
// 員工餘假 / Leave balance
// ========================================
export interface LeaveBalance {
  id: string;
  employeeId: string;
  leaveTypeId: string;
  year: number;
  totalDays: number;
  usedDays: number;
  remainingDays: number;
}

// ========================================
// 請假申請 / Leave request
// ========================================
export type HalfDay = 'FULL' | 'MORNING' | 'AFTERNOON';
export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface LeaveRequest {
  id: string;
  employeeId: string;
  leaveTypeId: string;
  startDate: string;
  endDate: string;
  startHalf: HalfDay;
  endHalf: HalfDay;
  totalHours: number;
  reason: string | null;
  delegateId: string | null;
  status: LeaveStatus;
  workflowInstanceId: string | null;
  createdAt: string;
}

// ========================================
// 請假申請 DTO / Submit request payload
// ========================================
export interface SubmitLeaveRequestPayload {
  employeeId: string;
  leaveTypeId: string;
  startDate: string;
  endDate: string;
  startHalf: HalfDay;
  endHalf: HalfDay;
  reason?: string;
  delegateId?: string;
}
