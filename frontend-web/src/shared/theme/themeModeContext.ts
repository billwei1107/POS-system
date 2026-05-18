/**
 * @file themeModeContext.ts
 * @description 主題模式 Context / Theme mode context
 * @description_en Shares dark and light mode state across the app
 * @description_zh 在應用程式內共享深色與淺色模式狀態
 */
import { createContext, useContext } from 'react';
import type { AppThemeMode } from './index';

export interface ThemeModeContextValue {
  mode: AppThemeMode;
  setMode: (mode: AppThemeMode) => void;
  toggleMode: () => void;
}

export const ThemeModeContext = createContext<ThemeModeContextValue>({
  mode: 'dark',
  setMode: () => undefined,
  toggleMode: () => undefined,
});

export const useThemeMode = () => useContext(ThemeModeContext);
