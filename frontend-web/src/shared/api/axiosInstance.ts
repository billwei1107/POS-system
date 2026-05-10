import axios from 'axios';
import { useAuthStore } from '../store/authStore';

/**
 * @file axiosInstance.ts
 * @description API 攔截器設定 / Axios instance with interceptors
 */
const axiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 10000,
});

const isPosAuthRequest = (url?: string) => url?.includes('/v1/pos/auth/') ?? false;

axiosInstance.interceptors.request.use(
    (config) => {
        const token = useAuthStore.getState().token;
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

axiosInstance.interceptors.response.use(
    (response) => {
        const body = response.data;
        if (body && typeof body === 'object' && 'code' in body && !('success' in body)) {
            const code = Number((body as { code: number }).code);
            return {
                ...body,
                success: code >= 200 && code < 300,
            };
        }
        return body;
    },
    (error) => {
        const requestUrl = typeof error.config?.url === 'string' ? error.config.url : undefined;
        if (error.response && error.response.status === 401 && !isPosAuthRequest(requestUrl)) {
            useAuthStore.getState().logout();
            window.location.href = window.location.pathname.startsWith('/pos') ? '/pos/login' : '/login';
        }
        return Promise.reject(error);
    }
);

export default axiosInstance;
