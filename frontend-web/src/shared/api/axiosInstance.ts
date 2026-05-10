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
        if (error.response && error.response.status === 401) {
            useAuthStore.getState().logout();
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default axiosInstance;
