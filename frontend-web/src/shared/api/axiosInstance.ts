import axios from 'axios';
import { useAuthStore } from '../store/authStore';

/**
 * @file axiosInstance.ts
 * @description API 攔截器設定 / Axios instance with interceptors
 * @description_en Normalizes API responses and clears expired auth sessions
 * @description_zh 正規化 API 回應並在認證失效時清除登入工作階段
 */
const axiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 10000,
});

const isPosAuthRequest = (url?: string) => url?.includes('/v1/pos/auth/') ?? false;

interface ApiResponseBody {
    code?: number;
    message?: string;
    success?: boolean;
}

const readApiResponseBody = (body: unknown): ApiResponseBody | null => {
    if (!body || typeof body !== 'object') return null;
    return body as ApiResponseBody;
};

// ========================================
// 認證失效處理 / Unauthorized Session Handling
// ========================================
const rejectUnauthorizedSession = (message?: string) => {
    useAuthStore.getState().logout();
    return Promise.reject(new Error(message || '登入已逾時，請重新登入。'));
};

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
        const apiBody = readApiResponseBody(body);
        const requestUrl = typeof response.config?.url === 'string' ? response.config.url : undefined;

        if (apiBody && 'code' in apiBody && Number(apiBody.code) === 401 && !isPosAuthRequest(requestUrl)) {
            return rejectUnauthorizedSession(apiBody.message);
        }

        if (apiBody && 'code' in apiBody && !('success' in apiBody)) {
            const code = Number(apiBody.code);
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
            return rejectUnauthorizedSession(error.response.data?.message);
        }
        return Promise.reject(error);
    }
);

export default axiosInstance;
