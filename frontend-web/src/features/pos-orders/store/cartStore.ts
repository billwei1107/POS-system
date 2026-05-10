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

interface CartState {
  lines: CartLine[];
  taxRate: number;
  addProduct: (product: ProductItem) => void;
  increase: (itemId: string) => void;
  decrease: (itemId: string) => void;
  remove: (itemId: string) => void;
  clear: () => void;
  totals: () => CartTotals;
  itemCount: () => number;
}

const roundMoney = (amount: number) => Math.round(amount * 100) / 100;

export const calculateCartTotals = (lines: CartLine[], taxRate: number): CartTotals => {
  const subtotal = roundMoney(lines.reduce((sum, line) => sum + line.unitPrice * line.quantity, 0));
  const discount = 0;
  const tax = roundMoney((subtotal - discount) * taxRate);
  return {
    subtotal,
    discount,
    tax,
    total: roundMoney(subtotal - discount + tax),
  };
};

export const calculateItemCount = (lines: CartLine[]) =>
  lines.reduce((sum, line) => sum + line.quantity, 0);

export const useCartStore = create<CartState>((set, get) => ({
  lines: [],
  taxRate: 0.05,

  // ========================================
  // 商品加入購物車 / Add Product To Cart
  // ========================================
  addProduct: (product) => set((state) => {
    const existing = state.lines.find((line) => line.itemId === product.id);
    if (existing) {
      return {
        lines: state.lines.map((line) =>
          line.itemId === product.id ? { ...line, quantity: line.quantity + 1 } : line
        ),
      };
    }

    return {
      lines: [
        ...state.lines,
        {
          itemId: product.id,
          sku: product.sku,
          name: product.name,
          unitPrice: Number(product.basePrice),
          quantity: 1,
          imageUrl: product.imageUrl,
        },
      ],
    };
  }),

  increase: (itemId) => set((state) => ({
    lines: state.lines.map((line) =>
      line.itemId === itemId ? { ...line, quantity: line.quantity + 1 } : line
    ),
  })),

  decrease: (itemId) => set((state) => ({
    lines: state.lines
      .map((line) => line.itemId === itemId ? { ...line, quantity: line.quantity - 1 } : line)
      .filter((line) => line.quantity > 0),
  })),

  remove: (itemId) => set((state) => ({
    lines: state.lines.filter((line) => line.itemId !== itemId),
  })),

  clear: () => set({ lines: [] }),

  // ========================================
  // 金額彙總 / Cart Totals
  // ========================================
  totals: () => calculateCartTotals(get().lines, get().taxRate),

  itemCount: () => calculateItemCount(get().lines),
}));
