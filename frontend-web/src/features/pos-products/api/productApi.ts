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
      .get<ApiResponse<Category[]>>('/v1/pos/categories')
      .then((res) => res.data),

  getRootCategories: () =>
    axiosInstance
      .get<ApiResponse<Category[]>>('/v1/pos/categories/roots')
      .then((res) => res.data),

  getCategoryChildren: (id: string) =>
    axiosInstance
      .get<ApiResponse<Category[]>>(`/v1/pos/categories/${id}/children`)
      .then((res) => res.data),

  createCategory: (data: CategoryRequest) =>
    axiosInstance
      .post<ApiResponse<Category>>('/v1/pos/categories', data)
      .then((res) => res.data),

  updateCategory: (id: string, data: CategoryRequest) =>
    axiosInstance
      .put<ApiResponse<Category>>(`/v1/pos/categories/${id}`, data)
      .then((res) => res.data),

  deleteCategory: (id: string) =>
    axiosInstance
      .delete<ApiResponse<void>>(`/v1/pos/categories/${id}`)
      .then((res) => res.data),

  // ========================================
  // 商品 / Products
  // ========================================

  getProducts: (params: ProductListParams = {}) =>
    axiosInstance
      .get<ApiResponse<PaginatedData<ProductItem>>>('/v1/pos/products', { params })
      .then((res) => res.data),

  getProductById: (id: string) =>
    axiosInstance
      .get<ApiResponse<ProductItem>>(`/v1/pos/products/${id}`)
      .then((res) => res.data),

  getProductBySku: (sku: string) =>
    axiosInstance
      .get<ApiResponse<ProductItem>>(`/v1/pos/products/by-sku/${sku}`)
      .then((res) => res.data),

  createProduct: (data: ProductItemRequest) =>
    axiosInstance
      .post<ApiResponse<ProductItem>>('/v1/pos/products', data)
      .then((res) => res.data),

  updateProduct: (id: string, data: ProductItemRequest) =>
    axiosInstance
      .put<ApiResponse<ProductItem>>(`/v1/pos/products/${id}`, data)
      .then((res) => res.data),

  deleteProduct: (id: string) =>
    axiosInstance
      .delete<ApiResponse<void>>(`/v1/pos/products/${id}`)
      .then((res) => res.data),
};
