/**
 * @file authStore.test.ts
 * @description 認證持久化測試 / Auth persistence tests
 * @description_en Verifies persisted auth state and POS session cleanup
 * @description_zh 驗證認證狀態持久化與 POS session 清理
 */
import { beforeEach, describe, expect, it } from 'vitest';
import { useAuthStore, type User } from '../../src/shared/store/authStore';

const user: User = {
  id: 'employee-demo',
  username: 'cashier',
  email: 'cashier@example.test',
  role: 'POS_CASHIER',
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

describe('useAuthStore persistence', () => {
  it('persists only auth identity and token into auth-storage', () => {
    useAuthStore.getState().setAuth(user, 'token-123');

    const raw = window.localStorage.getItem('auth-storage');
    expect(raw).not.toBeNull();
    expect(JSON.parse(raw!)).toEqual({
      state: {
        user,
        token: 'token-123',
        isAuthenticated: true,
      },
      version: 0,
    });
  });

  it('clears auth state and POS PIN session on logout', () => {
    window.localStorage.setItem('pos-session', JSON.stringify({ employeeId: user.id }));
    useAuthStore.getState().setAuth(user, 'token-123');

    useAuthStore.getState().logout();

    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(state.token).toBeNull();
    expect(state.isAuthenticated).toBe(false);
    expect(window.localStorage.getItem('pos-session')).toBeNull();
  });
});
