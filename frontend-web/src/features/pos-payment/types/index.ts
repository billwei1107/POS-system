/**
 * @file index.ts
 * @description POS 支付模組型別定義 / POS payment module type definitions
 * @description_en TypeScript interfaces for payment methods, transactions, cash drawers, and reconciliation
 * @description_zh 支付方式、交易記錄、現金抽屜與對帳的 TypeScript 型別定義
 */

// ========================================
// 支付方式 / Pay Method
// ========================================
export type MethodType = 'CASH' | 'CARD' | 'QR_CODE' | 'GIFT_CARD' | 'MIXED';

export interface PayMethod {
  id: string;
  storeId: string;
  code: string;
  name: string;
  methodType: MethodType;
  gatewayId: string | null;
  isChangeBack: boolean;
  sortOrder: number;
  isActive: boolean;
  createdAt: string;
}

export interface CreatePayMethodRequest {
  storeId: string;
  code: string;
  name: string;
  methodType: MethodType;
  gatewayId?: string;
  isChangeBack: boolean;
  sortOrder: number;
}

// ========================================
// 支付交易 / Payment Transaction
// ========================================
export type TxnStatus = 'SUCCESS' | 'FAILED' | 'VOIDED' | 'REFUNDED';

export interface PaymentTransaction {
  id: string;
  orderId: string;
  storeId: string;
  payMethodId: string;
  methodType: string;
  amount: number;
  tendered: number | null;
  changeGiven: number;
  status: TxnStatus;
  gatewayRef: string | null;
  processedAt: string;
}

export interface ProcessPaymentRequest {
  orderId: string;
  orderNo: string;
  storeId: string;
  payMethodId: string;
  amount: number;
  tendered?: number;
  currency?: string;
  note?: string;
}

// ========================================
// 現金抽屜 / Cash Drawer
// ========================================
export type DrawerStatus = 'OPEN' | 'CLOSED';

export interface CashDrawer {
  id: string;
  storeId: string;
  terminalId: string;
  openingAmount: number;
  closingAmount: number | null;
  expectedAmount: number | null;
  variance: number | null;
  openedBy: string;
  closedBy: string | null;
  openedAt: string;
  closedAt: string | null;
  status: DrawerStatus;
  note: string | null;
}

export interface OpenDrawerRequest {
  storeId: string;
  terminalId: string;
  openedBy: string;
  openingAmount?: number;
}

// ========================================
// 閘道配置 / Gateway Config
// ========================================
export type GatewayType = 'CASH' | 'MOCK_CARD' | 'LINE_PAY' | 'JKOPAY' | 'TAIWAN_PAY';

export interface GatewayConfig {
  id: string;
  storeId: string;
  gatewayType: GatewayType;
  displayName: string;
  merchantId: string | null;
  isActive: boolean;
  isSandbox: boolean;
  createdAt: string;
}

// ========================================
// 對帳記錄 / Reconciliation
// ========================================
export type ReconStatus = 'PENDING' | 'MATCHED' | 'DISCREPANCY';

export interface Reconciliation {
  id: string;
  storeId: string;
  reconDate: string;
  payMethodId: string;
  methodType: string;
  transactionCount: number;
  totalAmount: number;
  refundCount: number;
  refundAmount: number;
  netAmount: number;
  gatewayAmount: number | null;
  variance: number | null;
  status: ReconStatus;
  reconciledBy: string | null;
  reconciledAt: string | null;
}
