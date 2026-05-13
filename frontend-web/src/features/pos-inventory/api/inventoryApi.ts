/**
 * @file inventoryApi.ts
 * @description POS 庫存管理 API / POS inventory management API
 * @description_en Axios API functions for inventory stock, transfers, alerts, and stock takes
 * @description_zh 庫存管理的 Axios API 函式，涵蓋庫存查詢、調撥、警示與盤點
 */
import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse } from '../../../shared/types';
import type {
  AdjustStockPayload,
  CreateTransferRequestPayload,
  ReceiveStockPayload,
  StockAlert,
  StockMovement,
  StockTake,
  StoreStock,
  TransferRequest,
} from '../types';

const BASE = '/v1/inventory';

// ========================================
// 庫存查詢與調整 API / Stock query and adjustment API
// ========================================
export const stockApi = {
  listByStore: (storeId: string) =>
    axiosInstance.get<unknown, ApiResponse<StoreStock[]>>(`${BASE}/stores/${storeId}/stock`),

  getByItem: (storeId: string, itemId: string) =>
    axiosInstance.get<unknown, ApiResponse<StoreStock>>(`${BASE}/stores/${storeId}/stock/${itemId}`),

  adjust: (payload: AdjustStockPayload) =>
    axiosInstance.post<unknown, ApiResponse<string>>(`${BASE}/stock/adjust`, payload),

  receive: (payload: ReceiveStockPayload) =>
    axiosInstance.post<unknown, ApiResponse<string>>(`${BASE}/stock/receive`, payload),

  listMovements: (storeId: string, itemId: string) =>
    axiosInstance.get<unknown, ApiResponse<StockMovement[]>>(
      `${BASE}/stores/${storeId}/items/${itemId}/movements`
    ),
};

// ========================================
// 調撥申請 API / Transfer request API
// ========================================
export const transferApi = {
  create: (payload: CreateTransferRequestPayload) =>
    axiosInstance.post<unknown, ApiResponse<TransferRequest>>(`${BASE}/transfers`, payload),

  listByStore: (storeId: string) =>
    axiosInstance.get<unknown, ApiResponse<TransferRequest[]>>(`${BASE}/transfers/stores/${storeId}`),

  getById: (transferId: string) =>
    axiosInstance.get<unknown, ApiResponse<TransferRequest>>(`${BASE}/transfers/${transferId}`),

  approve: (transferId: string, approvedBy?: string) =>
    axiosInstance.post<unknown, ApiResponse<TransferRequest>>(
      `${BASE}/transfers/${transferId}/approve`,
      null,
      { params: { approvedBy } }
    ),

  ship: (transferId: string) =>
    axiosInstance.post<unknown, ApiResponse<TransferRequest>>(`${BASE}/transfers/${transferId}/ship`),

  receive: (transferId: string) =>
    axiosInstance.post<unknown, ApiResponse<TransferRequest>>(`${BASE}/transfers/${transferId}/receive`),

  cancel: (transferId: string) =>
    axiosInstance.post<unknown, ApiResponse<TransferRequest>>(`${BASE}/transfers/${transferId}/cancel`),
};

// ========================================
// 庫存警示 API / Stock alert API
// ========================================
export const alertApi = {
  listUnacknowledged: (storeId: string) =>
    axiosInstance.get<unknown, ApiResponse<StockAlert[]>>(`${BASE}/stores/${storeId}/alerts`),

  acknowledge: (alertId: string, acknowledgedBy?: string) =>
    axiosInstance.post<unknown, ApiResponse<StockAlert>>(
      `${BASE}/alerts/${alertId}/acknowledge`,
      null,
      { params: { acknowledgedBy } }
    ),
};

// ========================================
// 盤點 API / Stock take API
// ========================================
export const stockTakeApi = {
  start: (storeId: string, createdBy?: string) =>
    axiosInstance.post<unknown, ApiResponse<StockTake>>(
      `${BASE}/stock-takes/start`,
      null,
      { params: { storeId, createdBy } }
    ),

  listByStore: (storeId: string) =>
    axiosInstance.get<unknown, ApiResponse<StockTake[]>>(`${BASE}/stock-takes/stores/${storeId}`),

  getById: (stockTakeId: string) =>
    axiosInstance.get<unknown, ApiResponse<StockTake>>(`${BASE}/stock-takes/${stockTakeId}`),

  submitCount: (stockTakeId: string, itemId: string, countedQty: number) =>
    axiosInstance.post<unknown, ApiResponse<StockTake>>(
      `${BASE}/stock-takes/${stockTakeId}/items/${itemId}/count`,
      null,
      { params: { countedQty } }
    ),

  complete: (stockTakeId: string) =>
    axiosInstance.post<unknown, ApiResponse<StockTake>>(`${BASE}/stock-takes/${stockTakeId}/complete`),

  cancel: (stockTakeId: string) =>
    axiosInstance.post<unknown, ApiResponse<StockTake>>(`${BASE}/stock-takes/${stockTakeId}/cancel`),
};
