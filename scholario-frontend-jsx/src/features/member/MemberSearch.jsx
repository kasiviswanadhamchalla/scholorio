import { useRestQuery, useRestMutation } from '../../hooks/useRest';
import React, { useState } from 'react';
import axios from 'axios';

import { 
  Box, 
  Typography, 
  Grid, 
  Card, 
  CardContent, 
  Button, 
  TextField, 
  InputAdornment, 
  CircularProgress,
  Chip,
  IconButton
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import BookIcon from '@mui/icons-material/Book';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import InfoIcon from '@mui/icons-material/Info';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { Modal } from '../../components/Modal';

export const MemberSearch = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedBook, setSelectedBook] = useState(null);
  
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [reserveBook, { loading: reserving }] = useRestMutation('/api/lending/request', 'POST', 'reserveBook');

  const handleSearch = async (e) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      setLoading(true);
      try {
        const token = window.localStorage.getItem('scholario_token') || 'mock-jwt-token-123456';
        const response = await axios.get('/api/catalog', {
          headers: {
            'Authorization': token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json'
          },
          params: { title: searchTerm }
        });
        setBooks(response.data);
      } catch (err) {
        console.error('Search error:', err);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleReserve = async (bookId) => {
    try {
      await reserveBook({ variables: { bookId } });
      alert('Book reserved successfully!');
      setSelectedBook(null);
    } catch (err) {
      console.error(err);
      alert('Failed to reserve book.');
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      <Box component="header">
        <Typography variant="h4" fontWeight={900} color="text.primary" sx={{ letterSpacing: -1, textTransform: 'uppercase', fontFamily: 'Outfit, sans-serif' }}>
          Global Library Search
        </Typography>
        <Typography variant="body2" color="text.secondary" fontWeight={500}>
          Discover academic resources and reserve them for study
        </Typography>
      </Box>

      {/* Search Input Form */}
      <Box component="form" onSubmit={handleSearch} sx={{ maxWidth: 700, width: '100%' }}>
        <TextField
          fullWidth
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Search by title, ISBN, or keywords..."
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start" sx={{ color: 'text.secondary', pl: 1 }}>
                  <SearchIcon />
                </InputAdornment>
              ),
              endAdornment: (
                <InputAdornment position="end">
                  <Button 
                    type="submit"
                    disabled={loading}
                    variant="contained"
                    sx={{ 
                      borderRadius: 3.5, 
                      fontWeight: 'bold',
                      py: 1, 
                      px: 3, 
                      textTransform: 'none',
                      bgcolor: 'grey.900',
                      color: 'white',
                      '&:hover': { bgcolor: 'grey.800' }
                    }}
                  >
                    {loading ? 'Searching...' : 'Explore'}
                  </Button>
                </InputAdornment>
              )
            }
          }}
          sx={{
            '& .MuiOutlinedInput-root': {
              borderRadius: 5,
              bgcolor: 'white',
              p: 1,
              '& fieldset': {
                borderColor: 'divider',
                borderWidth: 2,
              },
              '&:hover fieldset': {
                borderColor: 'grey.300',
              },
              '&.Mui-focused fieldset': {
                borderColor: 'primary.main',
              }
            }
          }}
        />
      </Box>

      {/* Book Grid */}
      <Grid container spacing={3}>
        {loading ? (
          <Grid item xs={12}>
            <Box sx={{ py: 10, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
              <CircularProgress size={40} />
              <Typography variant="body2" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
                Accessing Global Registry...
              </Typography>
            </Box>
          </Grid>
        ) : books.length === 0 && searchTerm ? (
          <Grid item xs={12}>
            <Box sx={{ py: 10, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, border: '2px dashed', borderColor: 'divider', borderRadius: 5, bgcolor: 'white', color: 'text.disabled' }}>
              <WarningAmberIcon sx={{ fontSize: 48 }} />
              <Typography variant="body2" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 1 }}>
                No matching resources found
              </Typography>
            </Box>
          </Grid>
        ) : (
          books.map((book) => (
            <Grid item xs={12} sm={6} md={4} key={book.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', borderRadius: 5, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none', transition: 'all 0.2s', '&:hover': { borderColor: 'primary.lighter', boxShadow: 2 } }}>
                <CardContent sx={{ p: 3, flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
                  <Box sx={{ p: 1.25, borderRadius: 3, bgcolor: 'grey.50', border: '1px solid', borderColor: 'grey.100', color: 'text.secondary', display: 'inline-flex', alignSelf: 'flex-start', mb: 2 }}>
                    <BookIcon />
                  </Box>
                  <Typography variant="h6" fontWeight="bold" color="text.primary" sx={{ letterSpacing: -0.5, lineHeight: 1.25, mb: 1, flexGrow: 1 }}>
                    {book.title}
                  </Typography>
                  <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ fontFamily: 'monospace', textTransform: 'uppercase', mt: 1 }}>
                    ISBN: {book.isbn}
                  </Typography>
                  
                  <Box sx={{ display: 'flex', gap: 1.5, mt: 3 }}>
                    <Button 
                      fullWidth
                      variant="outlined" 
                      onClick={() => setSelectedBook(book)}
                      sx={{ borderRadius: 3, textTransform: 'none', fontWeight: 'bold', fontSize: 12 }}
                    >
                      View Details
                    </Button>
                    <IconButton 
                      color="primary"
                      disabled={book.state.type !== 'PUBLISHED' || reserving}
                      onClick={() => handleReserve(book.id)}
                      sx={{ 
                        borderRadius: 3, 
                        border: '1px solid',
                        borderColor: 'divider',
                        p: 1.25,
                        color: 'white',
                        bgcolor: 'primary.main',
                        '&:hover': { bgcolor: 'primary.dark' },
                        '&:disabled': { bgcolor: 'action.disabledBackground', color: 'action.disabled' }
                      }}
                    >
                      <BookmarkIcon fontSize="small" />
                    </IconButton>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))
        )}
      </Grid>

      {/* Details Modal */}
      <Modal
        isOpen={!!selectedBook}
        onClose={() => setSelectedBook(null)}
        title="Resource Forensics"
        subtitle="Detailed publication data and availability status"
      >
        {selectedBook && (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
            <Box sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 3, border: '1px solid', borderColor: 'divider' }}>
              <Typography variant="subtitle1" fontWeight="bold" color="text.primary" sx={{ mb: 1 }}>
                {selectedBook.title}
              </Typography>
              <Typography variant="body2" color="text.secondary" fontWeight={500} sx={{ lineHeight: 1.6 }}>
                {selectedBook.description || 'No digital abstract available for this resource.'}
              </Typography>
            </Box>

            <Grid container spacing={2}>
              <Grid item xs={6}>
                <Box sx={{ p: 2, bgcolor: 'white', borderRadius: 3, border: '1px solid', borderColor: 'divider' }}>
                  <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', display: 'block', mb: 0.5 }}>
                    Status
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Box 
                      sx={{ 
                        width: 8,
                        height: 8,
                        borderRadius: '50%', 
                        bgcolor: selectedBook.state.type === 'PUBLISHED' ? 'success.main' : 'error.main' 
                      }} 
                    />
                    <Typography variant="body2" fontWeight="bold">
                      {selectedBook.state.type}
                    </Typography>
                  </Box>
                </Box>
              </Grid>
              <Grid item xs={6}>
                <Box sx={{ p: 2, bgcolor: 'white', borderRadius: 3, border: '1px solid', borderColor: 'divider' }}>
                  <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', display: 'block', mb: 0.5 }}>
                    Registry Code
                  </Typography>
                  <Typography variant="body2" fontWeight="bold" sx={{ fontFamily: 'monospace' }}>
                    {selectedBook.isbn}
                  </Typography>
                </Box>
              </Grid>
            </Grid>

            <Box sx={{ p: 3, bgcolor: 'grey.900', color: 'white', borderRadius: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2, color: 'primary.light' }}>
                <CheckCircleIcon fontSize="small" />
                <Typography variant="caption" fontWeight="bold" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
                  Acquisition Protocol
                </Typography>
              </Box>
              <Button 
                fullWidth
                variant="contained"
                onClick={() => handleReserve(selectedBook.id)}
                disabled={selectedBook.state.type !== 'PUBLISHED' || reserving}
                sx={{ 
                  py: 1.5, 
                  bgcolor: 'primary.main', 
                  color: 'white', 
                  borderRadius: 3, 
                  fontWeight: 'bold',
                  textTransform: 'none',
                  boxShadow: '0 4px 14px rgba(99, 102, 241, 0.4)',
                  '&:hover': { bgcolor: 'primary.dark' }
                }}
              >
                {reserving ? 'Processing...' : 'Reserve for Collection'}
              </Button>
            </Box>
          </Box>
        )}
      </Modal>
    </Box>
  );
};
