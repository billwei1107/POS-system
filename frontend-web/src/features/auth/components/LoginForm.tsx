import { useState } from 'react';
import { Box, TextField, Button, CircularProgress, Alert } from '@mui/material';
import { useLogin } from '../hooks/useLogin';

/**
 * @file LoginForm.tsx
 * @description 登入表單組件 / Login form component
 */
export const LoginForm = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const { login, loading, error } = useLogin();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (username && password) {
            await login({ username, password });
        }
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 2 }}>
            {error && <Alert severity="error">{error}</Alert>}

            <TextField
                label="使用者帳號"
                variant="outlined"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                fullWidth
                required
            />

            <TextField
                label="登入密碼"
                type="password"
                variant="outlined"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                fullWidth
                required
            />

            <Button
                type="submit"
                variant="contained"
                size="large"
                disabled={loading}
                sx={{
                    mt: 2,
                    height: 50,
                    fontSize: '1.1rem',
                    fontWeight: 'bold',
                    backgroundColor: '#0d9488',
                    transition: 'all 0.2s',
                    '&:hover': {
                        backgroundColor: '#0f766e',
                        transform: 'translateY(-2px)',
                        boxShadow: 4
                    }
                }}
            >
                {loading ? <CircularProgress size={24} color="inherit" /> : '登入系統'}
            </Button>
        </Box>
    );
};
