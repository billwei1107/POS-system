/**
 * @file AdminLayout.test.tsx
 * @description 後台版面測試 / Admin layout tests
 * @description_en Verifies selected admin navigation contrast and sidebar collapse behavior
 * @description_zh 驗證淺色模式後台選取導覽項目的文字對比與側邊欄收合行為
 */
import { ThemeProvider } from '@mui/material';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import AdminLayout from '../../src/layouts/AdminLayout';
import { createPosTheme } from '../../src/shared/theme';

const renderAdminLayout = () => render(
  <ThemeProvider theme={createPosTheme('light')}>
    <MemoryRouter initialEntries={['/admin/pos/products']}>
      <Routes>
        <Route path="/admin" element={<AdminLayout />}>
          <Route path="pos/products" element={<div>Products outlet</div>} />
        </Route>
      </Routes>
    </MemoryRouter>
  </ThemeProvider>
);

describe('AdminLayout', () => {
  it('uses dark selected navigation text in light mode', () => {
    renderAdminLayout();

    const selectedLinks = screen.getAllByRole('link', { name: '商品管理' });

    selectedLinks.forEach((selectedLink) => {
      expect(selectedLink).toHaveClass('Mui-selected');
      expect(selectedLink).toHaveStyle({ color: '#7C2D12' });
    });
  });

  it('collapses and expands desktop sidebar navigation', async () => {
    const user = userEvent.setup();
    renderAdminLayout();

    expect(screen.getByTestId('admin-sidebar-menu-toggle-icon')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: '收合側邊欄' }));

    expect(screen.getByRole('button', { name: '展開側邊欄' })).toBeInTheDocument();
    expect(screen.getByTestId('admin-sidebar-menu-toggle-icon')).toBeInTheDocument();
    expect(screen.getAllByRole('link', { name: '商品管理' }).length).toBeGreaterThan(0);

    await user.click(screen.getByRole('button', { name: '展開側邊欄' }));

    expect(screen.getByRole('button', { name: '收合側邊欄' })).toBeInTheDocument();
    expect(screen.getByTestId('admin-sidebar-menu-toggle-icon')).toBeInTheDocument();
  });
});
