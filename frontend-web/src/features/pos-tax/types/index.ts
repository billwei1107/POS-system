/**
 * @file index.ts
 * @description POS 稅務模組型別定義 / POS tax module TypeScript type definitions
 * @description_en Type definitions for tax classes, invoice tracks, invoices, and allowances
 * @description_zh 稅率類別、字軌、電子發票、折讓單的 TypeScript 型別定義
 */

// ========================================
// 稅率類別 / Tax class
// ========================================
export type TaxType = 'INCLUSIVE' | 'EXCLUSIVE' | 'EXEMPT' | 'ZERO_RATED';

export interface TaxClass {
  id: string;
  storeId: string;
  name: string;
  taxType: TaxType;
  rate: number;
  description: string | null;
  isDefault: boolean;
  isActive: boolean;
}

export interface CreateTaxClassRequest {
  storeId: string;
  name: string;
  taxType: TaxType;
  rate: number;
  description?: string;
  isDefault: boolean;
}

// ========================================
// 發票字軌 / Invoice track
// ========================================
export interface InvoiceTrack {
  id: string;
  storeId: string;
  sellerId: string;
  trackPrefix: string;
  yearMonth: string;
  period: string;
  startNo: string;
  endNo: string;
  currentNo: string;
  isActive: boolean;
}

export interface AddInvoiceTrackRequest {
  storeId: string;
  sellerId: string;
  trackPrefix: string;
  yearMonth: string;
  period: string;
  startNo: string;
  endNo: string;
}

// ========================================
// 電子發票 / Invoice
// ========================================
export type InvoiceType = 'B2C' | 'B2B';
export type CarrierType = 'MEMBER' | 'MOBILE' | 'CITIZEN_DIGITAL';
export type InvoiceStatus = 'ISSUED' | 'VOIDED' | 'ALLOWANCE';
export type UploadStatus = 'PENDING' | 'SUCCESS' | 'FAILED';

export interface Invoice {
  id: string;
  storeId: string;
  orderId: string;
  fullInvoiceNo: string | null;
  invoiceType: InvoiceType;
  sellerId: string;
  sellerName: string;
  buyerId: string | null;
  buyerName: string | null;
  buyerEmail: string | null;
  carrierType: CarrierType | null;
  salesAmount: number;
  taxAmount: number;
  totalAmount: number;
  status: InvoiceStatus;
  uploadStatus: UploadStatus;
  issueAt: string;
  voidAt: string | null;
  voidReason: string | null;
}

export interface IssueInvoiceRequest {
  storeId: string;
  orderId: string;
  invoiceType?: InvoiceType;
  buyerId?: string;
  buyerName?: string;
  buyerEmail?: string;
  carrierType?: CarrierType;
  carrierId1?: string;
  carrierId2?: string;
  donateCode?: string;
}
