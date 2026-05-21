/**
 * @file LoginPage.test.tsx
 * @description 登入頁品牌測試 / Login page brand tests
 * @description_en Ensures the shared login entry uses the POS project brand
 * @description_zh 確認共用登入入口使用 POS 專案品牌文案
 */
import { render, screen } from '@testing-library/react';
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
});
