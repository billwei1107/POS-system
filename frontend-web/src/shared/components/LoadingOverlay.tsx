/**
 * @file LoadingOverlay.tsx
 * @description 全頁載入遮罩組件 / Full-page loading overlay component
 */
import { Backdrop, CircularProgress, Typography, Box } from '@mui/material';

interface LoadingOverlayProps {
  open: boolean;
  message?: string;
}

export function LoadingOverlay({ open, message }: LoadingOverlayProps) {
  return (
    <Backdrop open={open} sx={{ zIndex: theme => theme.zIndex.modal + 1, color: '#fff', flexDirection: 'column', gap: 2 }}>
      <CircularProgress color="inherit" />
      {message && <Box><Typography variant="body2">{message}</Typography></Box>}
    </Backdrop>
  );
}
