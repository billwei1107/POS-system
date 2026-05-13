/**
 * @file CheckoutPage.test.tsx
 * @description POS 結帳頁測試 / POS checkout page tests
 * @description_en Verifies cash tendering and order completion behavior
 * @description_zh 驗證現金收款找零與訂單完成行為
 */
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CheckoutPage from '../../src/features/pos-orders/pages/CheckoutPage';
import { useCartStore } from '../../src/features/pos-orders/store/cartStore';
import type { Order } from '../../src/features/pos-orders/types';

const orderApiMock = vi.hoisted(() => ({
  create: vi.fn(),
  complete: vi.fn(),
}));

vi.mock('../../src/features/pos-orders/api/orderApi', () => ({
  orderApi: orderApiMock,
}));

const completedOrder: Order = {
  id: 'order-001',
  orderNo: '000000-20260511032000-0001',
  storeId: '00000000-0000-0000-0000-000000000001',
  terminalId: '00000000-0000-0000-0000-000000000011',
  employeeId: '00000000-0000-0000-0000-000000000101',
  status: 'COMPLETED',
  orderType: 'DINE_IN',
  subtotal: 120,
  discountTotal: 0,
  taxTotal: 6,
  roundingAdj: 0,
  grandTotal: 126,
  paidTotal: 126,
  changeGiven: 4,
  createdAt: '2026-05-11T03:20:00Z',
  completedAt: '2026-05-11T03:21:00Z',
  items: [],
};

const renderCheckout = () => render(
  <MemoryRouter>
    <CheckoutPage />
  </MemoryRouter>
);

beforeEach(() => {
  window.localStorage.clear();
  orderApiMock.create.mockReset();
  orderApiMock.complete.mockReset();
  useCartStore.setState({
    lines: [{
      itemId: 'item-latte',
      sku: 'DEMO-LATTE',
      name: '拿鐵 12oz',
      unitPrice: 120,
      quantity: 1,
      imageUrl: null,
    }],
    taxRate: 0.05,
    discountAmount: 0,
    selectedMember: null,
    heldOrders: [],
  });
});

describe('CheckoutPage cash payment', () => {
  it('blocks cash payment while tendered amount is below order total', async () => {
    const user = userEvent.setup();
    renderCheckout();

    const tenderedInput = screen.getByLabelText('收款金額');
    await user.clear(tenderedInput);
    await user.type(tenderedInput, '100');

    expect(screen.getByText('不足')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '確認付款方式' })).toBeDisabled();
    expect(orderApiMock.create).not.toHaveBeenCalled();
  });

  it('creates and completes an order with CASH tendered amount and clears the cart', async () => {
    const user = userEvent.setup();
    orderApiMock.create.mockResolvedValue({
      success: true,
      message: 'created',
      data: { ...completedOrder, status: 'DRAFT' },
      code: 200,
    });
    orderApiMock.complete.mockResolvedValue({
      success: true,
      message: 'completed',
      data: completedOrder,
      code: 200,
    });
    renderCheckout();

    const tenderedInput = screen.getByLabelText('收款金額');
    await user.clear(tenderedInput);
    await user.type(tenderedInput, '130');
    await user.click(screen.getByRole('button', { name: '確認付款方式' }));

    await waitFor(() => expect(screen.getByText('付款完成')).toBeInTheDocument());
    expect(orderApiMock.create).toHaveBeenCalledWith(expect.objectContaining({
      taxIncluded: false,
      discountAmount: 0,
      items: [expect.objectContaining({
        itemId: 'item-latte',
        itemNameSnapshot: '拿鐵 12oz',
        skuSnapshot: 'DEMO-LATTE',
        unitPrice: 120,
        quantity: 1,
      })],
    }));
    expect(orderApiMock.complete).toHaveBeenCalledWith('order-001', 'CASH', 130);
    expect(useCartStore.getState().lines).toHaveLength(0);
  });
});
