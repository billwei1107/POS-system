/**
 * @file AdminLayout.test.tsx
 * @description 後台版面測試 / Admin layout tests
 * @description_en Verifies selected admin navigation contrast in light mode
 * @description_zh 驗證淺色模式後台選取導覽項目的文字對比
 */
import { ThemeProvider } from '@mui/material';
import { render, screen } from '@testing-library/react';
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
});
