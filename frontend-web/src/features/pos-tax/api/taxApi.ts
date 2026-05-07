/**
 * @file taxApi.ts
 * @description POS 稅務模組 API / POS tax module API client
 * @description_en Axios-based API calls for tax classes, invoice tracks, and invoices
 * @description_zh 稅率類別、字軌管理、電子發票的 Axios API 呼叫
 */
import axios from 'axios';
import type {
  TaxClass, CreateTaxClassRequest,
  InvoiceTrack, AddInvoiceTrackRequest,
  Invoice, IssueInvoiceRequest,
} from '../types';

const BASE = '/api/v1/pos';

// ========================================
// 稅率類別 API / Tax class API
// ========================================
export const taxClassApi = {
  list: (storeId: string) =>
    axios.get<{ data: TaxClass[] }>(`${BASE}/tax-classes`, { params: { storeId } }),
  create: (req: CreateTaxClassRequest) =>
    axios.post<{ data: TaxClass }>(`${BASE}/tax-classes`, req),
  deactivate: (id: string) =>
    axios.delete(`${BASE}/tax-classes/${id}`),
};

// ========================================
// 發票字軌 API / Invoice track API
// ========================================
export const invoiceTrackApi = {
  list: (storeId: string) =>
    axios.get<{ data: InvoiceTrack[] }>(`${BASE}/invoice-tracks`, { params: { storeId } }),
  add: (req: AddInvoiceTrackRequest) =>
    axios.post<{ data: InvoiceTrack }>(`${BASE}/invoice-tracks`, req),
};

// ========================================
// 電子發票 API / Invoice API
// ========================================
export const invoiceApi = {
  issue: (req: IssueInvoiceRequest) =>
    axios.post<{ data: Invoice }>(`${BASE}/invoices`, req),
  getByOrder: (orderId: string) =>
    axios.get<{ data: Invoice }>(`${BASE}/invoices/orders/${orderId}`),
  list: (storeId: string, from: string, to: string) =>
    axios.get<{ data: Invoice[] }>(`${BASE}/invoices`, { params: { storeId, from, to } }),
  void: (id: string, reason?: string) =>
    axios.post<{ data: Invoice }>(`${BASE}/invoices/${id}/void`, null, {
      params: { reason: reason ?? '手動作廢' },
    }),
};
