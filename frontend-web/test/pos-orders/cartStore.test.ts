/**
 * @file cartStore.test.ts
 * @description POS 購物車狀態測試 / POS cart store tests
 * @description_en Verifies cart totals, discounts and held order fallback state
 * @description_zh 驗證購物車金額、折扣與掛單備援狀態
 */
import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  calculateCartTotals,
  useCartStore,
  type CartLine,
  type CartMember,
} from '../../src/features/pos-orders/store/cartStore';
import type { ProductItem } from '../../src/features/pos-products/types';

const createProduct = (overrides: Partial<ProductItem> = {}): ProductItem => ({
  id: 'item-latte',
  sku: 'DEMO-LATTE',
  name: '拿鐵 12oz',
  description: null,
  categoryId: null,
  basePrice: 120,
  costPrice: null,
  taxClassId: null,
  unit: 'PCS',
  barcodePrimary: null,
  imageUrl: null,
  trackInventory: true,
  sellable: true,
  weightBased: false,
  active: true,
  createdAt: '2026-05-11T00:00:00Z',
  updatedAt: '2026-05-11T00:00:00Z',
  ...overrides,
});

const member: CartMember = {
  id: 'member-gold',
  memberNo: 'M0001',
  name: '金卡會員',
  phoneMasked: '0912***888',
  tier: 'GOLD',
  points: 1000,
  discountPercent: 10,
};

beforeEach(() => {
  window.localStorage.clear();
  useCartStore.setState({
    lines: [],
    taxRate: 0.05,
    discountAmount: 0,
    discountSource: null,
    selectedMember: null,
    appliedPromotion: null,
    heldOrders: [],
  });
  vi.useRealTimers();
});

describe('calculateCartTotals', () => {
  it('calculates subtotal, bounded discount, tax and rounded total', () => {
    const lines: CartLine[] = [
      { itemId: 'a', sku: 'A', name: 'A', unitPrice: 120, quantity: 1 },
      { itemId: 'b', sku: 'B', name: 'B', unitPrice: 80, quantity: 2 },
    ];

    expect(calculateCartTotals(lines, 0.05, 20)).toEqual({
      subtotal: 280,
      discount: 20,
      tax: 13,
      total: 273,
    });

    expect(calculateCartTotals(lines, 0.05, 999).discount).toBe(280);
  });
});

describe('useCartStore', () => {
  it('merges duplicate products and recalculates member discount', () => {
    const product = createProduct();

    useCartStore.getState().addProduct(product);
    useCartStore.getState().setMember(member);
    useCartStore.getState().addProduct(product);

    const state = useCartStore.getState();
    expect(state.lines).toHaveLength(1);
    expect(state.lines[0].quantity).toBe(2);
    expect(state.discountAmount).toBe(24);
    expect(state.discountSource).toBe('member');
    expect(state.totals()).toEqual({
      subtotal: 240,
      discount: 24,
      tax: 10.8,
      total: 227,
    });
  });

  it('applies promotion discount as a distinct discount source', () => {
    useCartStore.getState().addProduct(createProduct());
    useCartStore.getState().setPromotionDiscount({
      ruleId: 'promo-100',
      name: '咖啡滿百 9 折',
      code: null,
      discountAmount: 12,
    });

    const state = useCartStore.getState();
    expect(state.discountSource).toBe('promotion');
    expect(state.appliedPromotion?.name).toBe('咖啡滿百 9 折');
    expect(state.discountAmount).toBe(12);
    expect(state.selectedMember).toBeNull();
  });

  it('holds current order in localStorage and restores it into the cart', () => {
    vi.setSystemTime(new Date('2026-05-11T03:20:00.000Z'));
    useCartStore.getState().addProduct(createProduct());

    const heldOrder = useCartStore.getState().holdCurrentOrder();

    expect(heldOrder).not.toBeNull();
    expect(useCartStore.getState().lines).toHaveLength(0);
    expect(window.localStorage.getItem('pos-held-orders')).toContain('拿鐵 12oz');

    useCartStore.getState().restoreHeldOrder(heldOrder!.id);

    const state = useCartStore.getState();
    expect(state.lines).toHaveLength(1);
    expect(state.lines[0].itemId).toBe('item-latte');
    expect(state.heldOrders).toHaveLength(0);
    expect(window.localStorage.getItem('pos-held-orders')).toBe('[]');
  });
});
