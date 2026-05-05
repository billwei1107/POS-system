/**
 * @file moneyFormat.ts
 * @description 金額格式化工具 / Money formatting utilities
 */

// ========================================
// 金額格式化 / Money Formatting
// ========================================

export function formatMoney(
  amount: number | string | null | undefined,
  currency = 'TWD'
): string {
  if (amount === null || amount === undefined || amount === '') return '-';
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  if (isNaN(num)) return '-';
  return new Intl.NumberFormat('zh-TW', {
    style: 'currency',
    currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(num);
}

export function formatNumber(value: number | string | null | undefined): string {
  if (value === null || value === undefined || value === '') return '-';
  const num = typeof value === 'string' ? parseFloat(value) : value;
  if (isNaN(num)) return '-';
  return new Intl.NumberFormat('zh-TW').format(num);
}

export function parseMoney(value: string): number {
  return parseFloat(value.replace(/[^0-9.-]/g, '')) || 0;
}
