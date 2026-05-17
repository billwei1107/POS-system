/**
 * @file CartPromotion.test.tsx
 * @description 購物車促銷測試 / Cart promotion tests
 * @description_en Verifies automatic promotion evaluation inside the register cart
 * @description_zh 驗證收銀台購物車會自動試算並套用促銷
 */
import { MemoryRouter } from 'react-router-dom';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import Cart from '../../src/features/pos-orders/components/Cart';
import { useCartStore } from '../../src/features/pos-orders/store/cartStore';
import { promotionApi } from '../../src/features/pos-promotion/api/promotionApi';

vi.mock('../../src/features/pos-orders/api/orderApi', () => ({
  heldOrderApi: {
    create: vi.fn(),
    list: vi.fn(),
    remove: vi.fn(),
  },
}));

vi.mock('../../src/features/pos-crm/api/memberApi', () => ({
  memberApi: {
    search: vi.fn(),
  },
}));

vi.mock('../../src/features/pos-promotion/api/promotionApi', () => ({
  promotionApi: {
    evaluate: vi.fn(),
  },
}));

const mockPromotionApi = vi.mocked(promotionApi);

const renderCart = () => render(
  <MemoryRouter>
    <Cart />
  </MemoryRouter>
);

beforeEach(() => {
  vi.clearAllMocks();
  window.localStorage.clear();
  mockPromotionApi.evaluate.mockResolvedValue({
    success: true,
    code: 200,
    message: 'Success',
    data: {
      applied: true,
      ruleId: 'promo-auto-001',
      name: '咖啡滿百 9 折',
      code: null,
      discountType: 'PERCENT',
      discountValue: 10,
      discountAmount: 14.5,
      reason: null,
    },
  });
  useCartStore.setState({
    lines: [{
      itemId: 'item-latte',
      sku: 'DEMO-LATTE',
      name: '燕麥拿鐵 12oz',
      unitPrice: 145,
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

describe('Cart promotion evaluation', () => {
  it('automatically applies the best promotion discount', async () => {
    renderCart();

    await waitFor(() => {
      expect(mockPromotionApi.evaluate).toHaveBeenCalledWith(expect.objectContaining({
        subtotal: 145,
      }));
    });

    expect(await screen.findByText('咖啡滿百 9 折')).toBeInTheDocument();
    expect(useCartStore.getState().discountSource).toBe('promotion');
    expect(useCartStore.getState().discountAmount).toBe(14.5);
  });

  it('keeps selected member when promotion code replaces member discount', async () => {
    const user = userEvent.setup();
    useCartStore.setState({
      discountAmount: 14.5,
      discountSource: 'member',
      selectedMember: {
        id: 'member-gold',
        memberNo: 'M0001',
        name: '金卡會員',
        phoneMasked: '0912***888',
        tier: 'GOLD',
        points: 1000,
        discountPercent: 10,
      },
      appliedPromotion: null,
    });

    renderCart();

    await user.click(screen.getByRole('button', { name: /會員折扣/ }));
    await user.type(screen.getByLabelText('優惠碼'), 'CAFE20');
    await user.click(screen.getByRole('button', { name: '套優惠碼' }));

    await waitFor(() => expect(useCartStore.getState().discountSource).toBe('promotion'));
    expect(useCartStore.getState().selectedMember?.id).toBe('member-gold');
    expect(useCartStore.getState().appliedPromotion).toEqual(expect.objectContaining({
      ruleId: 'promo-auto-001',
      code: null,
      name: '咖啡滿百 9 折',
    }));
    await waitFor(() => {
      expect(screen.queryByRole('dialog', { name: '套用折扣' })).not.toBeInTheDocument();
    });
    expect(screen.getByRole('button', { name: /GOLD 金卡會員/ })).toBeInTheDocument();
  });
});
