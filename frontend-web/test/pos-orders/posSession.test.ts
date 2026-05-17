/**
 * @file posSession.test.ts
 * @description POS 工作階段測試 / POS session tests
 * @description_en Verifies active POS context resolution from local session storage
 * @description_zh 驗證 POS 操作脈絡會優先使用本地登入工作階段
 */
import { beforeEach, describe, expect, it } from 'vitest';
import { getActivePosContext, hasActivePosSession, readPosSession } from '../../src/features/pos-orders/posSession';

beforeEach(() => {
  window.localStorage.clear();
});

describe('posSession', () => {
  it('resolves store, terminal and employee from the active POS session', () => {
    window.localStorage.setItem('pos-session', JSON.stringify({
      storeId: 'store-from-session',
      terminalId: 'terminal-from-session',
      employeeId: 'employee-from-session',
      username: 'cashier',
    }));

    expect(readPosSession()).toEqual(expect.objectContaining({
      storeId: 'store-from-session',
      terminalId: 'terminal-from-session',
      employeeId: 'employee-from-session',
    }));
    expect(getActivePosContext()).toEqual({
      storeId: 'store-from-session',
      terminalId: 'terminal-from-session',
      employeeId: 'employee-from-session',
    });
    expect(hasActivePosSession()).toBe(true);
  });

  it('falls back to local demo defaults when the POS session is missing or invalid', () => {
    expect(getActivePosContext()).toEqual({
      storeId: '00000000-0000-0000-0000-000000000001',
      terminalId: '00000000-0000-0000-0000-000000000101',
      employeeId: '00000000-0000-0000-0000-000000000201',
    });

    window.localStorage.setItem('pos-session', '{bad-json');

    expect(readPosSession()).toBeNull();
    expect(hasActivePosSession()).toBe(false);
    expect(getActivePosContext()).toEqual({
      storeId: '00000000-0000-0000-0000-000000000001',
      terminalId: '00000000-0000-0000-0000-000000000101',
      employeeId: '00000000-0000-0000-0000-000000000201',
    });
  });

  it('requires store, terminal and employee ids before treating a session as active', () => {
    window.localStorage.setItem('pos-session', JSON.stringify({
      storeId: 'store-from-session',
      terminalId: 'terminal-from-session',
      username: 'cashier',
    }));

    expect(readPosSession()).toEqual(expect.objectContaining({
      storeId: 'store-from-session',
      terminalId: 'terminal-from-session',
    }));
    expect(hasActivePosSession()).toBe(false);
  });
});
