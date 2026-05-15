/**
 * @file PromotionRulePage.test.tsx
 * @description 促銷規則頁測試 / Promotion rule page tests
 * @description_en Verifies promotion rule listing and evaluation result rendering
 * @description_zh 驗證促銷規則列表與試算結果顯示
 */
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PromotionRulePage from '../../src/features/pos-promotion/pages/PromotionRulePage';
import { promotionApi } from '../../src/features/pos-promotion/api/promotionApi';

vi.mock('../../src/features/pos-promotion/api/promotionApi', () => ({
  promotionApi: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    deactivate: vi.fn(),
    evaluate: vi.fn(),
  },
}));

const mockPromotionApi = vi.mocked(promotionApi);

beforeEach(() => {
  vi.clearAllMocks();
  window.localStorage.clear();
  mockPromotionApi.list.mockResolvedValue({
    success: true,
    code: 200,
    message: 'Success',
    data: [
      {
        id: 'promo-1',
        storeId: null,
        name: '咖啡滿百 9 折',
        code: null,
        triggerType: 'AUTO',
        discountType: 'PERCENT',
        discountValue: 10,
        minimumSubtotal: 100,
        maxDiscountAmount: 80,
        startsAt: null,
        endsAt: null,
        active: true,
      },
    ],
  });
  mockPromotionApi.evaluate.mockResolvedValue({
    success: true,
    code: 200,
    message: 'Success',
    data: {
      applied: true,
      ruleId: 'promo-1',
      name: '咖啡滿百 9 折',
      code: null,
      discountType: 'PERCENT',
      discountValue: 10,
      discountAmount: 14.5,
      reason: null,
    },
  });
});

describe('PromotionRulePage', () => {
  it('renders promotion rules and evaluation result', async () => {
    render(<PromotionRulePage />);

    expect(await screen.findByText('咖啡滿百 9 折')).toBeInTheDocument();
    expect(screen.getByText('自動套用')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '試算' }));

    await waitFor(() => {
      expect(mockPromotionApi.evaluate).toHaveBeenCalledWith(expect.objectContaining({
        subtotal: 145,
      }));
    });
    expect(await screen.findByText(/折抵 \$15/)).toBeInTheDocument();
  });
});
