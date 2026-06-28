import { useRestQuery, useRestMutation } from '../../hooks/useRest';
import React from 'react';

import { 
  Box, 
  Typography, 
  Grid, 
  Card, 
  CardContent,
  CircularProgress
} from '@mui/material';
import SchoolIcon from '@mui/icons-material/School';
import BookIcon from '@mui/icons-material/Book';

export const MemberCourses = () => {
  const { data: profileData } = useRestQuery('/api/member/profile', 'getMyProfile');
  
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      <Box component="header">
        <Typography variant="h4" fontWeight={900} color="text.primary" sx={{ letterSpacing: -1, textTransform: 'uppercase', fontFamily: 'Outfit, sans-serif' }}>
          My Academic Modules
        </Typography>
        <Typography variant="body2" color="text.secondary" fontWeight={500}>
          Tracking enrolled courses and associated learning materials
        </Typography>
      </Box>

      <Grid container spacing={4}>
        <Grid item xs={12} md={6}>
          <Card 
            sx={{ 
              borderRadius: 5, 
              border: '1px solid', 
              borderColor: 'grey.100', 
              boxShadow: 'none', 
              display: 'flex', 
              flexDirection: 'column', 
              justifyContent: 'center', 
              alignItems: 'center', 
              textAlign: 'center',
              py: 8,
              px: 4
            }}
          >
            <CardContent>
              <Box 
                sx={{ 
                  p: 3, 
                  borderRadius: 5, 
                  bgcolor: 'primary.lighter', 
                  color: 'primary.main', 
                  display: 'inline-flex',
                  mb: 3
                }}
              >
                <SchoolIcon sx={{ fontSize: 40 }} />
              </Box>
              <Typography variant="h6" fontWeight="bold" color="text.primary" sx={{ textTransform: 'uppercase', tracking: -0.2 }}>
                Enrollment Matrix Pending
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1.5, maxWidth: 300, mx: 'auto', fontWeight: 500 }}>
                The digital course registry is being synchronized with your member ID.
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Box>
            <Typography 
              variant="caption" 
              fontWeight="black" 
              color="text.secondary" 
              sx={{ textTransform: 'uppercase', letterSpacing: 1.5, display: 'block', mb: 1, pl: 0.5 }}
            >
              Digital Syllabus Feed
            </Typography>
            <Card sx={{ bgcolor: 'grey.900', color: 'white', borderRadius: 5, p: 3, boxShadow: 'none' }}>
              <CardContent sx={{ p: 0, '&:last-child': { pb: 0 } }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                  <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: 'success.main' }} />
                  <Typography variant="caption" fontWeight="bold" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
                    Global Telemetry Active
                  </Typography>
                </Box>
                <Typography variant="body2" sx={{ fontFamily: 'monospace', color: 'grey.400', lineHeight: 1.6 }}>
                  [SYSTEM] Scanning academic nodes...<br/>
                  [SYSTEM] Verifying enrollment tokens...<br/>
                  [STABLE] Connection established.
                </Typography>
              </CardContent>
            </Card>
          </Box>
          
          <Card sx={{ borderRadius: 5, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none', p: 1 }}>
            <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, color: 'primary.main' }}>
                <BookIcon fontSize="small" />
                <Typography variant="subtitle2" fontWeight="bold" color="text.primary" sx={{ textTransform: 'uppercase', letterSpacing: 1 }}>
                  Material Archive
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic', fontWeight: 500 }}>
                "Access to course materials will be enabled upon module authorization."
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
