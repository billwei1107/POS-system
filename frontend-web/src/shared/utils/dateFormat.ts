/**
 * @file dateFormat.ts
 * @description 日期格式化工具 / Date formatting utilities
 */

export const POS_TIME_ZONE = 'Asia/Taipei';

const BACKEND_LOCAL_DATE_TIME_PATTERN = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?$/;

// ========================================
// 日期格式化 / Date Formatting
// ========================================

// ========================================
// API 時間解析 / API Date Parsing
// ========================================
export function parseApiDate(date: Date | string): Date {
  if (date instanceof Date) return date;
  const normalized = BACKEND_LOCAL_DATE_TIME_PATTERN.test(date) ? `${date}Z` : date;
  return new Date(normalized);
}

export function formatDate(date: Date | string | null | undefined): string {
  if (!date) return '-';
  const d = parseApiDate(date);
  if (isNaN(d.getTime())) return '-';
  return new Intl.DateTimeFormat('zh-TW', {
    timeZone: POS_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(d);
}

export function formatDateTime(date: Date | string | null | undefined): string {
  if (!date) return '-';
  const d = parseApiDate(date);
  if (isNaN(d.getTime())) return '-';
  return new Intl.DateTimeFormat('zh-TW', {
    timeZone: POS_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(d);
}

export function formatTime(date: Date | string | null | undefined): string {
  if (!date) return '-';
  const d = parseApiDate(date);
  if (isNaN(d.getTime())) return '-';
  return new Intl.DateTimeFormat('zh-TW', {
    timeZone: POS_TIME_ZONE,
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(d);
}

export function toISODateString(date: Date): string {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: POS_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(date);
  const values = Object.fromEntries(parts.map(part => [part.type, part.value]));
  return `${values.year}-${values.month}-${values.day}`;
}

export function formatPosClock(date: Date | string | null | undefined): string {
  if (!date) return '-';
  const d = parseApiDate(date);
  if (isNaN(d.getTime())) return '-';
  const time = new Intl.DateTimeFormat('zh-TW', {
    timeZone: POS_TIME_ZONE,
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(d);
  const day = new Intl.DateTimeFormat('zh-TW', {
    timeZone: POS_TIME_ZONE,
    month: '2-digit',
    day: '2-digit',
  }).format(d);
  return `${time} · ${day} GMT+8`;
}
