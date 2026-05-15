/**
 * @file promotionApi.ts
 * @description POS 促銷 API 層 / POS promotion API layer
 * @description_en Axios API client for promotion rules and order discount evaluation
 * @description_zh 促銷規則與訂單折扣試算 API 客戶端
 */
import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse } from '../../../shared/types';
import type {
  PromotionEvaluationRequest,
  PromotionEvaluationResult,
  PromotionRule,
  PromotionRuleRequest,
} from '../types';

const BASE = '/v1/pos/promotions';

export const promotionApi = {
  list: (storeId: string) =>
    axiosInstance.get<unknown, ApiResponse<PromotionRule[]>>(BASE, { params: { storeId } }),

  create: (req: PromotionRuleRequest) =>
    axiosInstance.post<unknown, ApiResponse<PromotionRule>, PromotionRuleRequest>(BASE, req),

  update: (id: string, req: PromotionRuleRequest) =>
    axiosInstance.put<unknown, ApiResponse<PromotionRule>, PromotionRuleRequest>(`${BASE}/${id}`, req),

  deactivate: (id: string) =>
    axiosInstance.delete<unknown, ApiResponse<null>>(`${BASE}/${id}`),

  evaluate: (req: PromotionEvaluationRequest) =>
    axiosInstance.post<unknown, ApiResponse<PromotionEvaluationResult>, PromotionEvaluationRequest>(
      `${BASE}/evaluate`,
      req
    ),
};
