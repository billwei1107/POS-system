/**
 * @file PosLoginPage.tsx
 * @description POS 終端機專用 PIN 碼登入畫面 / POS Terminal PIN Login Screen
 * @description_en Fullscreen PIN login specifically for physical POS terminals
 * @description_zh 實體 POS 機專用的全螢幕 PIN 碼登入介面
 */
import React, { useState, useEffect } from 'react';
import { Alert, Box, Typography, Avatar, IconButton, Button, AvatarGroup, CircularProgress } from '@mui/material';
import { ArrowForward, Backspace, PointOfSale, Add } from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuthStore } from '../../../shared/store/authStore';
import { pinLoginApi } from '../api/posAuthApi';
import { formatPosClock } from '@shared/utils';

const DEFAULT_TERMINAL_CODE = import.meta.env.VITE_DEFAULT_TERMINAL_CODE || 'DEMO-T-001';
const DEFAULT_REDIRECT_PATH = '/pos/register';

const resolvePosRedirectPath = (search: string) => {
    const redirect = new URLSearchParams(search).get('redirect');
    if (!redirect || !redirect.startsWith('/pos') || redirect.startsWith('//') || redirect === '/pos/login') {
        return DEFAULT_REDIRECT_PATH;
    }
    return redirect;
};

const PosLoginPage: React.FC = () => {
    const [pin, setPin] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const location = useLocation();
    const setAuth = useAuthStore((state) => state.setAuth);
    const hasHydrated = useAuthStore((state) => state.hasHydrated);
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

    // 更新當前時間
    const [currentTime, setCurrentTime] = useState(new Date());
    useEffect(() => {
        const timer = setInterval(() => setCurrentTime(new Date()), 1000);
        return () => clearInterval(timer);
    }, []);

    useEffect(() => {
        if (hasHydrated && isAuthenticated) {
            navigate(resolvePosRedirectPath(location.search), { replace: true });
        }
    }, [hasHydrated, isAuthenticated, location.search, navigate]);

    const handleNumberClick = (num: string) => {
        if (pin.length < 6) {
            setPin(prev => prev + num);
        }
    };

    const handleDelete = () => {
        setPin(prev => prev.slice(0, -1));
    };

    const handleSubmit = async () => {
        if (pin.length < 4 || loading) return;

        setLoading(true);
        setError('');
        try {
            const response = await pinLoginApi({
                pin,
                terminalCode: DEFAULT_TERMINAL_CODE,
            });

            setAuth(
                { id: response.userId, username: response.username, role: response.role },
                response.token
            );
            localStorage.setItem('pos-session', JSON.stringify({
                storeId: response.storeId,
                storeName: response.storeName,
                terminalId: response.terminalId,
                terminalCode: response.terminalCode ?? DEFAULT_TERMINAL_CODE,
                terminalName: response.terminalName,
                employeeId: response.employeeId,
                userId: response.userId,
                username: response.username,
                role: response.role,
            }));
            navigate(resolvePosRedirectPath(location.search), { replace: true });
        } catch (err: unknown) {
            const message = axios.isAxiosError<{ message?: string }>(err)
                ? err.response?.data?.message
                : undefined;
            setError(message || 'PIN 登入失敗，請確認 PIN 碼或終端設定');
            setPin('');
        } finally {
            setLoading(false);
        }
    };

    if (!hasHydrated) {
        return null;
    }

    return (
        <Box sx={{ 
            height: '100vh', 
            width: '100vw', 
            bgcolor: '#16171D',
            color: 'white',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden'
        }}>
            {/* Top Bar */}
            <Box sx={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center', 
                p: 3 
            }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                    <PointOfSale sx={{ color: 'text.secondary' }} />
                    <Typography variant="h6" fontWeight="bold">T-001</Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, bgcolor: 'rgba(76, 175, 80, 0.1)', px: 1, py: 0.5, borderRadius: 5 }}>
                        <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: '#4CAF50' }} />
                        <Typography variant="caption" sx={{ color: '#4CAF50', fontWeight: 'bold' }}>Online</Typography>
                    </Box>
                </Box>
                <Box sx={{ bgcolor: 'rgba(255,255,255,0.05)', px: 2, py: 1, borderRadius: 2 }}>
                    <Typography variant="body2" color="text.secondary" fontWeight="500">
                        {formatPosClock(currentTime)}
                    </Typography>
                </Box>
            </Box>

            {/* Main Content */}
            <Box sx={{ 
                flexGrow: 1, 
                display: 'flex', 
                flexDirection: 'column', 
                alignItems: 'center',
                justifyContent: 'center',
                mb: 8
            }}>
                {/* Store & User Info */}
                <Avatar 
                    src="https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=150&q=80" 
                    sx={{ width: 80, height: 80, mb: 3, border: '3px solid rgba(255,255,255,0.1)' }}
                />
                <Typography variant="h4" fontWeight="bold" sx={{ mb: 1 }}>
                    Xinyi Flagship Store
                </Typography>
                <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
                    Please enter PIN
                </Typography>
                {error && (
                    <Alert severity="error" sx={{ mb: 3, width: 320 }}>
                        {error}
                    </Alert>
                )}

                {/* PIN Dots */}
                <Box sx={{ display: 'flex', gap: 2, mb: 6 }}>
                    {[...Array(6)].map((_, i) => (
                        <Box 
                            key={i}
                            sx={{
                                width: 14,
                                height: 14,
                                borderRadius: '50%',
                                transition: 'all 0.2s',
                                bgcolor: i < pin.length ? '#b69aff' : 'rgba(255,255,255,0.1)',
                                boxShadow: i < pin.length ? '0 0 10px rgba(182, 154, 255, 0.5)' : 'none'
                            }}
                        />
                    ))}
                </Box>

                {/* Keypad */}
                <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 2 }}>
                    {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
                        <Button
                            key={num}
                            variant="contained"
                            onClick={() => handleNumberClick(num.toString())}
                            sx={{
                                width: 80,
                                height: 80,
                                borderRadius: 3,
                                bgcolor: 'rgba(255,255,255,0.08)',
                                fontSize: '28px',
                                fontWeight: 'bold',
                                color: 'white',
                                boxShadow: 'none',
                                transition: 'all 0.1s',
                                '&:hover': {
                                    bgcolor: 'rgba(255,255,255,0.15)',
                                    transform: 'scale(1.05)'
                                }
                            }}
                        >
                            {num}
                        </Button>
                    ))}
                    
                    {/* Delete Button */}
                    <Button
                        variant="contained"
                        onClick={handleDelete}
                        disabled={pin.length === 0}
                        sx={{
                            width: 80,
                            height: 80,
                            borderRadius: 3,
                            bgcolor: 'rgba(255,255,255,0.05)',
                            color: 'text.secondary',
                            boxShadow: 'none',
                            '&:hover': { bgcolor: 'rgba(255,255,255,0.1)' },
                            '&.Mui-disabled': { bgcolor: 'rgba(255,255,255,0.02)', color: 'rgba(255,255,255,0.1)' }
                        }}
                    >
                        <Backspace />
                    </Button>

                    {/* Zero */}
                    <Button
                        variant="contained"
                        onClick={() => handleNumberClick('0')}
                        sx={{
                            width: 80,
                            height: 80,
                            borderRadius: 3,
                            bgcolor: 'rgba(255,255,255,0.08)',
                            fontSize: '28px',
                            fontWeight: 'bold',
                            color: 'white',
                            boxShadow: 'none',
                            '&:hover': { bgcolor: 'rgba(255,255,255,0.15)', transform: 'scale(1.05)' }
                        }}
                    >
                        0
                    </Button>

                    {/* Submit Button */}
                    <Button
                        variant="contained"
                        onClick={handleSubmit}
                        disabled={pin.length < 4 || loading}
                        sx={{
                            width: 80,
                            height: 80,
                            borderRadius: 3,
                            background: pin.length >= 4 ? 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)' : 'rgba(255,255,255,0.05)',
                            ...(pin.length >= 4 && { background: 'linear-gradient(90deg, #7048E8 0%, #4D329A 100%)' }),
                            color: 'white',
                            boxShadow: pin.length >= 4 ? '0 4px 15px rgba(112, 72, 232, 0.4)' : 'none',
                            '&:hover': { 
                                filter: 'brightness(1.1)',
                                transform: pin.length >= 4 ? 'scale(1.05)' : 'none'
                             },
                            '&.Mui-disabled': { background: 'rgba(255,255,255,0.05)' }
                        }}
                    >
                        {loading ? (
                            <CircularProgress size={28} color="inherit" />
                        ) : (
                            <ArrowForward fontSize="large" sx={{ color: pin.length >= 4 ? 'white' : 'rgba(255,255,255,0.2)' }} />
                        )}
                    </Button>
                </Box>
            </Box>

            {/* Bottom Footer Section */}
            <Box sx={{ 
                display: 'flex', 
                flexDirection: 'column',
                alignItems: 'center', 
                pb: 6 
            }}>
                <Typography variant="caption" color="text.secondary" fontWeight="bold" sx={{ mb: 2, letterSpacing: 1 }}>
                    OTHER LOGGED-IN STAFF
                </Typography>
                <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                    <AvatarGroup max={4} sx={{ '& .MuiAvatar-root': { width: 40, height: 40, borderColor: '#16171D' } }}>
                        <Avatar src="https://images.unsplash.com/photo-1544725176-7c40e5a71c5e?w=100&q=80" />
                        <Avatar src="https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&q=80" />
                    </AvatarGroup>
                    <IconButton sx={{ 
                        bgcolor: 'rgba(255,255,255,0.1)', 
                        color: 'white', 
                        width: 40, height: 40,
                        '&:hover': { bgcolor: 'rgba(255,255,255,0.2)' }
                    }}>
                        <Add fontSize="small" />
                    </IconButton>
                </Box>
            </Box>

        </Box>
    );
};

export default PosLoginPage;
