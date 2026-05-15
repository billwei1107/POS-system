/**
 * @file posSession.ts
 * @description POS 終端工作階段 / POS terminal session context
 * @description_en Reads the active POS login session and resolves store, terminal and employee context
 * @description_zh 讀取目前 POS 登入工作階段，提供門店、終端與員工脈絡
 */
import { DEFAULT_EMPLOYEE_ID, DEFAULT_STORE_ID, DEFAULT_TERMINAL_ID } from './config';

const POS_SESSION_STORAGE_KEY = 'pos-session';

export interface PosSession {
  storeId?: string | null;
  storeName?: string | null;
  terminalId?: string | null;
  employeeId?: string | null;
  terminalCode?: string | null;
  terminalName?: string | null;
  userId?: string | null;
  username?: string | null;
  role?: string | null;
}

export interface ActivePosContext {
  storeId: string;
  terminalId: string;
  employeeId: string;
}

const hasValue = (value: string | null | undefined): value is string =>
  typeof value === 'string' && value.trim().length > 0;

// ========================================
// Session 讀取 / Session Reading
// ========================================
export const readPosSession = (): PosSession | null => {
  if (typeof window === 'undefined') return null;

  const raw = window.localStorage.getItem(POS_SESSION_STORAGE_KEY);
  if (!raw) return null;

  try {
    return JSON.parse(raw) as PosSession;
  } catch {
    return null;
  }
};

// ========================================
// POS 操作脈絡 / POS Operating Context
// ========================================
export const getActivePosContext = (): ActivePosContext => {
  const session = readPosSession();

  return {
    storeId: hasValue(session?.storeId) ? session.storeId : DEFAULT_STORE_ID,
    terminalId: hasValue(session?.terminalId) ? session.terminalId : DEFAULT_TERMINAL_ID,
    employeeId: hasValue(session?.employeeId) ? session.employeeId : DEFAULT_EMPLOYEE_ID,
  };
};
