/**
 * @file ProtectedRoute.tsx
 * @description 路由守衛組件，未登入自動導向登入頁 / Route guard component
 * @description_en Redirects unauthenticated or expired sessions to login
 * @description_zh 將未登入或 JWT 已過期的工作階段導回登入頁
 */
import { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { getTokenRole, isTokenExpired, useAuthStore } from '@shared/store/authStore';
import type { ReactNode } from 'react';

interface ProtectedRouteProps {
  children: ReactNode;
  requiredRole?: string;
  allowedRoles?: string[];
  redirectTo?: string;
}

const buildRedirectTarget = (redirectTo: string, location: ReturnType<typeof useLocation>) => {
  const currentPath = `${location.pathname}${location.search}${location.hash}`;
  const separator = redirectTo.includes('?') ? '&' : '?';
  return `${redirectTo}${separator}redirect=${encodeURIComponent(currentPath)}`;
};

const buildPermissionRedirectTarget = (redirectTo: string, location: ReturnType<typeof useLocation>) => {
  const baseTarget = buildRedirectTarget(redirectTo, location);
  const separator = baseTarget.includes('?') ? '&' : '?';
  return `${baseTarget}${separator}reason=admin-permission`;
};

export function ProtectedRoute({
  children,
  requiredRole,
  allowedRoles,
  redirectTo = '/login',
}: ProtectedRouteProps) {
  const { hasHydrated, isAuthenticated, logout, token, user } = useAuthStore();
  const location = useLocation();
  const tokenExpired = hasHydrated && isAuthenticated && isTokenExpired(token);

  useEffect(() => {
    if (tokenExpired) {
      logout();
    }
  }, [logout, tokenExpired]);

  if (!hasHydrated) {
    return null;
  }

  if (!isAuthenticated || tokenExpired) {
    return <Navigate to={buildRedirectTarget(redirectTo, location)} state={{ from: location }} replace />;
  }

  const effectiveRole = user?.role ?? getTokenRole(token);
  const roleAllowList = allowedRoles ?? (requiredRole ? [requiredRole] : []);
  if (roleAllowList.length > 0 && (!effectiveRole || !roleAllowList.includes(effectiveRole))) {
    return <Navigate to={buildPermissionRedirectTarget(redirectTo, location)} state={{ from: location }} replace />;
  }

  return <>{children}</>;
}
