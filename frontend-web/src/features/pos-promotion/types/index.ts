/**
 * @file index.ts
 * @description POS 促銷型別定義 / POS promotion type definitions
 * @description_en Type definitions for promotion rules and evaluation results
 * @description_zh 定義促銷規則與試算結果型別
 */

export type PromotionTriggerType = 'AUTO' | 'CODE';
export type PromotionDiscountType = 'PERCENT' | 'AMOUNT';

export interface PromotionRule {
  id: string;
  storeId: string | null;
  name: string;
  code: string | null;
  triggerType: PromotionTriggerType;
  discountType: PromotionDiscountType;
  discountValue: number;
  minimumSubtotal: number;
  maxDiscountAmount: number | null;
  startsAt: string | null;
  endsAt: string | null;
  active: boolean;
}

export interface PromotionRuleRequest {
  storeId?: string | null;
  name: string;
  code?: string | null;
  triggerType: PromotionTriggerType;
  discountType: PromotionDiscountType;
  discountValue: number;
  minimumSubtotal: number;
  maxDiscountAmount?: number | null;
  startsAt?: string | null;
  endsAt?: string | null;
  active: boolean;
}

export interface PromotionEvaluationRequest {
  storeId: string;
  subtotal: number;
  code?: string | null;
}

export interface PromotionEvaluationResult {
  applied: boolean;
  ruleId: string | null;
  name: string | null;
  code: string | null;
  discountType: PromotionDiscountType | null;
  discountValue: number;
  discountAmount: number;
  reason: string | null;
}
