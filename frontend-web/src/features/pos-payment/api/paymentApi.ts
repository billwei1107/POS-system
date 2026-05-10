/**
 * @file paymentApi.ts
 * @description POS 支付 API 層 / POS payment API layer
 * @description_en Axios-based API client for payment methods, transactions, cash drawers, and reconciliation
 * @description_zh 支付方式、交易、現金抽屜與對帳的 Axios API 客戶端
 */
import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse } from '../../../shared/types';
import type {
  PayMethod,
  CreatePayMethodRequest,
  PaymentTransaction,
  ProcessPaymentRequest,
  CashDrawer,
  OpenDrawerRequest,
  GatewayConfig,
  Reconciliation,
} from '../types';

const BASE = '/v1/pos/payments';
const DRAWER_BASE = '/v1/pos/cash-drawers';
const RECON_BASE = '/v1/pos/reconciliation';

// ========================================
// 支付方式 API / Pay method API
// ========================================
export const payMethodApi = {
  list: (storeId: string) =>
    axiosInstance.get<unknown, ApiResponse<PayMethod[]>>(`${BASE}/methods`, { params: { storeId } }),

  create: (req: CreatePayMethodRequest) =>
    axiosInstance.post<unknown, ApiResponse<PayMethod>, CreatePayMethodRequest>(`${BASE}/methods`, req),

  deactivate: (id: string) =>
    axiosInstance.delete<unknown, ApiResponse<null>>(`${BASE}/methods/${id}`),
};

// ========================================
// 支付交易 API / Payment transaction API
// ========================================
export const paymentApi = {
  process: (req: ProcessPaymentRequest) =>
    axiosInstance.post<unknown, ApiResponse<PaymentTransaction>, ProcessPaymentRequest>(BASE, req),

  getByOrder: (orderId: string) =>
    axiosInstance.get<unknown, ApiResponse<PaymentTransaction[]>>(`${BASE}/orders/${orderId}`),
};

// ========================================
// 現金抽屜 API / Cash drawer API
// ========================================
export const cashDrawerApi = {
  open: (req: OpenDrawerRequest) =>
    axiosInstance.post<unknown, ApiResponse<CashDrawer>, OpenDrawerRequest>(`${DRAWER_BASE}/open`, req),

  close: (id: string, closedBy: string, closingAmount?: number, note?: string) =>
    axiosInstance.post<unknown, ApiResponse<CashDrawer>>(`${DRAWER_BASE}/${id}/close`, null, {
      params: { closedBy, closingAmount, note },
    }),

  getOpen: (terminalId: string) =>
    axiosInstance.get<unknown, ApiResponse<CashDrawer>>(`${DRAWER_BASE}/terminal/${terminalId}/open`),
};

// ========================================
// 閘道配置 API / Gateway config API (placeholder)
// ========================================
export const gatewayApi = {
  list: (storeId: string) =>
    axiosInstance.get<unknown, ApiResponse<GatewayConfig[]>>(`${BASE}/gateways`, { params: { storeId } }),
};

// ========================================
// 對帳 API / Reconciliation API
// ========================================
export const reconciliationApi = {
  generate: (storeId: string, date: string) =>
    axiosInstance.post<unknown, ApiResponse<Reconciliation[]>>(`${RECON_BASE}/generate`, null, {
      params: { storeId, date },
    }),

  list: (storeId: string, date: string) =>
    axiosInstance.get<unknown, ApiResponse<Reconciliation[]>>(RECON_BASE, { params: { storeId, date } }),

  confirm: (id: string, gatewayAmount: number, reconciledBy: string) =>
    axiosInstance.post<unknown, ApiResponse<Reconciliation>>(`${RECON_BASE}/${id}/confirm`, null, {
      params: { gatewayAmount, reconciledBy },
    }),
};
