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

const BASE = '/api/v1/pos/payments';
const DRAWER_BASE = '/api/v1/pos/cash-drawers';
const RECON_BASE = '/api/v1/pos/reconciliation';

// ========================================
// 支付方式 API / Pay method API
// ========================================
export const payMethodApi = {
  list: (storeId: string) =>
    axiosInstance.get<ApiResponse<PayMethod[]>>(`${BASE}/methods`, { params: { storeId } }),

  create: (req: CreatePayMethodRequest) =>
    axiosInstance.post<ApiResponse<PayMethod>>(`${BASE}/methods`, req),

  deactivate: (id: string) =>
    axiosInstance.delete<ApiResponse<null>>(`${BASE}/methods/${id}`),
};

// ========================================
// 支付交易 API / Payment transaction API
// ========================================
export const paymentApi = {
  process: (req: ProcessPaymentRequest) =>
    axiosInstance.post<ApiResponse<PaymentTransaction>>(BASE, req),

  getByOrder: (orderId: string) =>
    axiosInstance.get<ApiResponse<PaymentTransaction[]>>(`${BASE}/orders/${orderId}`),
};

// ========================================
// 現金抽屜 API / Cash drawer API
// ========================================
export const cashDrawerApi = {
  open: (req: OpenDrawerRequest) =>
    axiosInstance.post<ApiResponse<CashDrawer>>(`${DRAWER_BASE}/open`, req),

  close: (id: string, closedBy: string, closingAmount?: number, note?: string) =>
    axiosInstance.post<ApiResponse<CashDrawer>>(`${DRAWER_BASE}/${id}/close`, null, {
      params: { closedBy, closingAmount, note },
    }),

  getOpen: (terminalId: string) =>
    axiosInstance.get<ApiResponse<CashDrawer>>(`${DRAWER_BASE}/terminal/${terminalId}/open`),
};

// ========================================
// 閘道配置 API / Gateway config API (placeholder)
// ========================================
export const gatewayApi = {
  list: (storeId: string) =>
    axiosInstance.get<ApiResponse<GatewayConfig[]>>(`${BASE}/gateways`, { params: { storeId } }),
};

// ========================================
// 對帳 API / Reconciliation API
// ========================================
export const reconciliationApi = {
  generate: (storeId: string, date: string) =>
    axiosInstance.post<ApiResponse<Reconciliation[]>>(`${RECON_BASE}/generate`, null, {
      params: { storeId, date },
    }),

  list: (storeId: string, date: string) =>
    axiosInstance.get<ApiResponse<Reconciliation[]>>(RECON_BASE, { params: { storeId, date } }),

  confirm: (id: string, gatewayAmount: number, reconciledBy: string) =>
    axiosInstance.post<ApiResponse<Reconciliation>>(`${RECON_BASE}/${id}/confirm`, null, {
      params: { gatewayAmount, reconciledBy },
    }),
};
