import React from 'react';
import { useAuth } from './AuthContext';
import { gql } from '@apollo/client';
import { useQuery } from '@apollo/client/react';
import { Navigate } from 'react-router-dom';
import { 
  Box, 
  Card, 
  CardContent, 
  Typography, 
  Button, 
  CircularProgress,
  List,
  ListItem,
  ListItemIcon,
  ListItemText
} from '@mui/material';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import LogoutIcon from '@mui/icons-material/Logout';
import ArrowRightIcon from '@mui/icons-material/ArrowRight';

const GET_MY_PROFILE = gql`
  query GetMyProfile {
    getMyProfile {
      id
      username
      email
      fullName
      roles
    }
  }
`;

export const UnassignedPage = () => {
  const { logout, username, role } = useAuth();
  
  const { loading } = useQuery(GET_MY_PROFILE, {
    fetchPolicy: 'network-only',
    onCompleted: (data) => {
      console.log('[Auth] User profile synced with backend:', data?.getMyProfile?.username);
    },
    onError: (error) => {
      console.error('[Auth] Profile sync failed:', error);
    }
  });

  // If the user already has a functional role, redirect them back to the root
  if (role && role !== 'UNASSIGNED') {
    return <Navigate to="/" replace />;
  }

  return (
    <Box 
      sx={{ 
        minHeight: '100vh', 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center', 
        bgcolor: 'grey.50',
        p: 3 
      }}
    >
      <Card sx={{ maxWidth: 450, w: '100%', borderRadius: 4, boxShadow: 3, p: 2 }}>
        <CardContent sx={{ textAlign: 'center' }}>
          <Box 
            sx={{ 
              display: 'inline-flex', 
              p: 2, 
              bgcolor: 'amber.50', 
              borderRadius: '50%', 
              color: 'warning.main',
              mb: 3 
            }}
          >
            {loading ? (
              <CircularProgress color="inherit" size={48} />
            ) : (
              <WarningAmberIcon sx={{ fontSize: 48 }} />
            )}
          </Box>
          
          <Typography variant="h5" fontWeight="bold" color="text.primary" gutterBottom>
            {loading ? 'Synchronizing Account...' : 'Account Unassigned'}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
            Hello <strong>{username}</strong>, your account has been authenticated, but you haven't been assigned a functional role yet.
          </Typography>
          
          <Box 
            sx={{ 
              bgcolor: 'amber.50', 
              border: '1px solid',
              borderColor: 'amber.100',
              borderRadius: 3, 
              p: 2.5, 
              textAlign: 'left',
              mb: 4 
            }}
          >
            <Typography variant="subtitle2" color="warning.dark" fontWeight="bold" sx={{ mb: 1 }}>
              What should I do?
            </Typography>
            <List dense disablePadding>
              {[
                'Your account is being registered in our system.',
                'Contact your system administrator for role assignment.',
                'Request a role (Faculty, Student, or Librarian).'
              ].map((text, idx) => (
                <ListItem key={idx} disableGutters sx={{ alignItems: 'flex-start', py: 0.5 }}>
                  <ListItemIcon sx={{ minWidth: 24, mt: 0.2, color: 'warning.main' }}>
                    <ArrowRightIcon fontSize="small" />
                  </ListItemIcon>
                  <ListItemText 
                    primary={text} 
                    primaryTypographyProps={{ variant: 'body2', color: 'warning.dark', fontWeight: 500 }} 
                  />
                </ListItem>
              ))}
            </List>
          </Box>

          <Button
            variant="contained"
            color="inherit"
            fullWidth
            onClick={logout}
            startIcon={<LogoutIcon />}
            sx={{ 
              py: 1.5, 
              borderRadius: 3, 
              bgcolor: 'grey.900', 
              color: 'white',
              '&:hover': {
                bgcolor: 'grey.800'
              },
              textTransform: 'none',
              fontWeight: 'bold'
            }}
          >
            Sign Out
          </Button>
        </CardContent>
      </Card>
    </Box>
  );
};
