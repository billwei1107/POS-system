/**
 * @file PosSessionRoute.test.tsx
 * @description POS 工作階段路由守衛測試 / POS session route guard tests
 * @description_en Verifies stale POS sessions return to PIN login
 * @description_zh 驗證 POS 工作階段遺失時會導回 PIN 登入
 */
import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { PosSessionRoute } from '../../src/features/pos-auth/components/PosSessionRoute';
import { useAuthStore, type User } from '../../src/shared/store/authStore';

const user: User = {
  id: 'employee-demo',
  username: 'cashier',
  role: 'CASHIER',
};

const renderPosSessionRoute = () => render(
  <MemoryRouter initialEntries={['/pos/register?table=A7']}>
    <Routes>
      <Route
        path="/pos/register"
        element={(
          <PosSessionRoute>
            <div>POS register content</div>
          </PosSessionRoute>
        )}
      />
      <Route path="/pos/login" element={<div>POS PIN login page</div>} />
    </Routes>
  </MemoryRouter>
);

beforeEach(() => {
  window.localStorage.clear();
  useAuthStore.setState({
    user: null,
    token: null,
    isAuthenticated: false,
    hasHydrated: true,
  });
});

describe('PosSessionRoute', () => {
  it('clears stale auth and redirects to PIN login when the POS session is missing', async () => {
    useAuthStore.setState({
      user,
      token: 'opaque-token',
      isAuthenticated: true,
      hasHydrated: true,
    });

    renderPosSessionRoute();

    expect(await screen.findByText('POS PIN login page')).toBeInTheDocument();
    await waitFor(() => {
      expect(useAuthStore.getState().isAuthenticated).toBe(false);
    });
    expect(window.localStorage.getItem('pos-session')).toBeNull();
  });

  it('renders POS content when the terminal session is complete', () => {
    window.localStorage.setItem('pos-session', JSON.stringify({
      storeId: 'store-demo',
      terminalId: 'terminal-demo',
      employeeId: user.id,
    }));
    useAuthStore.setState({
      user,
      token: 'opaque-token',
      isAuthenticated: true,
      hasHydrated: true,
    });

    renderPosSessionRoute();

    expect(screen.getByText('POS register content')).toBeInTheDocument();
  });
});
