import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: {
      main: '#4f46e5', // Sleek Indigo
      light: '#818cf8',
      dark: '#3730a3',
      lighter: '#e0e7ff',
    },
    secondary: {
      main: '#d946ef', // Hot Fuchsia
      light: '#f472b6',
      dark: '#a21caf',
    },
    background: {
      default: '#f1f5f9', // Slate-100 background
      paper: '#ffffff', // White cards
    },
    text: {
      primary: '#0f172a', // Slate-900 for high readability
      secondary: '#475569', // Slate-600 for supporting text
    },
    success: {
      main: '#10b981', // Emerald green
      lighter: '#ecfdf5',
    },
    warning: {
      main: '#f59e0b', // Amber yellow
      lighter: '#fef3c7',
    },
    error: {
      main: '#ef4444', // Red-500
      lighter: '#fef2f2',
    },
    divider: '#e2e8f0', // Slate-200 dividers
  },
  typography: {
    fontFamily: '"Inter", "Plus Jakarta Sans", "Roboto", sans-serif',
    h1: { fontFamily: 'Outfit, sans-serif', fontWeight: 900 },
    h2: { fontFamily: 'Outfit, sans-serif', fontWeight: 800 },
    h3: { fontFamily: 'Outfit, sans-serif', fontWeight: 800 },
    h4: { fontFamily: 'Outfit, sans-serif', fontWeight: 750 },
    h5: { fontFamily: 'Outfit, sans-serif', fontWeight: 700 },
    h6: { fontFamily: 'Outfit, sans-serif', fontWeight: 700 },
    subtitle1: { fontFamily: 'Inter, sans-serif', fontWeight: 600 },
    subtitle2: { fontFamily: 'Inter, sans-serif', fontWeight: 600 },
    body1: { fontFamily: 'Inter, sans-serif', fontWeight: 400 },
    body2: { fontFamily: 'Inter, sans-serif', fontWeight: 500 },
    button: { fontFamily: 'Outfit, sans-serif', fontWeight: 700, textTransform: 'none' },
  },
  shape: {
    borderRadius: 16, // rounded corners for everything
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 20,
          border: '1px solid #f1f5f9',
          boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.05), 0 2px 4px -2px rgb(0 0 0 / 0.05)',
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          '&:hover': {
            transform: 'translateY(-2px)',
            boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.08), 0 4px 6px -4px rgb(0 0 0 / 0.08)',
            borderColor: '#e2e8f0',
          },
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          padding: '8px 20px',
          transition: 'all 0.2s ease-in-out',
          boxShadow: 'none',
          '&:hover': {
            boxShadow: '0 4px 12px rgba(79, 70, 229, 0.15)',
          },
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          '& fieldset': {
            borderWidth: 1.5,
          },
        },
      },
    },
  },
});

export default theme;
