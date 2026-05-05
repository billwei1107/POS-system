/**
 * @file StatusChip.tsx
 * @description 狀態標籤組件 / Status chip component
 */
import { Chip } from '@mui/material';
import type { ChipProps } from '@mui/material';

interface StatusChipProps {
  status: string;
  labelMap?: Record<string, string>;
  colorMap?: Record<string, ChipProps['color']>;
}

const defaultColorMap: Record<string, ChipProps['color']> = {
  ACTIVE: 'success',
  ENABLED: 'success',
  COMPLETED: 'success',
  APPROVED: 'success',
  INACTIVE: 'default',
  DISABLED: 'default',
  CANCELLED: 'default',
  PENDING: 'warning',
  DRAFT: 'warning',
  PROCESSING: 'info',
  REJECTED: 'error',
  FAILED: 'error',
};

export function StatusChip({ status, labelMap, colorMap }: StatusChipProps) {
  const label = labelMap?.[status] ?? status;
  const color = (colorMap ?? defaultColorMap)[status] ?? 'default';

  return (
    <Chip label={label} color={color} size="small" />
  );
}
