/**
 * @file orderApi.ts
 * @description POS 訂單 API 層 / POS order API layer
 * @description_en Axios-based API client for order and refund management
 * @description_zh 基於 Axios 的訂單與退款管理 API 客戶端
 */
import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse, PaginatedData } from '../../../shared/types';
import type {
  Order,
  OrderListParams,
  CreateOrderRequest,
  OrderRefund,
  CreateRefundRequest,
  HeldOrderResponse,
  CreateHeldOrderRequest,
} from '../types';

const BASE = '/v1/pos/orders';
const REFUND_BASE = '/v1/pos/refunds';
const HELD_ORDER_BASE = '/v1/pos/held-orders';

// ========================================
// 訂單 API / Order APIs
// ========================================
export const orderApi = {
  create: (req: CreateOrderRequest) =>
    axiosInstance.post<unknown, ApiResponse<Order>, CreateOrderRequest>(BASE, req),

  getById: (id: string) =>
    axiosInstance.get<unknown, ApiResponse<Order>>(`${BASE}/${id}`),

  list: (params: OrderListParams) =>
    axiosInstance.get<unknown, ApiResponse<PaginatedData<Order>>>(BASE, { params }),

  complete: (id: string, payMethod: string, tendered?: number) =>
    axiosInstance.post<unknown, ApiResponse<Order>>(`${BASE}/${id}/complete`, null, {
      params: { payMethod, tendered },
    }),

  void: (id: string, voidedBy: string, reason?: string) =>
    axiosInstance.post<unknown, ApiResponse<Order>>(`${BASE}/${id}/void`, null, {
      params: { voidedBy, reason },
    }),
};

// ========================================
// 退款 API / Refund APIs
// ========================================
export const refundApi = {
  create: (req: CreateRefundRequest) =>
    axiosInstance.post<unknown, ApiResponse<OrderRefund>, CreateRefundRequest>(REFUND_BASE, req),

  complete: (id: string) =>
    axiosInstance.post<unknown, ApiResponse<OrderRefund>>(`${REFUND_BASE}/${id}/complete`),
};

// ========================================
// 掛單 API / Held order APIs
// ========================================
export const heldOrderApi = {
  create: (req: CreateHeldOrderRequest) =>
    axiosInstance.post<unknown, ApiResponse<HeldOrderResponse>, CreateHeldOrderRequest>(HELD_ORDER_BASE, req),

  list: (storeId: string, terminalId?: string) =>
    axiosInstance.get<unknown, ApiResponse<HeldOrderResponse[]>>(HELD_ORDER_BASE, {
      params: { storeId, terminalId },
    }),

  remove: (id: string) =>
    axiosInstance.delete<unknown, ApiResponse<void>>(`${HELD_ORDER_BASE}/${id}`),
};
