/**
 * @file ProtectedRoute.tsx
 * @description 路由守衛組件，未登入自動導向登入頁 / Route guard component
 * @description_en Redirects unauthenticated or expired sessions to login
 * @description_zh 將未登入或 JWT 已過期的工作階段導回登入頁
 */
import { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { isTokenExpired, useAuthStore } from '@shared/store/authStore';
import type { ReactNode } from 'react';

interface ProtectedRouteProps {
  children: ReactNode;
  requiredRole?: string;
  redirectTo?: string;
}

const buildRedirectTarget = (redirectTo: string, location: ReturnType<typeof useLocation>) => {
  const currentPath = `${location.pathname}${location.search}${location.hash}`;
  const separator = redirectTo.includes('?') ? '&' : '?';
  return `${redirectTo}${separator}redirect=${encodeURIComponent(currentPath)}`;
};

export function ProtectedRoute({
  children,
  requiredRole,
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

  if (requiredRole && user?.role !== requiredRole) {
    return <Navigate to="/403" replace />;
  }

  return <>{children}</>;
}
