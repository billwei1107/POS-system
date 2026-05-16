/**
 * @file cartStore.ts
 * @description POS 本地購物車狀態 / POS local cart state
 * @description_en Stores the active register cart before it is submitted as an order
 * @description_zh 儲存收銀台目前訂單，在送出成正式訂單前提供前端互動狀態
 */
import { create } from 'zustand';
import type { ProductItem } from '../../pos-products/types';

export interface CartLine {
  itemId: string;
  sku: string;
  name: string;
  unitPrice: number;
  quantity: number;
  imageUrl?: string | null;
  note?: string;
}

export interface CartTotals {
  subtotal: number;
  discount: number;
  tax: number;
  total: number;
}

export interface CartMember {
  id: string;
  memberNo: string;
  name: string;
  phoneMasked: string;
  tier: string;
  points: number;
  discountPercent: number;
}

export type CartDiscountSource = 'manual' | 'member' | 'promotion';

export interface CartPromotion {
  ruleId: string;
  name: string;
  code: string | null;
  discountAmount: number;
}

export interface HeldOrder {
  id: string;
  displayNo: string;
  lines: CartLine[];
  taxRate: number;
  discountAmount: number;
  discountSource: CartDiscountSource | null;
  selectedMember: CartMember | null;
  appliedPromotion: CartPromotion | null;
  itemCount: number;
  total: number;
  createdAt: string;
}

interface CartState {
  lines: CartLine[];
  taxRate: number;
  discountAmount: number;
  discountSource: CartDiscountSource | null;
  selectedMember: CartMember | null;
  appliedPromotion: CartPromotion | null;
  heldOrders: HeldOrder[];
  addProduct: (product: ProductItem) => void;
  increase: (itemId: string) => void;
  decrease: (itemId: string) => void;
  remove: (itemId: string) => void;
  setDiscountAmount: (amount: number) => void;
  clearDiscount: () => void;
  setMember: (member: CartMember) => void;
  clearMember: () => void;
  setPromotionDiscount: (promotion: CartPromotion) => void;
  clearPromotionDiscount: () => void;
  holdCurrentOrder: () => HeldOrder | null;
  replaceHeldOrders: (heldOrders: HeldOrder[]) => void;
  replaceHeldOrder: (temporaryId: string, heldOrder: HeldOrder) => void;
  restoreHeldOrder: (heldOrderId: string) => void;
  removeHeldOrder: (heldOrderId: string) => void;
  clear: () => void;
  totals: () => CartTotals;
  itemCount: () => number;
}

const HELD_ORDERS_STORAGE_KEY = 'pos-held-orders';
const roundMoney = (amount: number) => Math.round(amount * 100) / 100;

export const calculateCartTotals = (lines: CartLine[], taxRate: number, discountAmount = 0): CartTotals => {
  const subtotal = roundMoney(lines.reduce((sum, line) => sum + line.unitPrice * line.quantity, 0));
  const discount = Math.min(subtotal, Math.max(0, roundMoney(discountAmount)));
  const tax = roundMoney((subtotal - discount) * taxRate);
  return {
    subtotal,
    discount,
    tax,
    total: roundMoney(Math.round(subtotal - discount + tax)),
  };
};

export const calculateItemCount = (lines: CartLine[]) =>
  lines.reduce((sum, line) => sum + line.quantity, 0);

const calculateMemberDiscount = (lines: CartLine[], member: CartMember | null) => {
  if (!member || member.discountPercent <= 0) return 0;
  const subtotal = calculateCartTotals(lines, 0).subtotal;
  return roundMoney(subtotal * member.discountPercent / 100);
};

const capDiscount = (lines: CartLine[], taxRate: number, amount: number) => {
  const subtotal = calculateCartTotals(lines, taxRate).subtotal;
  return Math.min(subtotal, Math.max(0, roundMoney(amount)));
};

