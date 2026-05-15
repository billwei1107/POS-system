/**
 * @file index.ts
 * @description POS CRM 型別定義 / POS CRM type definitions
 * @description_en TypeScript interfaces for POS members and loyalty point ledgers
 * @description_zh POS 會員與忠誠點數流水的 TypeScript 型別定義
 */

export type MemberTier = 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM';
export type PointReason = 'ORDER_EARN' | 'MANUAL_ADJUST' | 'REDEEM' | 'REFUND_REVERSE' | 'EXPIRE';

export interface Member {
  id: string;
  memberNo: string;
  name: string;
  phone: string;
  phoneMasked: string;
  email: string | null;
  birthday: string | null;
  cardNo: string | null;
  barcode: string | null;
  tier: MemberTier;
  tierLabel: string;
  discountPercent: number;
  pointsBalance: number;
  storedValueBalance: number;
  annualSpend: number;
  active: boolean;
}

export interface MemberRequest {
  memberNo?: string;
  name: string;
  phone: string;
  email?: string;
  birthday?: string;
  cardNo?: string;
  barcode?: string;
  tier?: MemberTier;
  discountPercent?: number;
}

export interface PointLedger {
  id: string;
  memberId: string;
  orderId: string | null;
  referenceId: string | null;
  referenceType: string | null;
  pointsDelta: number;
  balanceAfter: number;
  reason: PointReason;
  note: string | null;
  occurredAt: string;
}

export interface PointAdjustmentRequest {
  pointsDelta: number;
  note?: string;
}

export interface PointRedemptionRequest {
  points: number;
  orderId?: string;
  note?: string;
}
