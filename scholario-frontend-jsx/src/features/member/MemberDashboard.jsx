import { useRestQuery, useRestMutation } from '../../hooks/useRest';
import React, { useState } from 'react';
import axios from 'axios';

import { 
  Box, 
  Grid, 
  Card, 
  CardContent, 
  Typography, 
  Button, 
  TextField, 
  InputAdornment,
  CircularProgress,
  Divider,
  Chip,
  ListItemText
} from '@mui/material';
import BookIcon from '@mui/icons-material/Book';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import HistoryIcon from '@mui/icons-material/History';
import SearchIcon from '@mui/icons-material/Search';

export const MemberDashboard = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const { data, loading, error } = useRestQuery('/api/lending/my-issued', 'getMyIssuedBooks');
  
  const [searchData, setSearchData] = useState(null);
  const [searching, setSearching] = useState(false);

  const handleSearch = async () => {
    if (searchTerm.trim()) {
      setSearching(true);
      try {
        const token = window.localStorage.getItem('scholario_token') || 'mock-jwt-token-123456';
        const response = await axios.get('/api/catalog', {
          headers: {
            'Authorization': token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json'
          },
          params: { title: searchTerm }
        });
        setSearchData({ searchBooks: response.data });
      } catch (err) {
        console.error('Search error:', err);
      } finally {
        setSearching(false);
      }
    }
  };

  const booksHeld = data?.getMyIssuedBooks.length || 0;
  
  const dueSoon = data?.getMyIssuedBooks.filter(issue => {
    const dueDate = new Date(issue.dueDate);
    const now = new Date();
    const diffTime = dueDate.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays >= 0 && diffDays <= 7;
  }).length || 0;

  const totalFines = data?.getMyIssuedBooks.reduce((acc, issue) => acc + (issue.penaltyAmount || 0), 0) || 0;
  const reservations = 0; // Planned feature

  const stats = [
    { label: 'Books Held', value: booksHeld, icon: BookIcon, color: '#4f46e5', bg: '#f5f3ff' },
    { label: 'Due Soon', value: dueSoon, icon: AccessTimeIcon, color: '#d97706', bg: '#fef3c7' },
    { label: 'Total Fines', value: `$${totalFines}`, icon: WarningAmberIcon, color: '#e11d48', bg: '#ffe4e6' },
    { label: 'Reservations', value: reservations, icon: BookmarkIcon, color: '#059669', bg: '#ecfdf5' },
  ];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4, pb: 6 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={900} color="text.primary" sx={{ letterSpacing: -1, fontFamily: 'Outfit, sans-serif' }}>
            My Library Activity
          </Typography>
          <Typography variant="body2" color="text.secondary" fontWeight={500}>
            Manage your academic resources and tracking.
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1.5, width: { xs: '100%', sm: 'auto' } }}>
          <Button
            variant="outlined"
            startIcon={<HistoryIcon />}
            sx={{ 
              borderRadius: 3, 
              fontWeight: 'bold', 
              py: 1, 
              px: 2,
              textTransform: 'none', 
              borderColor: 'divider',
              color: 'text.primary',
              '&:hover': {
                borderColor: 'primary.main',
                bgcolor: 'transparent'
              }
            }}
          >
            History
          </Button>
          <Button
            variant="contained"
            color="success"
            startIcon={<SearchIcon />}
            sx={{ 
              borderRadius: 3, 
              fontWeight: 'bold', 
              py: 1, 
              px: 2.5,
              textTransform: 'none', 
              boxShadow: '0 4px 10px rgba(16, 185, 129, 0.2)'
            }}
          >
            Find Books
          </Button>
        </Box>
      </Box>

      {/* Stats Cards */}
      <Grid container spacing={3}>
        {stats.map((stat, idx) => {
          const Icon = stat.icon;
          return (
            <Grid item xs={12} sm={6} md={3} key={idx}>
              <Card sx={{ borderRadius: 4, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none', transition: 'all 0.2s', '&:hover': { boxShadow: 2, borderColor: 'primary.lighter' } }}>
                <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Box sx={{ p: 1.25, borderRadius: 3, bgcolor: stat.bg, color: stat.color, display: 'flex' }}>
                      <Icon sx={{ fontSize: 24 }} />
                    </Box>
                  </Box>
                  <Box sx={{ mt: 2.5 }}>
                    <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
                      {stat.label}
                    </Typography>
                    <Typography variant="h4" fontWeight={900} color="text.primary" sx={{ mt: 0.5, letterSpacing: -0.5 }}>
                      {stat.value}
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          );
        })}
      </Grid>

      {/* Main Content Areas */}
      <Grid container spacing={4}>
        <Grid item xs={12} lg={8}>
          <Card sx={{ borderRadius: 4, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none' }}>
            <Box sx={{ px: 3, py: 2.5, display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid', borderColor: 'grey.50' }}>
              <Typography variant="subtitle2" fontWeight={800} sx={{ textTransform: 'uppercase', letterSpacing: 1 }}>
                Currently Borrowed
              </Typography>
              <Chip label={`${booksHeld} Units`} size="small" color="primary" sx={{ fontWeight: 800, borderRadius: 2, fontSize: 10 }} />
            </Box>
            
            <Box>
              {loading ? (
                <Box sx={{ p: 8, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
                  <CircularProgress size={32} />
                  <Typography variant="body2" color="text.secondary" fontWeight={500}>Loading your books...</Typography>
                </Box>
              ) : error ? (
                <Box sx={{ p: 8, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, color: 'error.main' }}>
                  <WarningAmberIcon sx={{ fontSize: 40 }} />
                  <Typography variant="body2" fontWeight="bold">Error loading library data</Typography>
                </Box>
              ) : data?.getMyIssuedBooks.map((issue) => {
                const dueDate = new Date(issue.dueDate);
                const now = new Date();
                const diffDays = Math.ceil((dueDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
                
                const isOverdue = issue.state.type === 'OVERDUE';
                const isDueSoon = diffDays <= 3 && diffDays >= 0;

                return (
                  <Box 
                    key={issue.id} 
                    sx={{ 
                      p: 3, 
                      display: 'flex', 
                      flexDirection: { xs: 'column', sm: 'row' }, 
                      justifyContent: 'space-between', 
                      alignItems: { xs: 'flex-start', sm: 'center' }, 
                      gap: 2,
                      borderBottom: '1px solid',
                      borderColor: 'grey.50',
                      '&:last-child': { borderBottom: 'none' },
                      transition: 'background-color 0.2s',
                      '&:hover': { bgcolor: 'grey.50' }
                    }}
                  >
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                      <Box sx={{ p: 1.5, borderRadius: 3, bgcolor: 'grey.50', border: '1px solid', borderColor: 'grey.100', color: 'primary.main', display: 'flex' }}>
                        <BookIcon />
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" fontWeight="bold" color="text.primary">
                          Resource ID: {issue.bookId}
                        </Typography>
                        <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ fontFamily: 'monospace', textTransform: 'uppercase' }}>
                          REF: {issue.id.substring(0, 8)}
                        </Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 1 }}>
                          <Chip 
                            label={isOverdue ? 'Overdue' : isDueSoon ? `Due in ${diffDays}d` : 'Secured'}
                            size="small"
                            color={isOverdue ? 'error' : isDueSoon ? 'warning' : 'success'}
                            sx={{ fontWeight: 'black', borderRadius: 1.5, fontSize: 9, height: 18 }}
                          />
                          <Typography variant="caption" color="text.secondary" fontWeight={600}>
                            Due {dueDate.toLocaleDateString()}
                          </Typography>
                        </Box>
                      </Box>
                    </Box>
                    <Box sx={{ display: 'flex', gap: 1, width: { xs: '100%', sm: 'auto' } }}>
                      <Button variant="outlined" size="small" sx={{ borderRadius: 2, textTransform: 'none', py: 0.75, px: 2, fontSize: 11, fontWeight: 'bold' }}>
                        Renew
                      </Button>
                      <Button variant="contained" size="small" sx={{ borderRadius: 2, textTransform: 'none', py: 0.75, px: 2, fontSize: 11, fontWeight: 'bold' }}>
                        Details
                      </Button>
                    </Box>
                  </Box>
                );
              })}
              {!loading && !error && data?.getMyIssuedBooks.length === 0 && (
                <Box sx={{ p: 6, textAlign: 'center' }}>
                  <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
                    No active loans found.
                  </Typography>
                </Box>
              )}
            </Box>
          </Card>
        </Grid>

        <Grid item xs={12} lg={4} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          {/* Quick Discovery */}
          <Card sx={{ borderRadius: 4, bgcolor: 'primary.main', color: 'white', border: 'none', p: 1 }}>
            <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
              <Typography variant="h6" fontWeight="bold" sx={{ mb: 2, tracking: -0.2 }}>
                Quick Discovery
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <TextField 
                  fullWidth
                  variant="outlined"
                  size="small"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  placeholder="Find academic resource..." 
                  slotProps={{
                    input: {
                      startAdornment: (
                        <InputAdornment position="start" sx={{ color: 'primary.light' }}>
                          <SearchIcon fontSize="small" />
                        </InputAdornment>
                      )
                    }
                  }}
                  sx={{
                    '& .MuiOutlinedInput-root': {
                      bgcolor: 'rgba(255,255,255,0.1)',
                      color: 'white',
                      borderRadius: 3,
                      '& fieldset': {
                        borderColor: 'rgba(255,255,255,0.2)',
                      },
                      '&:hover fieldset': {
                        borderColor: 'rgba(255,255,255,0.4)',
                      },
                      '&.Mui-focused fieldset': {
                        borderColor: 'white',
                      },
                    },
                    '& .MuiInputBase-input::placeholder': {
                      color: 'rgba(255, 255, 255, 0.7)',
                    }
                  }}
                />
                <Button 
                  fullWidth
                  onClick={handleSearch}
                  disabled={searching}
                  variant="contained"
                  sx={{ 
                    bgcolor: 'white', 
                    color: 'primary.main',
                    py: 1.25,
                    borderRadius: 3,
                    fontWeight: 'bold',
                    fontSize: 12,
                    textTransform: 'none',
                    '&:hover': {
                      bgcolor: 'grey.50'
                    },
                    '&:disabled': {
                      bgcolor: 'rgba(255,255,255,0.3)',
                      color: 'rgba(255,255,255,0.7)'
                    }
                  }}
                >
                  {searching ? 'Syncing...' : 'Execute Search'}
                </Button>

                {searchData?.searchBooks && (
                  <Box sx={{ mt: 1, borderTop: '1px solid rgba(255,255,255,0.1)', pt: 2 }}>
                    <Typography variant="caption" fontWeight="bold" sx={{ display: 'block', mb: 1, color: 'rgba(255,255,255,0.7)', textTransform: 'uppercase', letterSpacing: 1 }}>
                      Registry Matches
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                      {searchData.searchBooks.slice(0, 3).map((book) => (
                        <Box 
                          key={book.id} 
                          sx={{ 
                            p: 1.5, 
                            bgcolor: 'rgba(255,255,255,0.05)', 
                            borderRadius: 2.5, 
                            border: '1px solid rgba(255,255,255,0.1)',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center'
                          }}
                        >
                          <Box sx={{ minWidth: 0, pr: 1 }}>
                            <Typography variant="body2" fontWeight="bold" noWrap sx={{ fontSize: 12 }}>
                              {book.title}
                            </Typography>
                            <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.6)', fontFamily: 'monospace' }}>
                              {book.isbn}
                            </Typography>
                          </Box>
                          <Button size="small" sx={{ bgcolor: 'white', color: 'primary.main', fontSize: 9, fontWeight: 'bold', minWidth: 48, py: 0.5, borderRadius: 1.5, textTransform: 'none', '&:hover': { bgcolor: 'grey.100' } }}>
                            Hold
                          </Button>
                        </Box>
                      ))}
                      {searchData.searchBooks.length === 0 && (
                        <Typography variant="caption" sx={{ fontStyle: 'italic', color: 'rgba(255,255,255,0.6)' }}>
                          No nodes matched criteria.
                        </Typography>
                      )}
                    </Box>
                  </Box>
                )}
              </Box>
            </CardContent>
          </Card>

          {/* Payments & Dues */}
          <Card sx={{ borderRadius: 4, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none', transition: 'all 0.2s', '&:hover': { borderColor: 'success.lighter' } }}>
            <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
              <Box sx={{ p: 1.25, borderRadius: 3, bgcolor: '#ecfdf5', color: '#059669', display: 'inline-flex', mb: 2 }}>
                <WarningAmberIcon />
              </Box>
              <Typography variant="subtitle2" fontWeight="bold" color="text.primary">
                Payments & Dues
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, mb: 3 }}>
                Clear pending liabilities and view archival transaction history.
              </Typography>
              
              <Divider sx={{ mb: 2 }} />
              
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
                  Active Balance
                </Typography>
                <Typography variant="h5" fontWeight={900} color="text.primary">
                  ${totalFines.toFixed(2)}
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
