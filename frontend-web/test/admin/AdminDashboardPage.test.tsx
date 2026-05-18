/**
 * @file AdminDashboardPage.test.tsx
 * @description 後台首頁測試 / Admin dashboard tests
 * @description_en Verifies foreground and back office entry grouping
 * @description_zh 驗證後台首頁會區分門市前台與後台管理入口
 */
import { render, screen, within } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import AdminDashboardPage from '../../src/features/admin/pages/AdminDashboardPage';

const renderDashboard = () => render(
  <MemoryRouter>
    <AdminDashboardPage />
  </MemoryRouter>
);

describe('AdminDashboardPage', () => {
  it('separates storefront POS actions from back office management links', () => {
    renderDashboard();

    const storefrontSection = screen.getByText('門市前台').closest('.MuiCard-root');
    const backOfficeSection = screen.getByText('後台管理', { selector: 'h5' }).closest('.MuiCard-root');

    expect(storefrontSection).not.toBeNull();
    expect(backOfficeSection).not.toBeNull();

    expect(within(storefrontSection!).getByRole('link', { name: '進入收銀台' })).toHaveAttribute('href', '/pos/register');
    expect(within(storefrontSection!).getByRole('link', { name: '門市訂單' })).toHaveAttribute('href', '/pos/orders');
    expect(within(storefrontSection!).getByRole('link', { name: '退款處理' })).toHaveAttribute('href', '/pos/refunds');

    expect(within(backOfficeSection!).getByRole('link', { name: '商品管理' })).toHaveAttribute('href', '/admin/pos/products');
    expect(within(backOfficeSection!).getByRole('link', { name: '會員管理' })).toHaveAttribute('href', '/admin/pos/members');
    expect(within(backOfficeSection!).getByRole('link', { name: '庫存總覽' })).toHaveAttribute('href', '/admin/inventory/overview');
  });
});
