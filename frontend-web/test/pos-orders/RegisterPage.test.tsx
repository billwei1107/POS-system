/**
 * @file RegisterPage.test.tsx
 * @description 收銀台頁面測試 / Register page tests
 * @description_en Verifies barcode search mode and product loading behavior.
 * @description_zh 驗證條碼搜尋模式與商品載入行為。
 */
import { MemoryRouter } from 'react-router-dom';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import RegisterPage from '../../src/features/pos-orders/pages/RegisterPage';
import { productApi } from '../../src/features/pos-products/api/productApi';
import { useAuthStore } from '../../src/shared/store/authStore';
import { useCartStore } from '../../src/features/pos-orders/store/cartStore';
import type { Category, ProductItem } from '../../src/features/pos-products/types';

vi.mock('../../src/features/pos-products/api/productApi', () => ({
  productApi: {
    getProducts: vi.fn(),
    getCategories: vi.fn(),
  },
}));

const productApiMock = vi.mocked(productApi);

const coffeeCategory: Category = {
  id: 'category-coffee',
  name: '咖啡飲品',
  parentId: null,
  sortOrder: 1,
  imageUrl: null,
  displayColor: null,
  active: true,
  createdAt: '2026-05-17T00:00:00Z',
  updatedAt: '2026-05-17T00:00:00Z',
};

const americano: ProductItem = {
  id: 'item-americano',
  sku: 'DEMO-AMERICANO-12OZ',
  name: '美式咖啡 12oz',
  description: null,
  categoryId: coffeeCategory.id,
  basePrice: 90,
  costPrice: null,
  taxClassId: null,
  unit: 'PCS',
  barcodePrimary: '4710000000012',
  imageUrl: null,
  trackInventory: true,
  sellable: true,
  weightBased: false,
  active: true,
  createdAt: '2026-05-17T00:00:00Z',
  updatedAt: '2026-05-17T00:00:00Z',
};

const renderRegisterPage = () => render(
  <MemoryRouter>
    <RegisterPage />
  </MemoryRouter>
);

beforeEach(() => {
  window.localStorage.clear();
  vi.clearAllMocks();
  useAuthStore.setState({
    user: { id: 'employee-001', username: 'cashier' },
    token: 'demo-token',
    isAuthenticated: true,
    hasHydrated: true,
  });
  useCartStore.getState().clear();
  productApiMock.getCategories.mockResolvedValue({
    success: true,
    code: 200,
    message: 'ok',
    data: [coffeeCategory],
  });
  productApiMock.getProducts.mockResolvedValue({
    success: true,
    code: 200,
    message: 'ok',
    data: {
      content: [americano],
      totalElements: 1,
      totalPages: 1,
      size: 60,
      number: 0,
    },
  });
});

describe('RegisterPage barcode search mode', () => {
  it('submits typed keyword through the touch search button', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    expect(await screen.findByText('美式咖啡 12oz')).toBeInTheDocument();

    const searchInput = screen.getByPlaceholderText('搜尋商品、SKU 或條碼');
    await user.type(searchInput, 'DEMO-AMERICANO');
    await user.click(screen.getByRole('button', { name: '搜尋商品' }));

    await waitFor(() => {
      expect(productApiMock.getProducts).toHaveBeenLastCalledWith(expect.objectContaining({
        page: 0,
        size: 60,
        keyword: 'DEMO-AMERICANO',
      }));
    });
  });

  it('focuses the search field and submits barcode keyword with Enter', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    expect(await screen.findByText('美式咖啡 12oz')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: '掃描條碼' }));

    const searchInput = screen.getByPlaceholderText('搜尋商品、SKU 或條碼');
    expect(searchInput).toHaveFocus();
    expect(screen.getByText('掃描模式已啟動，請掃描條碼或輸入條碼後按 Enter。')).toBeInTheDocument();

    await user.type(searchInput, '4710000000012{Enter}');

    await waitFor(() => {
      expect(productApiMock.getProducts).toHaveBeenLastCalledWith(expect.objectContaining({
        page: 0,
        size: 60,
        keyword: '4710000000012',
      }));
    });
  });
});
