/**
 * @file index.ts
 * @description POS 訂單型別定義 / POS order TypeScript type definitions
 * @description_en Type definitions for orders, refunds and related API payloads
 * @description_zh 訂單、退款及相關 API 酬載的 TypeScript 型別定義
 */

// ========================================
// 訂單狀態與類型 / Order status and type
// ========================================
export type OrderStatus = 'DRAFT' | 'CONFIRMED' | 'PREPARING' | 'READY' | 'COMPLETED' | 'CLOSED' | 'VOIDED';
export type OrderType = 'DINE_IN' | 'TAKEOUT' | 'DELIVERY' | 'ONLINE';
export type OrderDiscountSource = 'MANUAL' | 'MEMBER' | 'PROMOTION';

// ========================================
// 訂單明細型別 / Order item type
// ========================================
export interface OrderItem {
  id: string;
  itemId: string;
  variantId?: string;
  itemNameSnapshot: string;
  skuSnapshot?: string;
  unitPrice: number;
  quantity: number;
  discountAmount: number;
  lineTotal: number;
  note?: string;
}

// ========================================
// 訂單型別 / Order type
// ========================================
export interface Order {
  id: string;
  orderNo: string;
  storeId: string;
  terminalId?: string;
  employeeId?: string;
  status: OrderStatus;
  orderType: OrderType;
  subtotal: number;
  discountTotal: number;
  discountSource?: OrderDiscountSource | null;
  promotionRuleId?: string | null;
  promotionCode?: string | null;
  discountLabel?: string | null;
  taxTotal: number;
  roundingAdj: number;
  grandTotal: number;
  paidTotal: number;
  changeGiven: number;
  memberId?: string;
  note?: string;
  tableNo?: string;
  guestCount?: number;
  completedAt?: string;
  createdAt: string;
  items: OrderItem[];
}

// ========================================
// 退款型別 / Refund type
// ========================================
export type RefundStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED';

export interface OrderRefund {
  id: string;
  orderId: string;
  refundNo: string;
  refundAmount: number;
  refundMethod: string;
  reason?: string;
  approvedBy?: string;
  status: RefundStatus;
  processedAt?: string;
  createdAt: string;
}

// ========================================
// 查詢參數 / Query params
// ========================================
export interface OrderListParams {
  storeId: string;
  status?: OrderStatus;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

// ========================================
// 請求型別 / Request types
// ========================================
export interface OrderItemRequest {
  itemId: string;
  variantId?: string;
  itemNameSnapshot: string;
  skuSnapshot?: string;
  unitPrice: number;
  quantity: number;
  modifierPriceAdjustment?: number;
  note?: string;
}

export interface CreateOrderRequest {
  storeId: string;
  terminalId?: string;
  employeeId?: string;
  orderType?: OrderType;
  items: OrderItemRequest[];
  discountAmount?: number;
  discountSource?: OrderDiscountSource;
  promotionRuleId?: string;
  promotionCode?: string;
  discountLabel?: string;
  memberId?: string;
  tableNo?: string;
  guestCount?: number;
  note?: string;
  taxIncluded: boolean;
}

export interface CreateRefundRequest {
  orderId: string;
  refundAmount: number;
  refundMethod: string;
  reason?: string;
  approvedBy?: string;
}

export interface HeldOrderResponse {
  id: string;
  storeId: string;
  terminalId?: string;
  label?: string;
  payload: string;
  heldAt: string;
  createdAt?: string;
}

export interface CreateHeldOrderRequest {
  storeId: string;
  terminalId?: string;
  label?: string;
  payload: string;
}
