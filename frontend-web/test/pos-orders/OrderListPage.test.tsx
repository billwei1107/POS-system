/**
 * @file OrderListPage.test.tsx
 * @description 訂單列表頁測試 / Order list page tests
 * @description_en Verifies order list audit details for discount source metadata
 * @description_zh 驗證訂單列表詳情可檢視折扣來源稽核資訊
 */
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import OrderListPage from '../../src/features/pos-orders/pages/OrderListPage';
import type { Order } from '../../src/features/pos-orders/types';
import type { PaymentTransaction } from '../../src/features/pos-payment/types';
import type { Invoice } from '../../src/features/pos-tax/types';

const navigateMock = vi.hoisted(() => vi.fn());

vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>();
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

const orderApiMock = vi.hoisted(() => ({
  list: vi.fn(),
  void: vi.fn(),
}));

const paymentApiMock = vi.hoisted(() => ({
  getByOrder: vi.fn(),
}));

const invoiceApiMock = vi.hoisted(() => ({
  getByOrder: vi.fn(),
}));

vi.mock('../../src/features/pos-orders/api/orderApi', () => ({
  orderApi: orderApiMock,
}));

vi.mock('../../src/features/pos-payment/api/paymentApi', () => ({
  paymentApi: paymentApiMock,
}));

vi.mock('../../src/features/pos-tax/api/taxApi', () => ({
  invoiceApi: invoiceApiMock,
}));

const promotionOrder: Order = {
  id: 'order-001',
  orderNo: '000000-20260515221933-98365',
  storeId: 'store-001',
  terminalId: 'terminal-001',
  employeeId: 'employee-001',
  status: 'COMPLETED',
  orderType: 'DINE_IN',
  subtotal: 145,
  discountTotal: 14.5,
  discountSource: 'PROMOTION',
  promotionRuleId: 'promo-rule-001',
  promotionCode: 'CAFE20',
  discountLabel: '咖啡滿百 9 折',
  taxTotal: 6.53,
  roundingAdj: 0,
  grandTotal: 137,
  paidTotal: 137,
  changeGiven: 0,
  tableNo: 'A7',
  guestCount: 2,
  completedAt: '2026-05-16T08:20:00Z',
  createdAt: '2026-05-16T08:19:00Z',
  items: [{
    id: 'item-row-001',
    itemId: 'item-oat-latte',
    itemNameSnapshot: '燕麥拿鐵 12oz',
    skuSnapshot: 'DEMO-OAT-LATTE-12OZ',
    unitPrice: 145,
    quantity: 1,
    discountAmount: 0,
    lineTotal: 145,
  }],
};

const manualOrder: Order = {
  ...promotionOrder,
  id: 'order-002',
  orderNo: '000000-20260517145729-74938',
  discountTotal: 0,
  discountSource: null,
  promotionRuleId: null,
  promotionCode: null,
  discountLabel: null,
  grandTotal: 95,
  paidTotal: 95,
  items: [{
    id: 'item-row-002',
    itemId: 'item-americano',
    itemNameSnapshot: '美式咖啡 12oz',
    skuSnapshot: 'DEMO-AMERICANO-12OZ',
    unitPrice: 90,
    quantity: 1,
    discountAmount: 0,
    lineTotal: 90,
  }],
};

const paymentTransaction: PaymentTransaction = {
  id: 'txn-001',
  orderId: promotionOrder.id,
  storeId: promotionOrder.storeId,
  payMethodId: 'cash-method',
  methodType: 'CASH',
  amount: 137,
  tendered: 137,
  changeGiven: 0,
  status: 'SUCCESS',
  gatewayRef: null,
  processedAt: '2026-05-16T08:20:10Z',
};

