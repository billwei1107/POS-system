import { createTheme } from '@mui/material/styles';

export const posTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#7048E8', // Titanium POS sidebar active color
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#FF6D00', // PAY NOW button color
      contrastText: '#FFFFFF',
    },
    background: {
      default: '#1A1C23', // App background
      paper: '#252836', // Cards, sidebar, drawer
    },
    text: {
      primary: '#FFFFFF',
      secondary: '#B2B3BD',
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
      styleOverrides: {
        root: {
          borderRadius: '8px', // Button border radius
          padding: '10px 24px',
        },
        containedSecondary: {
          fontWeight: 'bold',
          fontSize: '1.1rem',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundImage: 'none',
          borderRight: '1px solid rgba(255,255,255,0.05)',
          borderLeft: '1px solid rgba(255,255,255,0.05)',
        },
      },
    },
  },
});
