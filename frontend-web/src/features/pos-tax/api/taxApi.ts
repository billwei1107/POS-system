/**
 * @file taxApi.ts
 * @description POS 稅務模組 API / POS tax module API client
 * @description_en Axios-based API calls for tax classes, invoice tracks, and invoices
 * @description_zh 稅率類別、字軌管理、電子發票的 Axios API 呼叫
 */
import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse } from '../../../shared/types';
import type {
  TaxClass, CreateTaxClassRequest,
  InvoiceTrack, AddInvoiceTrackRequest,
  Invoice, IssueInvoiceRequest,
} from '../types';

const BASE = '/v1/pos';

// ========================================
// 稅率類別 API / Tax class API
// ========================================
export const taxClassApi = {
  list: (storeId: string) =>
    axiosInstance.get<unknown, ApiResponse<TaxClass[]>>(`${BASE}/tax-classes`, { params: { storeId } }),
  create: (req: CreateTaxClassRequest) =>
    axiosInstance.post<unknown, ApiResponse<TaxClass>, CreateTaxClassRequest>(`${BASE}/tax-classes`, req),
  deactivate: (id: string) =>
    axiosInstance.delete<unknown, ApiResponse<null>>(`${BASE}/tax-classes/${id}`),
};

// ========================================
// 發票字軌 API / Invoice track API
// ========================================
export const invoiceTrackApi = {
  list: (storeId: string) =>
    axiosInstance.get<unknown, ApiResponse<InvoiceTrack[]>>(`${BASE}/invoice-tracks`, { params: { storeId } }),
  add: (req: AddInvoiceTrackRequest) =>
    axiosInstance.post<unknown, ApiResponse<InvoiceTrack>, AddInvoiceTrackRequest>(`${BASE}/invoice-tracks`, req),
};

// ========================================
// 電子發票 API / Invoice API
// ========================================
export const invoiceApi = {
  issue: (req: IssueInvoiceRequest) =>
    axiosInstance.post<unknown, ApiResponse<Invoice>, IssueInvoiceRequest>(`${BASE}/invoices`, req),
  getByOrder: (orderId: string) =>
    axiosInstance.get<unknown, ApiResponse<Invoice>>(`${BASE}/invoices/orders/${orderId}`),
  list: (storeId: string, from: string, to: string) =>
    axiosInstance.get<unknown, ApiResponse<Invoice[]>>(`${BASE}/invoices`, { params: { storeId, from, to } }),
  void: (id: string, reason?: string) =>
    axiosInstance.post<unknown, ApiResponse<Invoice>>(`${BASE}/invoices/${id}/void`, null, {
      params: { reason: reason ?? '手動作廢' },
    }),
};
