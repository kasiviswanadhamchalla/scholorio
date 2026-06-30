import { useRestQuery, useRestMutation } from '../../hooks/useRest';
import React, { useState } from 'react';
import axios from 'axios';

import { 
  Box, 
  Typography, 
  Button, 
  TextField, 
  InputAdornment,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  CircularProgress,
  Alert,
  Menu,
  MenuItem
} from '@mui/material';
import BookIcon from '@mui/icons-material/Book';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { Modal } from '../../components/Modal';

const parseDate = (d) => {
  if (!d) return 'N/A';
  if (Array.isArray(d)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = d;
    return new Date(year, month - 1, day, hour, minute, second).toLocaleDateString();
  }
  const date = new Date(d);
  return isNaN(date.getTime()) ? 'N/A' : date.toLocaleDateString();
};

const getHeaders = () => {
  const token = window.localStorage.getItem('scholario_token') || 'mock-jwt-token-123456';
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : '',
  };
};





export const LibrarianStock = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [isStockModalOpen, setIsStockModalOpen] = useState(false);
  
  const [title, setTitle] = useState('');
  const [isbn, setIsbn] = useState('');
  const [description, setDescription] = useState('');

  const [menuAnchor, setMenuAnchor] = useState(null);
  const [activeBook, setActiveBook] = useState(null);
  const [activeReviewRequestId, setActiveReviewRequestId] = useState(null);

  const { data, loading, refetch } = useRestQuery('/api/catalog', 'getAllBooks');
  const [createBook, { loading: creating }] = useRestMutation('/api/catalog', 'POST', 'createBook');
  const [publishBook] = useRestMutation((v) => `/api/catalog/${v.id}/publish`, 'POST', 'publishBook');
  const [archiveBook] = useRestMutation((v) => `/api/catalog/${v.id}/archive`, 'POST', 'archiveBook');
  const [submitForReview] = useRestMutation((v) => `/api/approval/submit?bookId=${v.id}`, 'POST', 'submitForReview');
  const [deleteBook] = useRestMutation((v) => `/api/catalog/${v.id}`, 'DELETE', 'deleteBook');

  const handleOpenMenu = async (event, book) => {
    setMenuAnchor(event.currentTarget);
    setActiveBook(book);
    if (book.state?.type === 'REVIEW') {
      try {
        const response = await axios.get(`/api/approval/status/${book.id}`, {
          headers: getHeaders()
        });
        if (response.data) {
          setActiveReviewRequestId(response.data.id);
        }
      } catch (err) {
        console.error('Failed to fetch review status:', err);
      }
    }
  };

  const handleCloseMenu = () => {
    setMenuAnchor(null);
    setActiveBook(null);
    setActiveReviewRequestId(null);
  };

  const handlePublish = async () => {
    if (!activeBook) return;
    try {
      await publishBook({ variables: { id: activeBook.id } });
      refetch();
    } catch (err) {
      console.error(err);
    } finally {
      handleCloseMenu();
    }
  };

  const handleArchive = async () => {
    if (!activeBook) return;
    try {
      await archiveBook({ variables: { id: activeBook.id } });
      refetch();
    } catch (err) {
      console.error(err);
    } finally {
      handleCloseMenu();
    }
  };

  const handleSubmitReview = async () => {
    if (!activeBook) return;
    try {
      await submitForReview({ variables: { id: activeBook.id } });
      refetch();
    } catch (err) {
      console.error(err);
    } finally {
      handleCloseMenu();
    }
  };

  const handleApprove = async () => {
    if (!activeReviewRequestId) return;
    try {
      await axios.post(`/api/approval/${activeReviewRequestId}/approve?feedback=Approved by Librarian`, {}, {
        headers: getHeaders()
      });
      refetch();
    } catch (err) {
      console.error('Failed to approve book:', err);
    } finally {
      handleCloseMenu();
    }
  };

  const handleReject = async () => {
    if (!activeReviewRequestId) return;
    const feedback = window.prompt("Enter rejection feedback:");
    if (feedback === null) return;
    try {
      await axios.post(`/api/approval/${activeReviewRequestId}/reject?feedback=${encodeURIComponent(feedback || 'Rejected')}`, {}, {
        headers: getHeaders()
      });
      refetch();
    } catch (err) {
      console.error('Failed to reject book:', err);
    } finally {
      handleCloseMenu();
    }
  };

  const handleDelete = async () => {
    if (!activeBook) return;
    if (!window.confirm(`Are you sure you want to delete "${activeBook.title}"?`)) return;
    try {
      await deleteBook({ variables: { id: activeBook.id } });
      refetch();
    } catch (err) {
      console.error(err);
    } finally {
      handleCloseMenu();
    }
  };

  const handleAddStock = async () => {
    if (!title || !isbn) return;
    try {
      await createBook({
        variables: {
          input: { title, isbn, description }
        }
      });
      setIsStockModalOpen(false);
      setTitle('');
      setIsbn('');
      setDescription('');
      refetch();
    } catch (err) {
      console.error(err);
    }
  };

  const books = data?.getAllBooks || [];
  const filteredBooks = books.filter((b) => 
    b.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    b.isbn.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h4" fontWeight={900} color="text.primary" sx={{ letterSpacing: -1, textTransform: 'uppercase', fontFamily: 'Outfit, sans-serif' }}>
            Inventory Matrix
          </Typography>
          <Typography variant="body2" color="text.secondary" fontWeight={500}>
            Global academic resource stock management
          </Typography>
        </Box>
        <Button 
          variant="contained" 
          startIcon={<AddIcon />}
          onClick={() => setIsStockModalOpen(true)}
          sx={{ 
            borderRadius: 2, 
            py: 1.25, 
            px: 3, 
            bgcolor: 'grey.900', 
            color: 'white',
            fontWeight: 'bold',
            textTransform: 'none',
            fontSize: 12,
            boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
            '&:hover': { bgcolor: 'grey.800' }
          }}
        >
          Add New Stock
        </Button>
      </Box>

      {/* Filters row */}
      <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
        <TextField
          fullWidth
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Search inventory by title or ISBN..."
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start" sx={{ pl: 0.5 }}>
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              )
            }
          }}
          sx={{
            flexGrow: 1,
            '& .MuiOutlinedInput-root': {
              borderRadius: 1.5,
              bgcolor: 'white',
              fontSize: 14,
              '& fieldset': { borderColor: 'divider' },
              '&:hover fieldset': { borderColor: 'grey.300' }
            }
          }}
        />
        <Button 
          variant="outlined" 
          startIcon={<FilterListIcon />}
          sx={{ 
            borderRadius: 2, 
            fontWeight: 'bold', 
            py: 1.25, 
            px: 3,
            textTransform: 'none', 
            borderColor: 'divider',
            color: 'text.secondary',
            minWidth: { sm: 180 },
            '&:hover': {
              borderColor: 'primary.main',
              bgcolor: 'transparent'
            }
          }}
        >
          Sort Registry
        </Button>
      </Box>

      {/* Table Section */}
      <TableContainer component={Paper} sx={{ borderRadius: 2, border: '1px solid', borderColor: 'divider', boxShadow: 'none', overflow: 'hidden' }}>
        <Table sx={{ minWidth: 650 }}>
          <TableHead sx={{ bgcolor: 'grey.50' }}>
            <TableRow sx={{ '& th': { fontSize: 10, fontWeight: 'black', color: 'text.secondary', textTransform: 'uppercase', letterSpacing: 1.2 } }}>
              <TableCell sx={{ pl: 4, py: 2 }}>Resource Entity</TableCell>
              <TableCell>Registry ISBN</TableCell>
              <TableCell>Deployment Status</TableCell>
              <TableCell>On-Boarding Date</TableCell>
              <TableCell align="right" sx={{ pr: 4 }}>Ops</TableCell>
            </TableRow>
          </TableHead>
          <TableBody sx={{ fontFamily: 'monospace' }}>
            {loading ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 8 }}>
                  <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1.5 }}>
                    <CircularProgress size={24} />
                    <Typography variant="caption" fontWeight="bold" color="text.secondary" sx={{ textTransform: 'uppercase' }}>
                      Scanning Inventory Nodes...
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            ) : filteredBooks.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 6, color: 'text.secondary', fontStyle: 'italic' }}>
                  No stock found in registry
                </TableCell>
              </TableRow>
            ) : filteredBooks.map((book) => (
              <TableRow key={book.id} sx={{ '&:hover': { bgcolor: 'grey.50' } }}>
                <TableCell sx={{ pl: 4, py: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Box sx={{ p: 1, borderRadius: 1.5, bgcolor: 'grey.50', border: '1px solid', borderColor: 'grey.200', color: 'text.secondary', display: 'flex' }}>
                      <BookIcon fontSize="small" />
                    </Box>
                    <Typography variant="body2" fontWeight="bold" color="text.primary" sx={{ fontFamily: 'sans-serif' }}>
                      {book.title}
                    </Typography>
                  </Box>
                </TableCell>
                <TableCell sx={{ fontSize: 12, color: 'text.secondary', fontWeight: 500 }}>{book.isbn}</TableCell>
                <TableCell>
                  <Chip 
                    label={book.state.type} 
                    size="small" 
                    color={book.state.type === 'PUBLISHED' ? 'success' : 'default'}
                    sx={{ 
                      borderRadius: 1, 
                      fontWeight: 'bold', 
                      fontSize: 9, 
                      height: 20,
                      textTransform: 'uppercase'
                    }} 
                  />
                </TableCell>
                <TableCell sx={{ fontSize: 11, color: 'text.secondary', fontWeight: 500 }}>
                  {parseDate(book.createdAt)}
                </TableCell>
                <TableCell align="right" sx={{ pr: 4 }}>
                  <IconButton 
                    size="small" 
                    sx={{ borderRadius: 1 }}
                    onClick={(e) => handleOpenMenu(e, book)}
                  >
                    <MoreVertIcon sx={{ fontSize: 16 }} />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Row Ops Actions Menu */}
      <Menu
        anchorEl={menuAnchor}
        open={Boolean(menuAnchor)}
        onClose={handleCloseMenu}
        slotProps={{
          paper: {
            sx: {
              borderRadius: 2,
              mt: 0.5,
              minWidth: 180,
              boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06)',
              border: '1px solid',
              borderColor: 'divider',
              p: 0.5
            }
          }
        }}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
      >
        {activeBook?.state?.type === 'DRAFT' && (
          <MenuItem 
            onClick={handlePublish}
            sx={{ borderRadius: 1.5, fontSize: 12, fontWeight: 600, py: 1, px: 1.5 }}
          >
            Publish Resource
          </MenuItem>
        )}
        {activeBook?.state?.type === 'DRAFT' && (
          <MenuItem 
            onClick={handleSubmitReview}
            sx={{ borderRadius: 1.5, fontSize: 12, fontWeight: 600, py: 1, px: 1.5 }}
          >
            Submit for Review
          </MenuItem>
        )}
        {activeBook?.state?.type === 'PUBLISHED' && (
          <MenuItem 
            onClick={handleArchive}
            sx={{ borderRadius: 1.5, fontSize: 12, fontWeight: 600, py: 1, px: 1.5 }}
          >
            Archive Resource
          </MenuItem>
        )}
        {activeBook?.state?.type === 'REVIEW' && (
          <MenuItem 
            onClick={handleApprove}
            disabled={!activeReviewRequestId}
            sx={{ borderRadius: 1.5, fontSize: 12, fontWeight: 600, py: 1, px: 1.5 }}
          >
            Approve Resource
          </MenuItem>
        )}
        {activeBook?.state?.type === 'REVIEW' && (
          <MenuItem 
            onClick={handleReject}
            disabled={!activeReviewRequestId}
            sx={{ borderRadius: 1.5, fontSize: 12, fontWeight: 600, py: 1, px: 1.5, color: 'error.main' }}
          >
            Reject Resource
          </MenuItem>
        )}
        <MenuItem 
          onClick={handleDelete}
          sx={{ borderRadius: 1.5, fontSize: 12, fontWeight: 800, py: 1, px: 1.5, color: 'error.main', '&:hover': { bgcolor: 'error.lighter' } }}
        >
          Delete Resource
        </MenuItem>
      </Menu>

      {/* Add Stock Modal */}
      <Modal 
        isOpen={isStockModalOpen} 
        onClose={() => setIsStockModalOpen(false)} 
        title="Provision New Stock" 
        subtitle="Manually board a new physical resource into the digital registry"
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5 }}>
              Publication Title
            </Typography>
            <TextField 
              required
              fullWidth
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Modern Operating Systems"
              sx={{
                '& .MuiOutlinedInput-root': {
                  borderRadius: 1.5,
                  bgcolor: 'grey.50',
                  fontWeight: 600,
                  fontSize: 14,
                  '& fieldset': { borderColor: 'divider' },
                }
              }}
            />
          </Box>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5 }}>
              Registry ISBN
            </Typography>
            <TextField 
              required
              fullWidth
              value={isbn}
              onChange={(e) => setIsbn(e.target.value)}
              placeholder="978-..."
              sx={{
                '& .MuiOutlinedInput-root': {
                  borderRadius: 1.5,
                  bgcolor: 'grey.50',
                  fontWeight: 600,
                  fontSize: 14,
                  fontFamily: 'monospace',
                  '& fieldset': { borderColor: 'divider' },
                }
              }}
            />
          </Box>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5 }}>
              Inventory Notes
            </Typography>
            <TextField 
              fullWidth
              multiline
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Condition, shelf location, or acquisition details..."
              sx={{
                '& .MuiOutlinedInput-root': {
                  borderRadius: 1.5,
                  bgcolor: 'grey.50',
                  fontWeight: 500,
                  fontSize: 14,
                  '& fieldset': { borderColor: 'divider' },
                }
              }}
            />
          </Box>

          <Alert 
            severity="warning" 
            icon={<WarningAmberIcon sx={{ fontSize: 20 }} />}
            sx={{ 
              borderRadius: 2, 
              bgcolor: 'warning.lighter', 
              color: 'warning.dark',
              border: '1px solid',
              borderColor: 'warning.light',
              '& .MuiAlert-icon': { color: 'warning.main', display: 'flex', alignItems: 'center' }
            }}
          >
            <Typography variant="caption" fontWeight="black" sx={{ textTransform: 'uppercase', display: 'block', mb: 0.5, letterSpacing: 0.5 }}>
              Inventory Rule
            </Typography>
            <Typography variant="body2" sx={{ fontSize: 11, fontWeight: 500, lineHeight: 1.4 }}>
              Manually added stock defaults to DRAFT status and requires validation before global circulation.
            </Typography>
          </Alert>

          <Button 
            onClick={handleAddStock}
            disabled={!title || !isbn || creating}
            variant="contained"
            sx={{
              py: 1.5,
              bgcolor: 'grey.900',
              color: 'white',
              borderRadius: 2,
              fontWeight: 'bold',
              fontSize: 12,
              textTransform: 'uppercase',
              letterSpacing: 1.2,
              '&:hover': { bgcolor: 'grey.800' },
              '&:disabled': { bgcolor: 'action.disabledBackground', color: 'action.disabled' }
            }}
          >
            {creating ? 'Syncing...' : 'Initialize Stock Boarding'}
          </Button>
        </Box>
      </Modal>
    </Box>
  );
};
