/**
 * @file index.ts
 * @description POS 庫存管理型別定義 / POS inventory management TypeScript types
 * @description_en TypeScript type definitions for inventory entities and request/response types
 * @description_zh 庫存管理相關實體與請求/回應的 TypeScript 型別定義
 */

// ========================================
// 庫存異動類型 / Stock movement type enum
// ========================================
export type MovementType =
  | 'SALE'
  | 'RETURN'
  | 'TRANSFER_IN'
  | 'TRANSFER_OUT'
  | 'ADJUSTMENT'
  | 'RECEIVING'
  | 'WASTE';

// ========================================
// 調撥狀態 / Transfer status enum
// ========================================
export type TransferStatus =
  | 'REQUESTED'
  | 'APPROVED'
  | 'IN_TRANSIT'
  | 'RECEIVED'
  | 'CANCELLED';

// ========================================
// 盤點狀態 / Stock take status enum
// ========================================
export type StockTakeStatus = 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

// ========================================
// 警示類型 / Alert type enum
// ========================================
export type AlertType = 'LOW_STOCK' | 'OUT_OF_STOCK' | 'EXPIRING';

// ========================================
// 門店庫存 / Store stock
// ========================================
export interface StoreStock {
  id: string;
  storeId: string;
  itemId: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  reorderPoint: number;
  reorderQuantity: number;
}

// ========================================
// 庫存異動紀錄 / Stock movement
// ========================================
export interface StockMovement {
  id: string;
  storeId: string;
  itemId: string;
  quantityChange: number;
  movementType: MovementType;
  referenceId?: string;
  referenceType?: string;
  operatedBy?: string;
  notes?: string;
  createdAt: string;
}

// ========================================
// 調撥申請 / Transfer request
// ========================================
export interface TransferRequest {
  id: string;
  transferNo: string;
  fromStoreId: string;
  toStoreId: string;
  status: TransferStatus;
  notes?: string;
  requestedBy?: string;
  approvedBy?: string;
  shippedAt?: string;
  receivedAt?: string;
  createdAt: string;
  items: TransferItem[];
}

export interface TransferItem {
  id: string;
  itemId: string;
  requestedQty: number;
  shippedQty: number;
  receivedQty: number;
}

// ========================================
// 庫存警示 / Stock alert
// ========================================
export interface StockAlert {
  id: string;
  storeId: string;
  itemId: string;
  alertType: AlertType;
  currentQty: number;
  thresholdQty: number;
  acknowledged: boolean;
  acknowledgedBy?: string;
  acknowledgedAt?: string;
  createdAt: string;
}

// ========================================
// 盤點 / Stock take
// ========================================
export interface StockTake {
  id: string;
  storeId: string;
  status: StockTakeStatus;
  startedAt: string;
  completedAt?: string;
  createdBy?: string;
  notes?: string;
  items: StockTakeItem[];
}

export interface StockTakeItem {
  id: string;
  itemId: string;
  systemQty: number;
  countedQty?: number;
  difference?: number;
  notes?: string;
}

// ========================================
// 請求型別 / Request types
// ========================================
export interface CreateTransferRequestPayload {
  fromStoreId: string;
  toStoreId: string;
  notes?: string;
  requestedBy?: string;
  items: { itemId: string; requestedQty: number }[];
}

export interface AdjustStockPayload {
  storeId: string;
  itemId: string;
  adjustQty: number;
  operatedBy?: string;
  notes?: string;
}

export interface ReceiveStockPayload {
  storeId: string;
  operatedBy?: string;
  notes?: string;
  items: { itemId: string; receivedQty: number }[];
}
