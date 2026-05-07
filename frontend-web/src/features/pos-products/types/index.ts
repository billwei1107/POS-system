/**
 * @file types/index.ts
 * @description 商品管理型別定義 / Product management type definitions
 */

export type UnitType = 'PCS' | 'KG' | 'LB' | 'ML' | 'L';

export interface Category {
  id: string;
  name: string;
  parentId: string | null;
  sortOrder: number;
  imageUrl: string | null;
  displayColor: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CategoryRequest {
  name: string;
  parentId?: string | null;
  sortOrder?: number;
  imageUrl?: string | null;
  displayColor?: string | null;
  active?: boolean;
}

export interface ProductItem {
  id: string;
  sku: string;
  name: string;
  description: string | null;
  categoryId: string | null;
  basePrice: number;
  costPrice: number | null;
  taxClassId: string | null;
  unit: UnitType;
  barcodePrimary: string | null;
  imageUrl: string | null;
  trackInventory: boolean;
  sellable: boolean;
  weightBased: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductItemRequest {
  sku: string;
  name: string;
  description?: string;
  categoryId?: string | null;
  basePrice: number;
  costPrice?: number | null;
  taxClassId?: string | null;
  unit?: UnitType;
  barcodePrimary?: string | null;
  imageUrl?: string | null;
  trackInventory?: boolean;
  sellable?: boolean;
  weightBased?: boolean;
  active?: boolean;
}

export interface ProductListParams {
  page?: number;
  size?: number;
  categoryId?: string;
  keyword?: string;
}
