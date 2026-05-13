/**
 * @file ProtectedRoute.tsx
 * @description 路由守衛組件，未登入自動導向登入頁 / Route guard component
 */
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@shared/store/authStore';
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
  const { hasHydrated, isAuthenticated, user } = useAuthStore();
  const location = useLocation();

  if (!hasHydrated) {
    return null;
  }

  if (!isAuthenticated) {
    return <Navigate to={buildRedirectTarget(redirectTo, location)} state={{ from: location }} replace />;
  }

  if (requiredRole && user?.role !== requiredRole) {
    return <Navigate to="/403" replace />;
  }

  return <>{children}</>;
}
