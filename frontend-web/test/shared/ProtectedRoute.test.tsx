/**
 * @file ProtectedRoute.test.tsx
 * @description 路由守衛測試 / Protected route tests
 * @description_en Verifies expired sessions are redirected to login
 * @description_zh 驗證 JWT 過期時會清除工作階段並導回登入頁
 */
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { ProtectedRoute } from '../../src/shared/auth';
import { useAuthStore, type User } from '../../src/shared/store/authStore';

const user: User = {
  id: 'employee-demo',
  username: 'cashier',
  role: 'CASHIER',
};

const createJwt = (payload: Record<string, unknown>) => {
  const base64UrlEncode = (value: string) => btoa(value)
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');

  return [
    base64UrlEncode(JSON.stringify({ alg: 'HS256', typ: 'JWT' })),
    base64UrlEncode(JSON.stringify(payload)),
    'signature',
  ].join('.');
};

const renderProtectedPosRoute = () => render(
  <MemoryRouter initialEntries={['/pos/register']}>
    <Routes>
      <Route
        path="/pos/register"
        element={(
          <ProtectedRoute redirectTo="/pos/login">
            <div>POS protected content</div>
          </ProtectedRoute>
        )}
      />
      <Route path="/pos/login" element={<div>POS login page</div>} />
    </Routes>
  </MemoryRouter>
);

const renderProtectedAdminRoute = () => render(
  <MemoryRouter initialEntries={['/admin/access/users']}>
    <Routes>
      <Route
        path="/admin/access/users"
        element={(
          <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
            <div>Account management</div>
          </ProtectedRoute>
        )}
      />
      <Route path="/login" element={<LoginRouteProbe />} />
    </Routes>
  </MemoryRouter>
);

const LoginRouteProbe = () => {
  const location = useLocation();
  return <div>Admin login page {location.search}</div>;
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

describe('ProtectedRoute', () => {
  it('clears expired POS auth and redirects to POS login', async () => {
    window.localStorage.setItem('pos-session', JSON.stringify({ employeeId: user.id }));
    useAuthStore.setState({
      user,
      token: createJwt({ exp: 1_700_000_000 }),
      isAuthenticated: true,
      hasHydrated: true,
    });

    renderProtectedPosRoute();

    expect(await screen.findByText('POS login page')).toBeInTheDocument();
    await waitFor(() => {
      expect(useAuthStore.getState().isAuthenticated).toBe(false);
    });
    expect(window.localStorage.getItem('pos-session')).toBeNull();
  });

  it('requires a system admin role for protected admin-only pages', async () => {
    window.localStorage.setItem('pos-session', JSON.stringify({ employeeId: user.id }));
    useAuthStore.setState({
      user: { ...user, role: 'STORE_MANAGER' },
      token: createJwt({ role: 'STORE_MANAGER', exp: 4_000_000_000 }),
      isAuthenticated: true,
      hasHydrated: true,
    });

    renderProtectedAdminRoute();

    expect(await screen.findByText(/Admin login page/)).toHaveTextContent('reason=admin-permission');
  });
});
