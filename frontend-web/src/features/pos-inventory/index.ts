/**
 * @file index.ts
 * @description POS 庫存管理 Feature 匯出 / POS inventory feature barrel export
 * @description_en Barrel export for pos-inventory feature pages and API
 * @description_zh pos-inventory feature 頁面與 API 的統一匯出入口
 */
export { default as StockOverviewPage } from './pages/StockOverviewPage';
export { default as TransferPage } from './pages/TransferPage';
export { default as StockTakePage } from './pages/StockTakePage';
export { stockApi, transferApi, alertApi, stockTakeApi } from './api/inventoryApi';
export type * from './types';
