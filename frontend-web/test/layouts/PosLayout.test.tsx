/**
 * @file PosLayout.test.tsx
 * @description POS 版面測試 / POS layout tests
 * @description_en Verifies POS layout renders active session identity labels
 * @description_zh 驗證 POS 版面會顯示目前工作階段的門店、終端與操作員
 */
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import PosLayout from '../../src/layouts/PosLayout';

const renderPosLayout = () => render(
  <MemoryRouter initialEntries={['/pos/register']}>
    <Routes>
      <Route path="/pos" element={<PosLayout />}>
        <Route path="register" element={<div>Register outlet</div>} />
      </Route>
      <Route path="/admin/dashboard" element={<div>Admin dashboard outlet</div>} />
    </Routes>
  </MemoryRouter>
);

beforeEach(() => {
  window.localStorage.clear();
});

describe('PosLayout', () => {
  it('renders store, terminal and operator labels from the POS session', () => {
    window.localStorage.setItem('pos-session', JSON.stringify({
      storeName: '信義旗艦店',
      terminalName: 'Browser Demo Terminal',
      terminalCode: 'DEMO-T-001',
      username: 'cashier',
      role: 'STORE_MANAGER',
    }));

    renderPosLayout();

    expect(screen.getByText('信義旗艦店')).toBeInTheDocument();
    expect(screen.getAllByText('Browser Demo Terminal').length).toBeGreaterThan(0);
    expect(screen.getByText('操作員 cashier')).toBeInTheDocument();
    expect(screen.getAllByText('店長').length).toBeGreaterThan(0);
  });

  it('keeps management modules out of the POS foreground navigation', async () => {
    const user = userEvent.setup();
    renderPosLayout();

    expect(screen.getByRole('button', { name: '進入後台' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '收銀台' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '訂單' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '商品' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '庫存' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '會員' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '班次' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '對帳' })).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: '管理後台' }));

    expect(await screen.findByText('Admin dashboard outlet')).toBeInTheDocument();
  });

  it('opens the admin dashboard from the foreground top bar', async () => {
    const user = userEvent.setup();
    renderPosLayout();

    await user.click(screen.getByRole('button', { name: '進入後台' }));

    expect(await screen.findByText('Admin dashboard outlet')).toBeInTheDocument();
  });

  it('collapses and expands the foreground desktop sidebar', async () => {
    const user = userEvent.setup();
    renderPosLayout();

    expect(screen.getByTestId('pos-sidebar')).toHaveAttribute('data-collapsed', 'false');
    expect(screen.getByTestId('pos-sidebar-menu-toggle-icon')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: '收合側邊欄' }));

    expect(screen.getByTestId('pos-sidebar')).toHaveAttribute('data-collapsed', 'true');
    expect(screen.getByRole('button', { name: '展開側邊欄' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '收銀台' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '管理後台' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: '展開側邊欄' }));

    expect(screen.getByTestId('pos-sidebar')).toHaveAttribute('data-collapsed', 'false');
    expect(screen.getByRole('button', { name: '收合側邊欄' })).toBeInTheDocument();
  });
});
