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
  Divider
} from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import MailOutlinedIcon from '@mui/icons-material/MailOutlined';
import ShieldIcon from '@mui/icons-material/Shield';
import SaveIcon from '@mui/icons-material/Save';
import RefreshIcon from '@mui/icons-material/Refresh';
import SettingsIcon from '@mui/icons-material/Settings';
import SchoolIcon from '@mui/icons-material/School';





export const FacultySettings = () => {
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
      alert('Profile updated successfully');
      refetch();
    } catch (err) {
      console.error(err);
      alert('Failed to update profile');
    }
  };

  if (loading) {
    return (
      <Box sx={{ py: 10, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
        <CircularProgress size={40} />
        <Typography variant="body2" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
          Accessing Secure Profile...
        </Typography>
      </Box>
    );
  }

  const profile = data?.getMyProfile;

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4, pb: 6, maxWidth: 900, mx: 'auto' }}>
      <Box component="header">
        <Typography variant="h4" fontWeight={900} color="text.primary" sx={{ letterSpacing: -1, textTransform: 'uppercase', fontFamily: 'Outfit, sans-serif' }}>
          Faculty Configuration
        </Typography>
        <Typography variant="body2" color="text.secondary" fontWeight={500}>
          Manage your academic identity and portal settings
        </Typography>
      </Box>

      <Grid container spacing={4}>
        {/* Left Side Info Cards */}
        <Grid item xs={12} md={4} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Card sx={{ borderRadius: 5, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none', textAlign: 'center', p: 1 }}>
            <CardContent sx={{ py: 4 }}>
              <Avatar 
                sx={{ 
                  width: 80, 
                  height: 80, 
                  mx: 'auto', 
                  mb: 2, 
                  bgcolor: 'primary.main', 
                  boxShadow: '0 8px 24px rgba(99, 102, 241, 0.15)' 
                }}
              >
                <PersonIcon sx={{ fontSize: 40 }} />
              </Avatar>
              <Typography variant="h6" fontWeight="bold" color="text.primary" sx={{ lineHeight: 1.2 }}>
                {profile?.fullName}
              </Typography>
              <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ display: 'block', mt: 0.5, textTransform: 'lowercase' }}>
                @{profile?.username}
              </Typography>
              
              <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 3.5, border: '1px solid', borderColor: 'grey.100', textAlign: 'left' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1, color: 'text.disabled' }}>
                  <SchoolIcon sx={{ fontSize: 14 }} />
                  <Typography variant="caption" fontWeight="black" sx={{ textTransform: 'uppercase', letterSpacing: 1.2, fontSize: 9 }}>
                    Department
                  </Typography>
                </Box>
                <Typography variant="body2" fontWeight="bold" color="text.primary" sx={{ textTransform: 'uppercase' }}>
                  {profile?.department?.name || 'Unassigned'}
                </Typography>
                <Typography variant="caption" color="text.disabled" sx={{ display: 'block', fontFamily: 'monospace', mt: 0.25 }}>
                  {profile?.department?.code || 'N/A'}
                </Typography>
              </Box>
            </CardContent>
          </Card>

          <Card sx={{ borderRadius: 5, bgcolor: 'grey.900', color: 'white', border: 'none', p: 1 }}>
            <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3, color: 'grey.400' }}>
                <ShieldIcon fontSize="small" />
                <Typography variant="caption" fontWeight="bold" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
                  Portal Status
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="caption" fontWeight="bold" color="grey.500" sx={{ textTransform: 'uppercase' }}>
                    Auth Level
                  </Typography>
                  <Typography variant="caption" fontWeight="bold" color="primary.light" sx={{ textTransform: 'uppercase' }}>
                    Faculty
                  </Typography>
                </Box>
                <Divider sx={{ bgcolor: 'grey.800' }} />
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="caption" fontWeight="bold" color="grey.500" sx={{ textTransform: 'uppercase' }}>
                    Global Access
                  </Typography>
                  <Typography variant="caption" fontWeight="bold" color="success.main" sx={{ textTransform: 'uppercase' }}>
                    Enabled
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Right Side Settings Form */}
        <Grid item xs={12} md={8}>
          <Box component="form" onSubmit={handleUpdate} sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
            <Card sx={{ borderRadius: 5, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none' }}>
              <Box sx={{ px: 3, py: 2.5, display: 'flex', alignItems: 'center', gap: 1.5, borderBottom: '1px solid', borderColor: 'grey.50' }}>
                <SettingsIcon color="action" />
                <Typography variant="subtitle2" fontWeight={800} sx={{ textTransform: 'uppercase', letterSpacing: 1 }}>
                  Identity Settings
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
                    <PersonIcon sx={{ fontSize: 14 }} /> Full Identity Name
                  </Typography>
                  <TextField 
                    fullWidth
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    sx={{
                      '& .MuiOutlinedInput-root': {
                        borderRadius: 3.5,
                        bgcolor: 'grey.50',
                        fontWeight: 600,
                        fontSize: 14,
                        '& fieldset': { borderColor: 'transparent' },
                        '&:hover fieldset': { borderColor: 'divider' },
                        '&.Mui-focused': { bgcolor: 'white' },
                        '&.Mui-focused fieldset': { borderColor: 'primary.main', borderWidth: 2 }
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
                        borderRadius: 3.5,
                        bgcolor: 'grey.50',
                        fontWeight: 600,
                        fontSize: 14,
                        '& fieldset': { borderColor: 'transparent' },
                        '&:hover fieldset': { borderColor: 'divider' },
                        '&.Mui-focused': { bgcolor: 'white' },
                        '&.Mui-focused fieldset': { borderColor: 'primary.main', borderWidth: 2 }
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
                    disabled={updating}
                    startIcon={updating ? <CircularProgress size={16} color="inherit" /> : <SaveIcon />}
                    sx={{ 
                      borderRadius: 3.5, 
                      textTransform: 'uppercase', 
                      fontWeight: 'bold', 
                      fontSize: 11, 
                      letterSpacing: 1,
                      py: 1.5,
                      px: 4,
                      boxShadow: '0 4px 14px rgba(99, 102, 241, 0.2)'
                    }}
                  >
                    Synchronize Changes
                  </Button>
                </Box>
              </CardContent>
            </Card>

            {/* Security Box */}
            <Card sx={{ borderRadius: 5, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none' }}>
              <CardContent sx={{ p: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 3 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box sx={{ p: 1, borderRadius: 2, bgcolor: 'grey.50', color: 'text.disabled', display: 'flex' }}>
                    <ShieldIcon />
                  </Box>
                  <Box>
                    <Typography variant="subtitle2" fontWeight="bold" color="text.primary" sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                      Infrastructure Security
                    </Typography>
                    <Typography variant="body2" color="text.secondary" fontWeight={500}>
                      Reset your secure access credentials.
                    </Typography>
                  </Box>
                </Box>
                <Button variant="outlined" sx={{ borderRadius: 3, textTransform: 'uppercase', fontWeight: 800, fontSize: 10, letterSpacing: 1, py: 1.25, px: 3, borderColor: 'divider', color: 'text.primary', '&:hover': { borderColor: 'primary.main', bgcolor: 'transparent' } }}>
                  Change Password
                </Button>
              </CardContent>
            </Card>
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
};
