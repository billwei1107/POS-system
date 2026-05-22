import { useEffect } from 'react';
import { Alert, Box, Paper, Typography } from '@mui/material';
import { Navigate, useLocation } from 'react-router-dom';
import { getTokenRole, useAuthStore } from '@shared/store/authStore';
import { APP_BRAND } from '@shared/config/appBrand';
import { LoginForm } from '../components/LoginForm';

const DEFAULT_REDIRECT_PATH = '/pos/register';

const resolveRedirectPath = (search: string) => {
    const redirect = new URLSearchParams(search).get('redirect');
    if (!redirect || !redirect.startsWith('/') || redirect.startsWith('//')) {
        return DEFAULT_REDIRECT_PATH;
    }
    return redirect;
};

/**
 * @file LoginPage.tsx
 * @description 獨立全螢幕登入視圖 / Fullscreen login view
 */
export const LoginPage = () => {
    const location = useLocation();
    const hasHydrated = useAuthStore((state) => state.hasHydrated);
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    const authUser = useAuthStore((state) => state.user);
    const token = useAuthStore((state) => state.token);
    const logout = useAuthStore((state) => state.logout);
    const reason = new URLSearchParams(location.search).get('reason');
    const requiresAdminLogin = reason === 'admin-permission';
    const currentRole = authUser?.role ?? getTokenRole(token);
    const shouldClearForAdminLogin = requiresAdminLogin && isAuthenticated && currentRole !== 'SUPER_ADMIN';

    useEffect(() => {
        if (hasHydrated && shouldClearForAdminLogin) {
            logout();
        }
    }, [hasHydrated, logout, shouldClearForAdminLogin]);

    if (!hasHydrated) {
        return null;
    }

    if (shouldClearForAdminLogin) {
        return null;
    }

    if (isAuthenticated) {
        return <Navigate to={resolveRedirectPath(location.search)} replace />;
    }

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
                <Typography
                    variant="h3"
                    component="h1"
                    gutterBottom
                    align="center"
                    aria-label={APP_BRAND.productName}
                    sx={{ fontWeight: 800, color: '#1e293b', letterSpacing: 0 }}
                >
                    {APP_BRAND.productNameParts.primary}
                    <Box component="span" sx={{ color: APP_BRAND.colors.accent }}>
                        {APP_BRAND.productNameParts.accent}
                    </Box>
                </Typography>
                <Typography variant="subtitle1" align="center" color="text.secondary" sx={{ mb: 3 }}>
                    {APP_BRAND.subtitle}
                </Typography>
                {requiresAdminLogin && (
                    <Alert severity="info" sx={{ mb: 2 }}>
                        此功能需要系統管理員帳號，請重新登入後台。
                    </Alert>
                )}

                <LoginForm />

                <Typography variant="body2" align="center" color="text.disabled" sx={{ mt: 4 }}>
                    {APP_BRAND.copyright}
                </Typography>
            </Paper>
        </Box>
    );
};
