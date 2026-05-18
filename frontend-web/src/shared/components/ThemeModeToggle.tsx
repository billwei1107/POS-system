/**
 * @file ThemeModeToggle.tsx
 * @description 主題模式切換按鈕 / Theme mode toggle button
 * @description_en Reusable icon button for switching between dark and light modes
 * @description_zh 可重用的深色與淺色模式切換圖示按鈕
 */
import { IconButton, Tooltip } from '@mui/material';
import { DarkMode, LightMode } from '@mui/icons-material';
import { useThemeMode } from '@shared/theme/themeModeContext';

export const ThemeModeToggle = () => {
  const { mode, toggleMode } = useThemeMode();
  const isDarkMode = mode === 'dark';
  const label = isDarkMode ? '切換淺色模式' : '切換深色模式';

  return (
    <Tooltip title={label}>
      <IconButton
        aria-label={label}
        onClick={toggleMode}
        color="inherit"
        sx={{
          bgcolor: isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(17,24,39,0.06)',
          border: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(17,24,39,0.08)',
          '&:hover': {
            bgcolor: isDarkMode ? 'rgba(255,255,255,0.1)' : 'rgba(17,24,39,0.1)',
          },
        }}
      >
        {isDarkMode ? <LightMode /> : <DarkMode />}
      </IconButton>
    </Tooltip>
  );
};