const recalculateDiscountForLines = (
  lines: CartLine[],
  taxRate: number,
  currentDiscount: number,
  selectedMember: CartMember | null,
  discountSource: CartDiscountSource | null,
  appliedPromotion: CartPromotion | null
) => {
  if (discountSource === 'member') return calculateMemberDiscount(lines, selectedMember);
  if (discountSource === 'promotion' && appliedPromotion) return capDiscount(lines, taxRate, appliedPromotion.discountAmount);
  return capDiscount(lines, taxRate, currentDiscount);
};

const loadHeldOrders = (): HeldOrder[] => {
  if (typeof window === 'undefined') return [];
  try {
    const raw = window.localStorage.getItem(HELD_ORDERS_STORAGE_KEY);
    return raw ? JSON.parse(raw) as HeldOrder[] : [];
  } catch {
    return [];
  }
};

const persistHeldOrders = (heldOrders: HeldOrder[]) => {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(HELD_ORDERS_STORAGE_KEY, JSON.stringify(heldOrders));
};

export const useCartStore = create<CartState>((set, get) => ({
  lines: [],
  taxRate: 0.05,
  discountAmount: 0,
  discountSource: null,
  selectedMember: null,
  appliedPromotion: null,
  heldOrders: loadHeldOrders(),

  // ========================================
  // 商品加入購物車 / Add Product To Cart
  // ========================================
  addProduct: (product) => set((state) => {
    const existing = state.lines.find((line) => line.itemId === product.id);
    if (existing) {
      const lines = state.lines.map((line) =>
        line.itemId === product.id ? { ...line, quantity: line.quantity + 1 } : line
      );
      return {
        lines,
        discountAmount: recalculateDiscountForLines(
          lines,
          state.taxRate,
          state.discountAmount,
          state.selectedMember,
          state.discountSource,
          state.appliedPromotion
        ),
      };
    }

    const lines = [
      ...state.lines,
      {
        itemId: product.id,
        sku: product.sku,
        name: product.name,
        unitPrice: Number(product.basePrice),
        quantity: 1,
        imageUrl: product.imageUrl,
      },
    ];

    return {
      lines,
      discountAmount: recalculateDiscountForLines(
        lines,
        state.taxRate,
        state.discountAmount,
        state.selectedMember,
        state.discountSource,
        state.appliedPromotion
      ),
    };
  }),

  increase: (itemId) => set((state) => {
    const lines = state.lines.map((line) =>
      line.itemId === itemId ? { ...line, quantity: line.quantity + 1 } : line
    );
    return {
      lines,
      discountAmount: recalculateDiscountForLines(
        lines,
        state.taxRate,
        state.discountAmount,
        state.selectedMember,
        state.discountSource,
        state.appliedPromotion
      ),
    };
  }),

  decrease: (itemId) => set((state) => {
    const lines = state.lines
      .map((line) => line.itemId === itemId ? { ...line, quantity: line.quantity - 1 } : line)
      .filter((line) => line.quantity > 0);
    return {
      lines,
      discountAmount: recalculateDiscountForLines(
        lines,
        state.taxRate,
        state.discountAmount,
        state.selectedMember,
        state.discountSource,
        state.appliedPromotion
      ),
    };
  }),

  remove: (itemId) => set((state) => {
    const lines = state.lines.filter((line) => line.itemId !== itemId);
    return {
      lines,
      discountAmount: recalculateDiscountForLines(
        lines,
        state.taxRate,
        state.discountAmount,
        state.selectedMember,
        state.discountSource,
        state.appliedPromotion
      ),
    };
  }),

  setDiscountAmount: (amount) => set((state) => {
    return {
      discountAmount: capDiscount(state.lines, state.taxRate, amount),
      discountSource: amount > 0 ? 'manual' : null,
      appliedPromotion: null,
    };
  }),

  clearDiscount: () => set({
    discountAmount: 0,
    discountSource: null,
    appliedPromotion: null,
  }),

  setMember: (member) => set((state) => ({
    selectedMember: member,
    discountAmount: calculateMemberDiscount(state.lines, member),
    discountSource: member.discountPercent > 0 ? 'member' : null,
    appliedPromotion: null,
  })),

  clearMember: () => set((state) => ({
    selectedMember: null,
    discountAmount: state.discountSource === 'member' ? 0 : state.discountAmount,
    discountSource: state.discountSource === 'member' ? null : state.discountSource,
  })),

  setPromotionDiscount: (promotion) => set((state) => ({
    appliedPromotion: {
      ...promotion,
      discountAmount: capDiscount(state.lines, state.taxRate, promotion.discountAmount),
    },
    discountAmount: capDiscount(state.lines, state.taxRate, promotion.discountAmount),
    discountSource: promotion.discountAmount > 0 ? 'promotion' : null,
  })),

  clearPromotionDiscount: () => set((state) => {
    if (state.discountSource !== 'promotion' && !state.appliedPromotion) return state;
    const memberDiscount = calculateMemberDiscount(state.lines, state.selectedMember);
    return {
      appliedPromotion: null,
      discountAmount: memberDiscount,
      discountSource: memberDiscount > 0 ? 'member' : null,
    };
  }),

  // ========================================
  // 掛單與取單 / Hold And Restore Orders
  // ========================================
  holdCurrentOrder: () => {
    const state = get();
    if (state.lines.length === 0) return null;

    const totals = calculateCartTotals(state.lines, state.taxRate, state.discountAmount);
    const heldOrder: HeldOrder = {
      id: `hold-${Date.now()}`,
      displayNo: `H${String(Date.now()).slice(-6)}`,
      lines: state.lines.map((line) => ({ ...line })),
      taxRate: state.taxRate,
      discountAmount: state.discountAmount,
      discountSource: state.discountSource,
      selectedMember: state.selectedMember,
      appliedPromotion: state.appliedPromotion,
      itemCount: calculateItemCount(state.lines),
      total: totals.total,
      createdAt: new Date().toISOString(),
    };
    const heldOrders = [heldOrder, ...state.heldOrders].slice(0, 20);
    persistHeldOrders(heldOrders);
    set({
      lines: [],
      discountAmount: 0,
      discountSource: null,
      selectedMember: null,
      appliedPromotion: null,
      heldOrders,
    });
    return heldOrder;
  },

  replaceHeldOrders: (heldOrders) => {
    persistHeldOrders(heldOrders);
    set({ heldOrders });
  },

  replaceHeldOrder: (temporaryId, heldOrder) => set((state) => {
    const heldOrders = state.heldOrders.map((order) => order.id === temporaryId ? heldOrder : order);
    persistHeldOrders(heldOrders);
    return { heldOrders };
  }),

  restoreHeldOrder: (heldOrderId) => set((state) => {
    const heldOrder = state.heldOrders.find((order) => order.id === heldOrderId);
    if (!heldOrder) return state;

    const heldOrders = state.heldOrders.filter((order) => order.id !== heldOrderId);
    persistHeldOrders(heldOrders);
    return {
      lines: heldOrder.lines.map((line) => ({ ...line })),
      taxRate: heldOrder.taxRate,
      discountAmount: heldOrder.discountAmount,
      discountSource: heldOrder.discountSource ?? null,
      selectedMember: heldOrder.selectedMember,
      appliedPromotion: heldOrder.appliedPromotion ?? null,
      heldOrders,
    };
  }),

  removeHeldOrder: (heldOrderId) => set((state) => {
    const heldOrders = state.heldOrders.filter((order) => order.id !== heldOrderId);
    persistHeldOrders(heldOrders);
    return { heldOrders };
  }),

  clear: () => set({
    lines: [],
    discountAmount: 0,
    discountSource: null,
    selectedMember: null,
    appliedPromotion: null,
  }),

  // ========================================
  // 金額彙總 / Cart Totals
  // ========================================
  totals: () => calculateCartTotals(get().lines, get().taxRate, get().discountAmount),

  itemCount: () => calculateItemCount(get().lines),
}));
