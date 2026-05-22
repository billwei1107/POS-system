import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface User {
    id: string;
    username: string;
    email?: string;
    role?: string;
}

interface AuthState {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    hasHydrated: boolean;
    setAuth: (user: User, token: string) => void;
    setHasHydrated: (hasHydrated: boolean) => void;
    logout: () => void;
}

const clearPosSession = () => {
    if (typeof window !== 'undefined') {
        window.localStorage.removeItem('pos-session');
    }
};

interface JwtPayload {
    exp?: number;
    role?: string;
}

// ========================================
// JWT 到期檢查 / JWT Expiration Check
// ========================================
const decodeBase64Url = (value: string) => {
    const decoder = typeof globalThis.atob === 'function' ? globalThis.atob : null;
    if (!decoder) return null;

    const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
    return decoder(padded);
};

export const getTokenExpirationMs = (token: string | null) => {
    if (!token) return null;

    const [, payloadSegment] = token.split('.');
    if (!payloadSegment) return null;

    try {
        const decodedPayload = decodeBase64Url(payloadSegment);
        if (!decodedPayload) return null;

        const payload = JSON.parse(decodedPayload) as JwtPayload;
        const expirationSeconds = Number(payload.exp);
        return Number.isFinite(expirationSeconds) ? expirationSeconds * 1000 : null;
    } catch {
        return null;
    }
};

export const getTokenRole = (token: string | null) => {
    if (!token) return null;

    const [, payloadSegment] = token.split('.');
    if (!payloadSegment) return null;

    try {
        const decodedPayload = decodeBase64Url(payloadSegment);
        if (!decodedPayload) return null;

        const payload = JSON.parse(decodedPayload) as JwtPayload;
        return typeof payload.role === 'string' && payload.role.trim() ? payload.role : null;
    } catch {
        return null;
    }
};

export const isTokenExpired = (token: string | null, nowMs = Date.now()) => {
    const expirationMs = getTokenExpirationMs(token);
    return expirationMs !== null && expirationMs <= nowMs;
};

/**
 * @file authStore.ts
 * @description 全局狀態管理 (Zustand) 用於認證 / Global auth state store
 * @description_en Stores auth identity, token and hydration status
 * @description_zh 保存登入身分、JWT token 與持久化 hydration 狀態
 */
export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            user: null,
            token: null,
            isAuthenticated: false,
            hasHydrated: false,
            setAuth: (user, token) => {
                if (isTokenExpired(token)) {
                    clearPosSession();
                    set({ user: null, token: null, isAuthenticated: false });
                    return;
                }
                set({ user, token, isAuthenticated: true });
            },
            setHasHydrated: (hasHydrated) => set({ hasHydrated }),
            logout: () => {
                clearPosSession();
                set({ user: null, token: null, isAuthenticated: false });
            },
        }),
        {
            name: 'auth-storage',
            partialize: (state) => ({
                user: state.user,
                token: state.token,
                isAuthenticated: state.isAuthenticated,
            }),
            onRehydrateStorage: () => (state) => {
                state?.setHasHydrated(true);
            },
            merge: (persistedState, currentState) => {
                const persisted = persistedState as Partial<AuthState> | undefined;
                const user = persisted?.user ?? null;
                const token = persisted?.token ?? null;
                const expired = isTokenExpired(token);

                if (expired) {
                    clearPosSession();
                }

                return {
                    ...currentState,
                    ...persisted,
                    user: expired ? null : user,
                    token: expired ? null : token,
                    isAuthenticated: Boolean(user && token && !expired),
                    hasHydrated: true,
                };
            },
        }
    )
);
