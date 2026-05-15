/**
 * @file memberApi.ts
 * @description POS 會員 API 層 / POS member API layer
 * @description_en Axios client for POS member lookup, creation and loyalty point operations
 * @description_zh POS 會員查詢、建立與忠誠點數操作的 Axios API 客戶端
 */
import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse } from '../../../shared/types';
import type {
  Member,
  MemberRequest,
  PointAdjustmentRequest,
  PointLedger,
  PointRedemptionRequest,
} from '../types';

const BASE = '/v1/pos/members';

export const memberApi = {
  search: (query: string, limit = 10) =>
    axiosInstance.get<unknown, ApiResponse<Member[]>>(`${BASE}/search`, { params: { query, limit } }),

  getById: (id: string) =>
    axiosInstance.get<unknown, ApiResponse<Member>>(`${BASE}/${id}`),

  create: (req: MemberRequest) =>
    axiosInstance.post<unknown, ApiResponse<Member>, MemberRequest>(BASE, req),

  listPointLedgers: (id: string) =>
    axiosInstance.get<unknown, ApiResponse<PointLedger[]>>(`${BASE}/${id}/points`),

  redeemPoints: (id: string, req: PointRedemptionRequest) =>
    axiosInstance.post<unknown, ApiResponse<PointLedger>, PointRedemptionRequest>(`${BASE}/${id}/points/redemptions`, req),

  adjustPoints: (id: string, req: PointAdjustmentRequest) =>
    axiosInstance.post<unknown, ApiResponse<PointLedger>, PointAdjustmentRequest>(`${BASE}/${id}/points/adjustments`, req),
};
