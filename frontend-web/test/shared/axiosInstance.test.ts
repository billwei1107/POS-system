/**
 * @file axiosInstance.test.ts
 * @description Axios 攔截器測試 / Axios interceptor tests
 * @description_en Verifies API-level unauthorized responses clear auth state
 * @description_zh 驗證 API body 回傳 401 時會清除登入狀態
 */
import { beforeEach, describe, expect, it } from 'vitest';
import axiosInstance from '../../src/shared/api/axiosInstance';
import { useAuthStore, type User } from '../../src/shared/store/authStore';

const user: User = {
  id: 'employee-demo',
  username: 'cashier',
  role: 'CASHIER',
};

beforeEach(() => {
  window.localStorage.clear();
  useAuthStore.setState({
    user: null,
    token: null,
    isAuthenticated: false,
    hasHydrated: true,
  });
});

describe('axiosInstance unauthorized handling', () => {
  it('logs out when an API response body reports code 401', async () => {
    window.localStorage.setItem('pos-session', JSON.stringify({ employeeId: user.id }));
    useAuthStore.setState({
      user,
      token: 'opaque-token',
      isAuthenticated: true,
      hasHydrated: true,
    });

    await expect(axiosInstance.get('/v1/pos/products', {
      adapter: async (config) => ({
        data: {
          code: 401,
          message: 'JWT expired',
          data: null,
          timestamp: '2026-05-16T19:05:00+08:00',
        },
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
      }),
    })).rejects.toThrow('JWT expired');

    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(window.localStorage.getItem('pos-session')).toBeNull();
  });
});
