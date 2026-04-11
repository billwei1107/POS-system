import { Box, Paper, Typography } from '@mui/material';
import { LoginForm } from '../components/LoginForm';

/**
 * @file LoginPage.tsx
 * @description 獨立全螢幕登入視圖 / Fullscreen login view
 */
export const LoginPage = () => {
    return (
        <Box sx={{
            minHeight: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)'
        }}>
            <Paper
                elevation={24}
                sx={{
                    p: 5,
                    width: '100%',
                    maxWidth: 420,
                    borderRadius: 3,
                    background: 'rgba(255, 255, 255, 0.95)',
                    backdropFilter: 'blur(10px)'
                }}
            >
                <Typography variant="h3" component="h1" gutterBottom align="center" sx={{ fontWeight: 800, color: '#1e293b', letterSpacing: -1 }}>
                    HIVE<span style={{ color: '#0d9488' }}>.ERP</span>
                </Typography>
                <Typography variant="subtitle1" align="center" color="text.secondary" sx={{ mb: 3 }}>
                    企業模塊化組件系統
                </Typography>

                <LoginForm />

                <Typography variant="body2" align="center" color="text.disabled" sx={{ mt: 4 }}>
                    © 2026 Enterprise Inc. All rights reserved.
                </Typography>
            </Paper>
        </Box>
    );
};
