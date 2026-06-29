import { useRestQuery, useRestMutation } from '../../hooks/useRest';
import React, { useState, useEffect } from 'react';


import { 
  Box, 
  Typography, 
  Grid, 
  Card, 
  CardContent, 
  Avatar, 
  TextField, 
  Button, 
  CircularProgress,
  Divider,
  Chip
} from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import MailOutlinedIcon from '@mui/icons-material/MailOutlined';
import ShieldIcon from '@mui/icons-material/Shield';
import SaveIcon from '@mui/icons-material/Save';
import RefreshIcon from '@mui/icons-material/Refresh';
import SettingsIcon from '@mui/icons-material/Settings';
import LocalLibraryIcon from '@mui/icons-material/LocalLibrary';





export const LibrarianSettings = () => {
  const { data, loading, refetch } = useRestQuery('/api/member/profile', 'getMyProfile');
  const [updateProfile, { loading: updating }] = useRestMutation('/api/member/profile', 'PUT', 'updateProfile');

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');

  useEffect(() => {
    if (data?.getMyProfile) {
      setFullName(data.getMyProfile.fullName || '');
      setEmail(data.getMyProfile.email || '');
    }
  }, [data]);

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      await updateProfile({
        variables: {
          input: { fullName, email }
        }
      });
      alert('Librarian profile synchronized');
      refetch();
    } catch (err) {
      console.error(err);
      alert('Failed to synchronize registry');
    }
  };

  if (loading) {
    return (
      <Box sx={{ py: 10, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
        <CircularProgress size={40} />
        <Typography variant="body2" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
          Accessing secure archive...
        </Typography>
      </Box>
    );
  }

  const profile = data?.getMyProfile;

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4, pb: 6, maxWidth: 900, mx: 'auto' }}>
      <Box component="header">
        <Typography variant="h4" fontWeight={900} color="text.primary" sx={{ letterSpacing: -1, textTransform: 'uppercase', fontFamily: 'Outfit, sans-serif' }}>
          Registry Node Configuration
        </Typography>
        <Typography variant="body2" color="text.secondary" fontWeight={500}>
          Manage your professional identity and archive access settings
        </Typography>
      </Box>

      <Grid container spacing={4}>
        {/* Left Side Info Cards */}
        <Grid size={{ xs: 12, md: 4 }} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Card sx={{ borderRadius: 2, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none', textAlign: 'center', p: 1 }}>
            <CardContent sx={{ py: 4 }}>
              <Avatar 
                sx={{ 
                  width: 80, 
                  height: 80, 
                  mx: 'auto', 
                  mb: 2, 
                  bgcolor: 'success.main', 
                  boxShadow: '0 8px 24px rgba(16, 185, 129, 0.15)' 
                }}
              >
                <LocalLibraryIcon sx={{ fontSize: 40 }} />
              </Avatar>
              <Typography variant="h6" fontWeight="bold" color="text.primary" sx={{ lineHeight: 1.2 }}>
                {profile?.fullName}
              </Typography>
              <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ display: 'block', mt: 0.5, fontFamily: 'monospace', textTransform: 'uppercase' }}>
                NODE_ID: {profile?.id?.toString()?.substring(0, 8) || 'N/A'}
              </Typography>
              <Box sx={{ mt: 3, display: 'flex', justifyContent: 'center' }}>
                <Chip 
                  label="Librarian" 
                  size="small" 
                  color="success" 
                  sx={{ fontWeight: 'black', borderRadius: 2, fontSize: 10, py: 1.5 }} 
                />
              </Box>
            </CardContent>
          </Card>

          <Card sx={{ borderRadius: 2, bgcolor: 'grey.900', color: 'white', border: 'none', p: 1 }}>
            <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3, color: 'grey.400' }}>
                <ShieldIcon fontSize="small" />
                <Typography variant="caption" fontWeight="bold" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
                  Clearance Level
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="caption" fontWeight="bold" color="grey.500" sx={{ textTransform: 'uppercase' }}>
                    Circulation Auth
                  </Typography>
                  <Typography variant="caption" fontWeight="bold" color="success.main" sx={{ textTransform: 'uppercase' }}>
                    Granted
                  </Typography>
                </Box>
                <Divider sx={{ bgcolor: 'grey.800' }} />
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="caption" fontWeight="bold" color="grey.500" sx={{ textTransform: 'uppercase' }}>
                    Stock Management
                  </Typography>
                  <Typography variant="caption" fontWeight="bold" color="success.main" sx={{ textTransform: 'uppercase' }}>
                    Full Control
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Right Side Settings Form */}
        <Grid size={{ xs: 12, md: 8 }}>
          <Box component="form" onSubmit={handleUpdate} sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
            <Card sx={{ borderRadius: 2, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none' }}>
              <Box sx={{ px: 3, py: 2.5, display: 'flex', alignItems: 'center', gap: 1.5, borderBottom: '1px solid', borderColor: 'grey.50' }}>
                <SettingsIcon color="success" />
                <Typography variant="subtitle2" fontWeight={800} sx={{ textTransform: 'uppercase', letterSpacing: 1 }}>
                  Identity Synchronization
                </Typography>
              </Box>
              
              <CardContent sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 4 }}>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                  <Typography 
                    variant="caption" 
                    fontWeight="black" 
                    color="text.secondary" 
                    sx={{ textTransform: 'uppercase', letterSpacing: 1.5, pl: 0.5, display: 'flex', alignItems: 'center', gap: 1 }}
                  >
                    <PersonIcon sx={{ fontSize: 14 }} /> Professional Name
                  </Typography>
                  <TextField 
                    fullWidth
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    sx={{
                      '& .MuiOutlinedInput-root': {
                        borderRadius: 1.5,
                        bgcolor: 'grey.50',
                        fontWeight: 600,
                        fontSize: 14,
                        '& fieldset': { borderColor: 'transparent' },
                        '&:hover fieldset': { borderColor: 'divider' },
                        '&.Mui-focused': { bgcolor: 'white' },
                        '&.Mui-focused fieldset': { borderColor: 'success.main', borderWidth: 2 }
                      }
                    }}
                  />
                </Box>

                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                  <Typography 
                    variant="caption" 
                    fontWeight="black" 
                    color="text.secondary" 
                    sx={{ textTransform: 'uppercase', letterSpacing: 1.5, pl: 0.5, display: 'flex', alignItems: 'center', gap: 1 }}
                  >
                    <MailOutlinedIcon sx={{ fontSize: 14 }} /> Registry Email
                  </Typography>
                  <TextField 
                    fullWidth
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    sx={{
                      '& .MuiOutlinedInput-root': {
                        borderRadius: 1.5,
                        bgcolor: 'grey.50',
                        fontWeight: 600,
                        fontSize: 14,
                        '& fieldset': { borderColor: 'transparent' },
                        '&:hover fieldset': { borderColor: 'divider' },
                        '&.Mui-focused': { bgcolor: 'white' },
                        '&.Mui-focused fieldset': { borderColor: 'success.main', borderWidth: 2 }
                      }
                    }}
                  />
                </Box>

                <Divider sx={{ my: 1 }} />

                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Button 
                    variant="text" 
                    onClick={() => refetch()}
                    startIcon={<RefreshIcon sx={{ animation: loading ? 'spin 1s linear infinite' : 'none' }} />}
                    sx={{ 
                      color: 'text.secondary', 
                      textTransform: 'uppercase', 
                      fontSize: 11, 
                      fontWeight: 800, 
                      letterSpacing: 1,
                      '&:hover': { bgcolor: 'transparent', color: 'text.primary' }
                    }}
                  >
                    Reload Data
                  </Button>
                  <Button 
                    type="submit"
                    variant="contained"
                    color="success"
                    disabled={updating}
                    startIcon={updating ? <CircularProgress size={16} color="inherit" /> : <SaveIcon />}
                    sx={{ 
                      borderRadius: 2, 
                      textTransform: 'uppercase', 
                      fontWeight: 'bold', 
                      fontSize: 11, 
                      letterSpacing: 1,
                      py: 1.5,
                      px: 4,
                      boxShadow: '0 4px 14px rgba(16, 185, 129, 0.2)'
                    }}
                  >
                    Synchronize Node
                  </Button>
                </Box>
              </CardContent>
            </Card>

            {/* Security Box */}
            <Card sx={{ borderRadius: 2, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none', transition: 'all 0.2s', '&:hover': { borderColor: 'success.lighter' } }}>
              <CardContent sx={{ p: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 3 }}>
                <Box>
                  <Typography variant="subtitle2" fontWeight="bold" color="text.primary" sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                    <ShieldIcon color="action" /> Security Protocol
                  </Typography>
                  <Typography variant="body2" color="text.secondary" fontWeight={500}>
                    Reset your secure access pin and circulation credentials.
                  </Typography>
                </Box>
                <Button variant="outlined" color="success" sx={{ borderRadius: 2, textTransform: 'uppercase', fontWeight: 800, fontSize: 10, letterSpacing: 1, py: 1.25, px: 3, borderColor: 'divider', color: 'text.primary', '&:hover': { borderColor: 'success.main', bgcolor: 'transparent' } }}>
                  Reset Access
                </Button>
              </CardContent>
            </Card>
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
};
