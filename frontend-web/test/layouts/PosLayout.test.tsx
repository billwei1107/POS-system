/**
 * @file PosLayout.test.tsx
 * @description POS 版面測試 / POS layout tests
 * @description_en Verifies POS layout renders active session identity labels
 * @description_zh 驗證 POS 版面會顯示目前工作階段的門店、終端與操作員
 */
import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import PosLayout from '../../src/layouts/PosLayout';

const renderPosLayout = () => render(
  <MemoryRouter initialEntries={['/pos/register']}>
    <Routes>
      <Route path="/pos" element={<PosLayout />}>
        <Route path="register" element={<div>Register outlet</div>} />
      </Route>
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
});
