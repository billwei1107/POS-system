/**
 * @file validators.ts
 * @description 表單驗證工具 / Form validation utilities
 */

// ========================================
// 欄位驗證 / Field Validators
// ========================================

export const validators = {
  required: (value: unknown): string | undefined =>
    !value && value !== 0 ? '此欄位為必填' : undefined,

  email: (value: string): string | undefined =>
    value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
      ? '請輸入有效的 Email 格式'
      : undefined,

  minLength:
    (min: number) =>
    (value: string): string | undefined =>
      value && value.length < min ? `最少需要 ${min} 個字元` : undefined,

  maxLength:
    (max: number) =>
    (value: string): string | undefined =>
      value && value.length > max ? `最多 ${max} 個字元` : undefined,

  phone: (value: string): string | undefined =>
    value && !/^[0-9]{8,15}$/.test(value.replace(/[-\s]/g, ''))
      ? '請輸入有效的電話號碼'
      : undefined,

  positiveNumber: (value: unknown): string | undefined =>
    value !== undefined && value !== null && (isNaN(Number(value)) || Number(value) <= 0)
      ? '請輸入正數'
      : undefined,

  nonNegativeNumber: (value: unknown): string | undefined =>
    value !== undefined && value !== null && (isNaN(Number(value)) || Number(value) < 0)
      ? '請輸入非負數'
      : undefined,
};

export function composeValidators(
  ...fns: Array<(value: unknown) => string | undefined>
) {
  return (value: unknown): string | undefined => {
    for (const fn of fns) {
      const error = fn(value);
      if (error) return error;
    }
    return undefined;
  };
}
