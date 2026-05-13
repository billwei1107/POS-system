/**
 * @file config.ts
 * @description POS 訂單前端設定 / POS order frontend configuration
 * @description_en Centralizes register defaults used by local checkout flows
 * @description_zh 集中管理本地 checkout 流程使用的門店、終端與操作員預設值
 */

export const DEFAULT_STORE_ID =
  import.meta.env.VITE_DEFAULT_STORE_ID || '00000000-0000-0000-0000-000000000001';

export const DEFAULT_TERMINAL_ID =
  import.meta.env.VITE_DEFAULT_TERMINAL_ID || '00000000-0000-0000-0000-000000000101';

export const DEFAULT_EMPLOYEE_ID =
  import.meta.env.VITE_DEFAULT_EMPLOYEE_ID || '00000000-0000-0000-0000-000000000201';

export const DEFAULT_TABLE_NO = import.meta.env.VITE_DEFAULT_TABLE_NO || 'A7';
export const DEFAULT_GUEST_COUNT = Number(import.meta.env.VITE_DEFAULT_GUEST_COUNT || 2);

export interface DemoPosMember {
  id: string;
  memberNo: string;
  name: string;
  phoneMasked: string;
  tier: string;
  points: number;
  discountPercent: number;
}

export const DEMO_POS_MEMBERS: DemoPosMember[] = [
  {
    id: '00000000-0000-0000-0000-000000000801',
    memberNo: 'M-000801',
    name: '林依晨',
    phoneMasked: '0912-***-801',
    tier: '金卡',
    points: 1280,
    discountPercent: 10,
  },
  {
    id: '00000000-0000-0000-0000-000000000802',
    memberNo: 'M-000802',
    name: '陳柏宇',
    phoneMasked: '0922-***-802',
    tier: '銀卡',
    points: 640,
    discountPercent: 5,
  },
  {
    id: '00000000-0000-0000-0000-000000000803',
    memberNo: 'M-000803',
    name: '王小安',
    phoneMasked: '0933-***-803',
    tier: '一般',
    points: 120,
    discountPercent: 0,
  },
];
