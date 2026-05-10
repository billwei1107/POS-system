/**
 * @file productApi.ts
 * @description 商品管理 API 層 / Product management API layer
 * @description_en API calls for product categories and items management
 * @description_zh 商品分類與商品主表的 API 呼叫
 */
import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse, PaginatedData } from '../../../shared/types';
import type {
  Category,
  CategoryRequest,
  ProductItem,
  ProductItemRequest,
  ProductListParams,
} from '../types';

export const productApi = {
  // ========================================
  // 商品分類 / Product Categories
  // ========================================

  getCategories: () =>
    axiosInstance
      .get<unknown, ApiResponse<Category[]>>('/v1/pos/categories'),

  getRootCategories: () =>
    axiosInstance
      .get<unknown, ApiResponse<Category[]>>('/v1/pos/categories/roots'),

  getCategoryChildren: (id: string) =>
    axiosInstance
      .get<unknown, ApiResponse<Category[]>>(`/v1/pos/categories/${id}/children`),

  createCategory: (data: CategoryRequest) =>
    axiosInstance
      .post<unknown, ApiResponse<Category>, CategoryRequest>('/v1/pos/categories', data),

  updateCategory: (id: string, data: CategoryRequest) =>
    axiosInstance
      .put<unknown, ApiResponse<Category>, CategoryRequest>(`/v1/pos/categories/${id}`, data),

  deleteCategory: (id: string) =>
    axiosInstance
      .delete<unknown, ApiResponse<void>>(`/v1/pos/categories/${id}`),

  // ========================================
  // 商品 / Products
  // ========================================

  getProducts: (params: ProductListParams = {}) =>
    axiosInstance
      .get<unknown, ApiResponse<PaginatedData<ProductItem>>>('/v1/pos/products', { params }),

  getProductById: (id: string) =>
    axiosInstance
      .get<unknown, ApiResponse<ProductItem>>(`/v1/pos/products/${id}`),

  getProductBySku: (sku: string) =>
    axiosInstance
      .get<unknown, ApiResponse<ProductItem>>(`/v1/pos/products/by-sku/${sku}`),

  createProduct: (data: ProductItemRequest) =>
    axiosInstance
      .post<unknown, ApiResponse<ProductItem>, ProductItemRequest>('/v1/pos/products', data),

  updateProduct: (id: string, data: ProductItemRequest) =>
    axiosInstance
      .put<unknown, ApiResponse<ProductItem>, ProductItemRequest>(`/v1/pos/products/${id}`, data),

  deleteProduct: (id: string) =>
    axiosInstance
      .delete<unknown, ApiResponse<void>>(`/v1/pos/products/${id}`),
};
