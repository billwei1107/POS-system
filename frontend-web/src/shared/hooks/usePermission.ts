/**
 * @file usePermission.ts
 * @description 權限檢查 Hook / Permission check hook
 */
import { useAuthStore } from '@shared/store/authStore';

export function usePermission() {
  const user = useAuthStore(state => state.user);

  const hasRole = (role: string): boolean => {
    return user?.role === role;
  };

  const hasAnyRole = (...roles: string[]): boolean => {
    return roles.some(r => user?.role === r);
  };

  const isAuthenticated = useAuthStore(state => state.isAuthenticated);

  return { hasRole, hasAnyRole, isAuthenticated, user };
}
