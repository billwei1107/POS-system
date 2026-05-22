import { useState } from 'react';
import { isAxiosError } from 'axios';
import { loginApi } from '../api/authApi';
import type { LoginRequest } from '../types';
import { useAuthStore } from '../../../shared/store/authStore';
import { useLocation, useNavigate } from 'react-router-dom';

const DEFAULT_REDIRECT_PATH = '/pos/register';

const resolveRedirectPath = (search: string) => {
    const redirect = new URLSearchParams(search).get('redirect');
    if (!redirect || !redirect.startsWith('/') || redirect.startsWith('//')) {
        return DEFAULT_REDIRECT_PATH;
    }
    return redirect;
};

export const useLogin = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const setAuth = useAuthStore((state) => state.setAuth);
    const navigate = useNavigate();
    const location = useLocation();

    const login = async (data: LoginRequest) => {
        setLoading(true);
        setError(null);
        try {
            const response = await loginApi(data);
            setAuth({ id: response.userId, username: response.username, role: response.role }, response.token);
            navigate(resolveRedirectPath(location.search), { replace: true });
        } catch (err: unknown) {
            const message = isAxiosError<{ message?: string }>(err)
                ? err.response?.data?.message
                : undefined;
            setError(message || 'Login failed');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return { login, loading, error };
};
