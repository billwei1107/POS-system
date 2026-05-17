/**
 * @file PosSessionRoute.tsx
 * @description POS 工作階段路由守衛 / POS session route guard
 * @description_en Redirects stale POS terminal sessions back to PIN login
 * @description_zh 當 POS 終端工作階段遺失時，將畫面導回 PIN 登入
 */
import { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import type { ReactNode } from 'react';
import { hasActivePosSession } from '@features/pos-orders/posSession';
import { useAuthStore } from '@shared/store/authStore';

interface PosSessionRouteProps {
  children: ReactNode;
}

const POS_LOGIN_PATH = '/pos/login';

const buildPosLoginTarget = (location: ReturnType<typeof useLocation>) => {
  const currentPath = `${location.pathname}${location.search}${location.hash}`;
  return `${POS_LOGIN_PATH}?redirect=${encodeURIComponent(currentPath)}`;
};

export function PosSessionRoute({ children }: PosSessionRouteProps) {
  const location = useLocation();
  const logout = useAuthStore((state) => state.logout);
  const sessionReady = hasActivePosSession();

  useEffect(() => {
    if (!sessionReady) {
      logout();
    }
  }, [logout, sessionReady]);

  if (!sessionReady) {
    return <Navigate to={buildPosLoginTarget(location)} state={{ from: location }} replace />;
  }

  return <>{children}</>;
}
