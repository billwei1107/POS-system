/**
 * @file LoginPage.test.tsx
 * @description 登入頁品牌測試 / Login page brand tests
 * @description_en Ensures the shared login entry uses the POS project brand
 * @description_zh 確認共用登入入口使用 POS 專案品牌文案
 */
import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { LoginPage } from '../../src/features/auth/pages/LoginPage';
import { useAuthStore } from '../../src/shared/store/authStore';

const renderLoginPage = () => render(
  <MemoryRouter initialEntries={['/login?redirect=/admin/dashboard']}>
    <LoginPage />
  </MemoryRouter>
);

describe('LoginPage', () => {
  beforeEach(() => {
    useAuthStore.setState({
      user: null,
      token: null,
      isAuthenticated: false,
      hasHydrated: true,
    });
  });

  it('uses Titanium POS branding on the shared login page', () => {
    renderLoginPage();

    expect(screen.getByRole('heading', { name: 'Titanium POS' })).toBeInTheDocument();
    expect(screen.getByText('咖啡門市收銀與後台管理系統')).toBeInTheDocument();
    expect(screen.getByText('© 2026 Titanium POS. All rights reserved.')).toBeInTheDocument();
    expect(screen.queryByText('HIVE.ERP')).not.toBeInTheDocument();
    expect(screen.queryByText('企業模塊化組件系統')).not.toBeInTheDocument();
  });

  it('clears POS sessions when admin-only pages require a fresh admin login', async () => {
    window.localStorage.setItem('pos-session', JSON.stringify({ userId: 'cashier-demo' }));
    useAuthStore.setState({
      user: { id: 'cashier-demo', username: 'cashier', role: 'STORE_MANAGER' },
      token: 'token-123',
      isAuthenticated: true,
      hasHydrated: true,
    });

    render(
      <MemoryRouter initialEntries={['/login?redirect=/admin/access/users&reason=admin-permission']}>
        <LoginPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(useAuthStore.getState().isAuthenticated).toBe(false);
    });
    expect(window.localStorage.getItem('pos-session')).toBeNull();
    expect(screen.getByText('此功能需要系統管理員帳號，請重新登入後台。')).toBeInTheDocument();
  });
});
