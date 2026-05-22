import { useState } from 'react';
import { Box, TextField, Button, CircularProgress, Alert } from '@mui/material';
import { APP_BRAND, LOGIN_COPY } from '@shared/config/appBrand';
import { useLogin } from '../hooks/useLogin';

const loginTextFieldSx = {
    '& .MuiInputLabel-root': {
        color: '#64748b',
        fontWeight: 700,
    },
    '& .MuiInputLabel-root.Mui-focused': {
        color: APP_BRAND.colors.accent,
    },
    '& .MuiOutlinedInput-root': {
        color: '#1e293b',
        backgroundColor: '#ffffff',
        '& fieldset': {
            borderColor: '#cbd5e1',
        },
        '&:hover fieldset': {
            borderColor: '#94a3b8',
        },
        '&.Mui-focused fieldset': {
            borderColor: APP_BRAND.colors.accent,
        },
    },
    '& .MuiOutlinedInput-input': {
        color: '#1e293b',
        WebkitTextFillColor: '#1e293b',
    },
};

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
                label={LOGIN_COPY.usernameLabel}
                variant="outlined"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                fullWidth
                required
                sx={loginTextFieldSx}
            />

            <TextField
                label={LOGIN_COPY.passwordLabel}
                type="password"
                variant="outlined"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                fullWidth
                required
                sx={loginTextFieldSx}
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
                    backgroundColor: APP_BRAND.colors.accent,
                    transition: 'all 0.2s',
                    '&:hover': {
                        backgroundColor: APP_BRAND.colors.accentHover,
                        transform: 'translateY(-2px)',
                        boxShadow: 4
                    }
                }}
            >
                {loading ? <CircularProgress size={24} color="inherit" /> : LOGIN_COPY.submitLabel}
            </Button>
        </Box>
    );
};
