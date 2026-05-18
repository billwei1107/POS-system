/**
 * @file index.ts
 * @description POS 主題設定 / POS theme configuration
 * @description_en Builds MUI themes for dark and light POS modes
 * @description_zh 建立 POS 深色與淺色模式的 MUI 主題
 */
import { createTheme } from '@mui/material/styles';

export type AppThemeMode = 'dark' | 'light';

export const THEME_MODE_STORAGE_KEY = 'pos-theme-mode';

export const createPosTheme = (mode: AppThemeMode) => createTheme({
  palette: {
    mode,
    primary: {
      main: '#7048E8', // Titanium POS sidebar active color
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#FF6D00', // PAY NOW button color
      contrastText: '#FFFFFF',
    },
    background: {
      default: mode === 'dark' ? '#1A1C23' : '#F4F6FB', // App background
      paper: mode === 'dark' ? '#252836' : '#FFFFFF', // Cards, sidebar, drawer
    },
    text: {
      primary: mode === 'dark' ? '#FFFFFF' : '#111827',
      secondary: mode === 'dark' ? '#B2B3BD' : '#5F6675',
    },
    success: {
      main: '#23C16B',
    },
    error: {
      main: '#FF5252',
    },
  },
  typography: {
    fontFamily: '"Inter", "Noto Sans TC", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontWeight: 700,
    },
    h2: {
      fontWeight: 700,
    },
    h3: {
      fontWeight: 600,
    },
    h4: {
      fontWeight: 600,
    },
    button: {
      textTransform: 'none',
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 12, // Default card border radius
  },
  components: {
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          borderRadius: '8px', // Button border radius
          padding: '10px 24px',
          minHeight: 48,
          touchAction: 'manipulation',
        },
        containedSecondary: {
          fontWeight: 'bold',
          fontSize: '1.1rem',
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          minWidth: 44,
          minHeight: 44,
          touchAction: 'manipulation',
        },
        sizeSmall: {
          minWidth: 44,
          minHeight: 44,
        },
      },
    },
    MuiTextField: {
      defaultProps: {
        size: 'medium',
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          minHeight: 48,
        },
        input: {
          paddingTop: 13,
          paddingBottom: 13,
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        select: {
          minHeight: 24,
          display: 'flex',
          alignItems: 'center',
        },
      },
    },
    MuiMenuItem: {
      styleOverrides: {
        root: {
          minHeight: 48,
        },
      },
    },
    MuiPaginationItem: {
      styleOverrides: {
        root: {
          minWidth: 44,
          height: 44,
          margin: '0 3px',
          touchAction: 'manipulation',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          boxShadow: mode === 'dark' ? '0 4px 6px rgba(0,0,0,0.1)' : '0 10px 24px rgba(17,24,39,0.08)',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundImage: 'none',
          borderRight: `1px solid ${mode === 'dark' ? 'rgba(255,255,255,0.05)' : 'rgba(17,24,39,0.08)'}`,
          borderLeft: `1px solid ${mode === 'dark' ? 'rgba(255,255,255,0.05)' : 'rgba(17,24,39,0.08)'}`,
        },
      },
    },
    MuiDialogActions: {
      styleOverrides: {
        root: {
          padding: '16px 24px 24px',
          gap: 8,
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderBottomColor: mode === 'dark' ? 'rgba(255,255,255,0.08)' : 'rgba(17,24,39,0.08)',
        },
        head: {
          fontWeight: 800,
          color: mode === 'dark' ? '#D8D9E2' : '#374151',
        },
      },
    },
  },
});

export const posTheme = createPosTheme('dark');
