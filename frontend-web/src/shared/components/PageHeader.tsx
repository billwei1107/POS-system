/**
 * @file PageHeader.tsx
 * @description 頁面標題列組件 / Page header component
 */
import { Box, Typography, Divider } from '@mui/material';
import type { ReactNode } from 'react';

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  actions?: ReactNode;
}

export function PageHeader({ title, subtitle, actions }: PageHeaderProps) {
  return (
    <Box>
      <Box
        sx={{
          display: 'flex',
          flexDirection: { xs: 'column', sm: 'row' },
          alignItems: { xs: 'stretch', sm: 'center' },
          justifyContent: 'space-between',
          gap: 1.5,
          mb: 1,
        }}
      >
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="h5" fontWeight={800} sx={{ lineHeight: 1.2 }}>{title}</Typography>
          {subtitle && (
            <Typography variant="body2" color="text.secondary" mt={0.5}>{subtitle}</Typography>
          )}
        </Box>
        {actions && (
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', '& > *': { flex: { xs: '1 1 auto', sm: '0 0 auto' } } }}>
            {actions}
          </Box>
        )}
      </Box>
      <Divider sx={{ mb: 3 }} />
    </Box>
  );
}
