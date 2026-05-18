/**
 * @file ThemeModeProvider.tsx
 * @description 主題模式狀態提供者 / Theme mode state provider
 * @description_en Provides persistent dark and light mode switching
 * @description_zh 提供可持久化的深色與淺色模式切換能力
 */
import React, { useCallback, useMemo, useState } from 'react';
import { ThemeProvider } from '@mui/material';
import { createPosTheme, THEME_MODE_STORAGE_KEY, type AppThemeMode } from './index';
import { ThemeModeContext } from './themeModeContext';

const readInitialThemeMode = (): AppThemeMode => {
  if (typeof window === 'undefined') return 'dark';

  const storedMode = window.localStorage.getItem(THEME_MODE_STORAGE_KEY);
  return storedMode === 'light' || storedMode === 'dark' ? storedMode : 'dark';
};

export const ThemeModeProvider = ({ children }: { children: React.ReactNode }) => {
  const [mode, setModeState] = useState<AppThemeMode>(readInitialThemeMode);
  const theme = useMemo(() => createPosTheme(mode), [mode]);

  const setMode = useCallback((nextMode: AppThemeMode) => {
    setModeState(nextMode);
    window.localStorage.setItem(THEME_MODE_STORAGE_KEY, nextMode);
  }, []);

  const toggleMode = useCallback(() => {
    setModeState((currentMode) => {
      const nextMode = currentMode === 'dark' ? 'light' : 'dark';
      window.localStorage.setItem(THEME_MODE_STORAGE_KEY, nextMode);
      return nextMode;
    });
  }, []);

  const contextValue = useMemo(
    () => ({ mode, setMode, toggleMode }),
    [mode, setMode, toggleMode],
  );

  return (
    <ThemeModeContext.Provider value={contextValue}>
      <ThemeProvider theme={theme}>{children}</ThemeProvider>
    </ThemeModeContext.Provider>
  );
};
