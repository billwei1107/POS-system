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

/**
 * @file authStore.ts
 * @description 全局狀態管理 (Zustand) 用於認證 / Global auth state store
 */
export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            user: null,
            token: null,
            isAuthenticated: false,
            hasHydrated: false,
            setAuth: (user, token) => set({ user, token, isAuthenticated: true }),
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

                return {
                    ...currentState,
                    ...persisted,
                    user,
                    token,
                    isAuthenticated: Boolean(user && token),
                    hasHydrated: true,
                };
            },
        }
    )
);
