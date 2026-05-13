/**
 * @file dateFormat.test.ts
 * @description 日期格式化工具測試 / Date formatting utility tests
 * @description_en Verifies POS timezone conversion for backend API timestamps
 * @description_zh 驗證 POS 後端 API 時間戳會正確轉為台灣時區顯示
 */
import { describe, expect, it } from 'vitest';
import { formatDateTime, parseApiDate } from '../../src/shared/utils';

describe('dateFormat utilities', () => {
  it('treats backend LocalDateTime strings as UTC before formatting to Taipei time', () => {
    expect(parseApiDate('2026-05-11T08:34:17.952569').toISOString()).toBe('2026-05-11T08:34:17.952Z');
    expect(formatDateTime('2026-05-11T08:34:17.952569')).toContain('16:34');
  });

  it('keeps explicit timezone strings intact', () => {
    expect(parseApiDate('2026-05-11T08:34:17.952569+08:00').toISOString()).toBe('2026-05-11T00:34:17.952Z');
    expect(formatDateTime('2026-05-11T08:34:17.952569+08:00')).toContain('08:34');
  });
});
