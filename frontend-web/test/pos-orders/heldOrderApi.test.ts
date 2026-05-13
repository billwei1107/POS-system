/**
 * @file heldOrderApi.test.ts
 * @description 掛單 API 測試 / Held order API tests
 * @description_en Verifies API-backed hold, list and remove calls
 * @description_zh 驗證後端掛單建立、查詢與刪除呼叫
 */
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { ApiResponse } from '../../src/shared/types';
import type { HeldOrderResponse } from '../../src/features/pos-orders/types';

const axiosMock = vi.hoisted(() => ({
  post: vi.fn(),
  get: vi.fn(),
  delete: vi.fn(),
}));

vi.mock('../../src/shared/api/axiosInstance', () => ({
  default: axiosMock,
}));

describe('heldOrderApi', () => {
  beforeEach(() => {
    axiosMock.post.mockReset();
    axiosMock.get.mockReset();
    axiosMock.delete.mockReset();
  });

  it('creates, lists and removes held orders through the backend API', async () => {
    const { heldOrderApi } = await import('../../src/features/pos-orders/api/orderApi');
    const heldOrder: HeldOrderResponse = {
      id: 'hold-001',
      storeId: 'store-demo',
      terminalId: 'terminal-01',
      label: 'H000001',
      payload: '{"lines":[]}',
      heldAt: '2026-05-11T03:20:00Z',
      createdAt: '2026-05-11T03:20:00Z',
    };
    const response: ApiResponse<HeldOrderResponse> = {
      success: true,
      message: 'ok',
      data: heldOrder,
      code: 200,
    };
    axiosMock.post.mockResolvedValue(response);
    axiosMock.get.mockResolvedValue({ ...response, data: [heldOrder] });
    axiosMock.delete.mockResolvedValue({ ...response, data: undefined });

    await expect(heldOrderApi.create({
      storeId: 'store-demo',
      terminalId: 'terminal-01',
      label: 'H000001',
      payload: '{"lines":[]}',
    })).resolves.toBe(response);
    await heldOrderApi.list('store-demo', 'terminal-01');
    await heldOrderApi.remove('hold-001');

    expect(axiosMock.post).toHaveBeenCalledWith('/v1/pos/held-orders', {
      storeId: 'store-demo',
      terminalId: 'terminal-01',
      label: 'H000001',
      payload: '{"lines":[]}',
    });
    expect(axiosMock.get).toHaveBeenCalledWith('/v1/pos/held-orders', {
      params: { storeId: 'store-demo', terminalId: 'terminal-01' },
    });
    expect(axiosMock.delete).toHaveBeenCalledWith('/v1/pos/held-orders/hold-001');
  });
});
