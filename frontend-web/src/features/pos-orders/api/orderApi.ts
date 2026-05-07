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
} from '../types';

const BASE = '/api/v1/pos/orders';
const REFUND_BASE = '/api/v1/pos/refunds';

// ========================================
// 訂單 API / Order APIs
// ========================================
export const orderApi = {
  create: (req: CreateOrderRequest) =>
    axiosInstance.post<ApiResponse<Order>>(BASE, req).then(r => r.data),

  getById: (id: string) =>
    axiosInstance.get<ApiResponse<Order>>(`${BASE}/${id}`).then(r => r.data),

  list: (params: OrderListParams) =>
    axiosInstance.get<ApiResponse<PaginatedData<Order>>>(BASE, { params }).then(r => r.data),

  complete: (id: string, payMethod: string, tendered?: number) =>
    axiosInstance.post<ApiResponse<Order>>(`${BASE}/${id}/complete`, null, {
      params: { payMethod, tendered },
    }).then(r => r.data),

  void: (id: string, voidedBy: string, reason?: string) =>
    axiosInstance.post<ApiResponse<Order>>(`${BASE}/${id}/void`, null, {
      params: { voidedBy, reason },
    }).then(r => r.data),
};

// ========================================
// 退款 API / Refund APIs
// ========================================
export const refundApi = {
  create: (req: CreateRefundRequest) =>
    axiosInstance.post<ApiResponse<OrderRefund>>(REFUND_BASE, req).then(r => r.data),

  complete: (id: string) =>
    axiosInstance.post<ApiResponse<OrderRefund>>(`${REFUND_BASE}/${id}/complete`).then(r => r.data),
};