const issuedInvoice: Invoice = {
  id: 'invoice-001',
  storeId: promotionOrder.storeId,
  orderId: promotionOrder.id,
  fullInvoiceNo: 'AA12345678',
  invoiceType: 'B2C',
  sellerId: '24536806',
  sellerName: 'POS Demo Store',
  buyerId: null,
  buyerName: null,
  buyerEmail: null,
  carrierType: null,
  salesAmount: 130.47,
  taxAmount: 6.53,
  totalAmount: 137,
  status: 'ISSUED',
  uploadStatus: 'PENDING',
  issueAt: '2026-05-16T08:20:12Z',
  voidAt: null,
  voidReason: null,
};

const renderOrderList = () => render(
  <MemoryRouter>
    <OrderListPage />
  </MemoryRouter>
);

beforeEach(() => {
  window.localStorage.clear();
  window.localStorage.setItem('pos-session', JSON.stringify({
    storeId: 'store-001',
    terminalId: 'terminal-001',
    employeeId: 'employee-001',
  }));
  orderApiMock.list.mockReset();
  orderApiMock.void.mockReset();
  paymentApiMock.getByOrder.mockReset();
  invoiceApiMock.getByOrder.mockReset();
  navigateMock.mockReset();
  orderApiMock.list.mockResolvedValue({
    success: true,
    message: 'ok',
    code: 200,
    data: {
      content: [promotionOrder, manualOrder],
      totalElements: 2,
      totalPages: 1,
      size: 20,
      number: 0,
    },
  });
  paymentApiMock.getByOrder.mockResolvedValue({
    success: true,
    message: 'ok',
    code: 200,
    data: [paymentTransaction],
  });
  invoiceApiMock.getByOrder.mockResolvedValue({
    success: true,
    message: 'ok',
    code: 200,
    data: issuedInvoice,
  });
});

describe('OrderListPage order details', () => {
  it('opens detail dialog with promotion discount audit metadata', async () => {
    const user = userEvent.setup();
    renderOrderList();

    await waitFor(() => expect(screen.getAllByText(promotionOrder.orderNo).length).toBeGreaterThan(0));
    await user.click(screen.getAllByRole('button', { name: '詳情' })[0]);

    const dialog = await screen.findByRole('dialog', { name: `訂單詳情 ${promotionOrder.orderNo}` });
    const scopedDialog = within(dialog);

    expect(scopedDialog.getByText('折扣稽核')).toBeInTheDocument();
    expect(scopedDialog.getByText('促銷折扣')).toBeInTheDocument();
    expect(scopedDialog.getByText('咖啡滿百 9 折')).toBeInTheDocument();
    expect(scopedDialog.getByText('promo-rule-001')).toBeInTheDocument();
    expect(scopedDialog.getByText('CAFE20')).toBeInTheDocument();
    expect(scopedDialog.getByText('燕麥拿鐵 12oz')).toBeInTheDocument();
    expect(scopedDialog.getByText('DEMO-OAT-LATTE-12OZ')).toBeInTheDocument();
  });

  it('filters the visible order list by order number keyword', async () => {
    const user = userEvent.setup();
    renderOrderList();

    await waitFor(() => expect(screen.getAllByText(promotionOrder.orderNo).length).toBeGreaterThan(0));
    expect(screen.getAllByText(manualOrder.orderNo).length).toBeGreaterThan(0);

    await user.type(screen.getByPlaceholderText('依訂單編號搜尋'), '145729');

    expect(screen.queryAllByText(promotionOrder.orderNo)).toHaveLength(0);
    expect(screen.getAllByText(manualOrder.orderNo).length).toBeGreaterThan(0);

    await user.clear(screen.getByPlaceholderText('依訂單編號搜尋'));
    await user.type(screen.getByPlaceholderText('依訂單編號搜尋'), 'NO-MATCH');

    expect(screen.getAllByText('找不到符合條件的訂單').length).toBeGreaterThan(0);
  });

  it('navigates to the register page when creating a new order', async () => {
    const user = userEvent.setup();
    renderOrderList();

    await user.click(screen.getByRole('button', { name: '新增訂單' }));

    expect(navigateMock).toHaveBeenCalledWith('/pos/register');
  });
});
