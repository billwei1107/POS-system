/**
 * @file config.ts
 * @description POS 訂單前端設定 / POS order frontend configuration
 * @description_en Centralizes register defaults used by local checkout flows
 * @description_zh 集中管理本地 checkout 流程使用的門店、終端與操作員預設值
 */

export const DEFAULT_STORE_ID =
  import.meta.env.VITE_DEFAULT_STORE_ID || '00000000-0000-0000-0000-000000000001';

export const DEFAULT_TERMINAL_ID =
  import.meta.env.VITE_DEFAULT_TERMINAL_ID || '00000000-0000-0000-0000-000000000101';

export const DEFAULT_EMPLOYEE_ID =
  import.meta.env.VITE_DEFAULT_EMPLOYEE_ID || '00000000-0000-0000-0000-000000000201';

export const DEFAULT_TABLE_NO = import.meta.env.VITE_DEFAULT_TABLE_NO || 'A7';
export const DEFAULT_GUEST_COUNT = Number(import.meta.env.VITE_DEFAULT_GUEST_COUNT || 2);
