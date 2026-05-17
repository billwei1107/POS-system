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
import type { CreateOrderRequest, Order } from '../../src/features/pos-orders/types';

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

const selectedMember = {
  id: 'member-gold',
  memberNo: 'M0001',
  name: '金卡會員',
  phoneMasked: '0912***888',
  tier: 'GOLD',
  points: 1000,
  discountPercent: 10,
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
    discountSource: null,
    selectedMember: null,
    appliedPromotion: null,
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
    window.localStorage.setItem('pos-session', JSON.stringify({
      storeId: 'store-from-session',
      terminalId: 'terminal-from-session',
      employeeId: 'employee-from-session',
    }));
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
    expect(screen.getByText('拿鐵 12oz')).toBeInTheDocument();
    expect(screen.getByText('數量：1')).toBeInTheDocument();
    expect(screen.getByText('找零').parentElement).toHaveTextContent('$4');
    expect(screen.queryByText('購物車是空的')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: '付款已完成' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '返回收銀台' })).toBeInTheDocument();
    expect(screen.getByLabelText('收款金額')).toBeDisabled();
    expect(orderApiMock.create).toHaveBeenCalledWith(expect.objectContaining({
      storeId: 'store-from-session',
      terminalId: 'terminal-from-session',
      employeeId: 'employee-from-session',
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

  it('keeps member id in create order payload when promotion discount is applied', async () => {
    const user = userEvent.setup();
    useCartStore.setState({
      discountAmount: 14.5,
      discountSource: 'promotion',
      selectedMember,
      appliedPromotion: {
        ruleId: 'promo-rule-001',
        name: '咖啡滿百 9 折',
        code: 'CAFE20',
        discountAmount: 14.5,
      },
    });
    orderApiMock.create.mockResolvedValue({
      success: true,
      message: 'created',
      data: { ...completedOrder, status: 'DRAFT' },
      code: 200,
    });
    orderApiMock.complete.mockResolvedValue({
      success: true,
      message: 'completed',
      data: {
        ...completedOrder,
        discountTotal: 14.5,
        discountSource: 'PROMOTION',
        promotionRuleId: 'promo-rule-001',
        promotionCode: 'CAFE20',
        discountLabel: '咖啡滿百 9 折',
      },
      code: 200,
    });

    renderCheckout();
    await user.click(screen.getByRole('button', { name: '確認付款方式' }));

    await waitFor(() => expect(orderApiMock.create).toHaveBeenCalledTimes(1));
    const createPayload = orderApiMock.create.mock.calls[0]?.[0] as CreateOrderRequest;
    expect(createPayload.memberId).toBe('member-gold');
    expect(createPayload.discountAmount).toBe(14.5);
    expect(createPayload.discountSource).toBe('PROMOTION');
    expect(createPayload.promotionRuleId).toBe('promo-rule-001');
    expect(createPayload.promotionCode).toBe('CAFE20');
    expect(createPayload.discountLabel).toBe('咖啡滿百 9 折');
    await waitFor(() => expect(screen.getByText('付款完成')).toBeInTheDocument());
    expect(screen.getByText('GOLD · 金卡會員 · 1,000 點')).toBeInTheDocument();
    expect(screen.getByText('咖啡滿百 9 折 · CAFE20')).toBeInTheDocument();
    expect(screen.queryByText('購物車是空的')).not.toBeInTheDocument();
  });
});
