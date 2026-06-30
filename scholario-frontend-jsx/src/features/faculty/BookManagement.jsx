import { useRestQuery, useRestMutation } from '../../hooks/useRest';
import React, { useState } from 'react';

const parseDate = (d) => {
  if (!d) return 'N/A';
  if (Array.isArray(d)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = d;
    return new Date(year, month - 1, day, hour, minute, second).toLocaleDateString();
  }
  const date = new Date(d);
  return isNaN(date.getTime()) ? 'N/A' : date.toLocaleDateString();
};


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
  Grid,
  Menu,
  MenuItem,
  Alert
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';
import SendIcon from '@mui/icons-material/Send';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import LaunchIcon from '@mui/icons-material/Launch';
import RefreshIcon from '@mui/icons-material/Refresh';
import { Modal } from '../../components/Modal';
import { CustomSelect } from '../../components/CustomSelect';















export const BookManagement = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedBook, setSelectedBook] = useState(null);
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedReviewer, setSelectedReviewer] = useState("");

  // Create Book Form States
  const [title, setTitle] = useState('');
  const [isbn, setIsbn] = useState('');
  const [description, setDescription] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Menu Anchors for More options
  const [menuAnchor, setMenuAnchor] = useState(null);
  const [activeMenuBook, setActiveMenuBook] = useState(null);

  const { data: profileData } = useRestQuery('/api/member/profile', 'getMyProfile');
  const facultyId = profileData?.getMyProfile?.id;

  const { data: booksData, loading: booksLoading, refetch: refetchBooks } = useRestQuery('/api/catalog', 'getBooksByFaculty', {
    variables: { facultyId },
    skip: !facultyId
  });

  const { data: facultyListData } = useRestQuery('/api/member/users', 'getFacultyList');
  const { data: deptData, loading: deptLoading } = useRestQuery('/api/member/departments', 'getDepartments');

  const [submitForReview] = useRestMutation((v) => `/api/approval/submit?bookId=${v.bookId}&reviewerId=${v.reviewerId || ''}`, 'POST', 'submitBookForReview');
  const [publishBook] = useRestMutation((v) => `/api/catalog/${v.id}/publish`, 'POST', 'publishBook');
  const [createBook] = useRestMutation('/api/catalog', 'POST', 'createBook');

  const handleReviewSubmit = async () => {
    if (!selectedBook || !selectedReviewer) return;
    try {
      await submitForReview({
        variables: {
          bookId: selectedBook.id,
          reviewerId: selectedReviewer
        }
      });
      setIsReviewModalOpen(false);
      setSelectedBook(null);
      setSelectedReviewer("");
      refetchBooks();
      alert('Publication submitted for peer review');
    } catch (err) {
      console.error('Error submitting for review:', err);
    }
  };

  const handleCreateBook = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await createBook({
        variables: {
          input: { title, isbn, description }
        }
      });
      setIsCreateModalOpen(false);
      setTitle('');
      setIsbn('');
      setDescription('');
      refetchBooks();
      alert('New resource drafted successfully');
    } catch (err) {
      console.error('Error creating book:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handlePublish = async (bookId) => {
    try {
      await publishBook({ variables: { id: bookId } });
      refetchBooks();
      alert('Publication finalized and published globally');
    } catch (err) {
      console.error(err);
    }
  };

  const handleMenuOpen = (event, book) => {
    setMenuAnchor(event.currentTarget);
    setActiveMenuBook(book);
  };

  const handleMenuClose = () => {
    setMenuAnchor(null);
    setActiveMenuBook(null);
  };

  const books = booksData?.getBooksByFaculty || [];
  const filteredBooks = books.filter((book) => 
    book.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    book.isbn.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getStatusColor = (type) => {
    switch (type) {
      case 'PUBLISHED': return 'success';
      case 'REVIEW': return 'warning';
      case 'DRAFT': return 'default';
      case 'ARCHIVED': return 'error';
      default: return 'default';
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h4" fontWeight={900} color="text.primary" sx={{ letterSpacing: -1, textTransform: 'uppercase', fontFamily: 'Outfit, sans-serif' }}>
            Publication Engine
          </Typography>
          <Typography variant="body2" color="text.secondary" fontWeight={500}>
            Manage your authored academic resources and review cycles
          </Typography>
        </Box>
        <Button 
          variant="contained" 
          startIcon={<AddIcon />}
          onClick={() => setIsCreateModalOpen(true)}
          sx={{ 
            borderRadius: 3.5, 
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
          Draft New Resource
        </Button>
      </Box>

      {/* Filters row */}
      <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
        <TextField
          fullWidth
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Search registry by title or ISBN..."
          InputProps={{
            startAdornment: (
              <InputAdornment position="start" sx={{ pl: 0.5 }}>
                <SearchIcon fontSize="small" />
              </InputAdornment>
            )
          }}
          sx={{
            flexGrow: 1,
            '& .MuiOutlinedInput-root': {
              borderRadius: 3.5,
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
            borderRadius: 3.5, 
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
          Filter Registry
        </Button>
      </Box>

      {/* Table Section */}
      <TableContainer component={Paper} sx={{ borderRadius: 4, border: '1px solid', borderColor: 'divider', boxShadow: 'none', overflow: 'hidden' }}>
        <Table sx={{ minWidth: 650 }}>
          <TableHead sx={{ bgcolor: 'grey.50' }}>
            <TableRow sx={{ '& th': { fontSize: 10, fontWeight: 'black', color: 'text.secondary', textTransform: 'uppercase', letterSpacing: 1.2 } }}>
              <TableCell sx={{ pl: 4, py: 2 }}>Publication Entity</TableCell>
              <TableCell>Registry ISBN</TableCell>
              <TableCell>Operational Status</TableCell>
              <TableCell>Creation Date</TableCell>
              <TableCell align="right" sx={{ pr: 4 }}>Ops</TableCell>
            </TableRow>
          </TableHead>
          <TableBody sx={{ fontFamily: 'monospace' }}>
            {booksLoading ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 8 }}>
                  <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1.5 }}>
                    <CircularProgress size={24} />
                    <Typography variant="caption" fontWeight="bold" color="text.secondary" sx={{ textTransform: 'uppercase' }}>
                      Synchronizing Publication Data...
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            ) : filteredBooks.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 6, color: 'text.secondary', fontStyle: 'italic' }}>
                  No matching resources in registry
                </TableCell>
              </TableRow>
            ) : filteredBooks.map((book) => (
              <TableRow key={book.id} sx={{ '&:hover': { bgcolor: 'grey.50' } }}>
                <TableCell sx={{ pl: 4, py: 2.5 }}>
                  <Typography variant="body2" fontWeight="bold" color="text.primary" sx={{ fontFamily: 'sans-serif' }}>
                    {book.title}
                  </Typography>
                </TableCell>
                <TableCell sx={{ fontSize: 12, color: 'text.secondary', fontWeight: 500 }}>{book.isbn}</TableCell>
                <TableCell>
                  <Chip 
                    label={book.state.type} 
                    size="small" 
                    color={getStatusColor(book.state.type)}
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
                  <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                    {book.state.type === 'DRAFT' && (
                      <IconButton 
                        size="small" 
                        color="primary"
                        onClick={() => {
                          setSelectedBook(book);
                          setIsReviewModalOpen(true);
                        }}
                        sx={{ bgcolor: 'primary.lighter', '&:hover': { bgcolor: 'primary.light', color: 'white' }, borderRadius: 2 }}
                        title="Submit for Review"
                      >
                        <SendIcon sx={{ fontSize: 14 }} />
                      </IconButton>
                    )}
                    {book.state.type === 'REVIEW' && (
                      <IconButton 
                        size="small" 
                        color="success"
                        onClick={() => handlePublish(book.id)}
                        sx={{ bgcolor: 'success.lighter', '&:hover': { bgcolor: 'success.light', color: 'white' }, borderRadius: 2 }}
                        title="Finalize Publication"
                      >
                        <CheckCircleIcon sx={{ fontSize: 14 }} />
                      </IconButton>
                    )}
                    <IconButton size="small" onClick={(e) => handleMenuOpen(e, book)} sx={{ borderRadius: 2 }}>
                      <MoreVertIcon sx={{ fontSize: 16 }} />
                    </IconButton>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Row Ops Menu */}
      <Menu
        anchorEl={menuAnchor}
        open={Boolean(menuAnchor)}
        onClose={handleMenuClose}
        slotProps={{ paper: { sx: { borderRadius: 2, mt: 0.5 } } }}
      >
        <MenuItem onClick={handleMenuClose} sx={{ fontSize: 12, fontWeight: 600 }}>Edit Draft</MenuItem>
        <MenuItem onClick={handleMenuClose} sx={{ fontSize: 12, fontWeight: 600, color: 'error.main' }}>Decommission</MenuItem>
      </Menu>

      {/* Review Modal */}
      <Modal 
        isOpen={isReviewModalOpen} 
        onClose={() => setIsReviewModalOpen(false)} 
        title="Peer Review Submission" 
        subtitle="Initiate the authorization cycle for this publication"
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5 }}>
          <Box sx={{ p: 2, bg: 'grey.50', bgcolor: 'grey.50', borderRadius: 3, border: '1px solid', borderColor: 'divider' }}>
            <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 1, display: 'block', mb: 0.5 }}>
              Target Entity
            </Typography>
            <Typography variant="body2" fontWeight="bold" color="text.primary">
              {selectedBook?.title}
            </Typography>
          </Box>
           
          <CustomSelect 
            label="Assigned Reviewer"
            options={facultyListData?.getFacultyList
              .filter((f) => f.id !== facultyId)
              .map((f) => ({ id: f.id, name: `${f.fullName} (@${f.username})` })) || []}
            value={selectedReviewer}
            onChange={setSelectedReviewer}
            placeholder="Select peer reviewer..."
          />

          <Alert 
            severity="info" 
            icon={<LaunchIcon sx={{ fontSize: 20 }} />}
            sx={{ 
              borderRadius: 3, 
              bgcolor: 'primary.lighter', 
              color: 'primary.dark',
              border: '1px solid',
              borderColor: 'primary.light',
              '& .MuiAlert-icon': { color: 'primary.main', display: 'flex', alignItems: 'center' }
            }}
          >
            <Typography variant="caption" fontWeight="black" sx={{ textTransform: 'uppercase', display: 'block', mb: 0.5, letterSpacing: 0.5 }}>
              Verification Notice
            </Typography>
            <Typography variant="body2" sx={{ fontSize: 11, fontWeight: 500, lineHeight: 1.4 }}>
              Peer review is a mandatory protocol. Reviewers will be notified of your submission via global telemetry.
            </Typography>
          </Alert>

          <Button 
            fullWidth
            onClick={handleReviewSubmit}
            disabled={!selectedReviewer}
            variant="contained"
            sx={{ 
              py: 1.5, 
              borderRadius: 3.5, 
              bgcolor: 'grey.900', 
              color: 'white',
              fontWeight: 'bold', 
              fontSize: 12,
              textTransform: 'uppercase',
              letterSpacing: 1.2,
              boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
              '&:hover': { bgcolor: 'grey.800' }
            }}
          >
            Finalize Review Request
          </Button>
        </Box>
      </Modal>

      {/* Create Book Modal */}
      <Modal 
        isOpen={isCreateModalOpen} 
        onClose={() => setIsCreateModalOpen(false)} 
        title="Draft Resource" 
        subtitle="Initialize a new publication in the Scholario registry"
      >
        <Box component="form" onSubmit={handleCreateBook} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5, pl: 0.5 }}>
              Publication Title
            </Typography>
            <TextField 
              required
              fullWidth
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Advanced Quantum Mechanics"
              sx={{
                '& .MuiOutlinedInput-root': {
                  borderRadius: 3,
                  bgcolor: 'grey.50',
                  fontWeight: 600,
                  fontSize: 14,
                  '& fieldset': { borderColor: 'divider' },
                }
              }}
            />
          </Box>

          <Grid container spacing={2}>
            <Grid item xs={6}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5, pl: 0.5 }}>
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
                      borderRadius: 3,
                      bgcolor: 'grey.50',
                      fontWeight: 600,
                      fontSize: 14,
                      fontFamily: 'monospace',
                      '& fieldset': { borderColor: 'divider' },
                    }
                  }}
                />
              </Box>
            </Grid>
            <Grid item xs={6}>
              <CustomSelect 
                label="Authored Dept"
                options={deptData?.getDepartments.map((d) => ({ id: d.id, name: d.name })) || []}
                value={departmentId}
                onChange={setDepartmentId}
                placeholder={deptLoading ? "Syncing..." : "Select Unit"}
              />
            </Grid>
          </Grid>

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5, pl: 0.5 }}>
              Resource Abstract
            </Typography>
            <TextField 
              fullWidth
              multiline
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Module summary and publication scope..."
              sx={{
                '& .MuiOutlinedInput-root': {
                  borderRadius: 3,
                  bgcolor: 'grey.50',
                  fontWeight: 505,
                  fontSize: 14,
                  '& fieldset': { borderColor: 'divider' },
                }
              }}
            />
          </Box>

          <Button 
            type="submit"
            disabled={isSubmitting || deptLoading}
            variant="contained"
            sx={{
              py: 1.5,
              bgcolor: 'grey.900',
              color: 'white',
              borderRadius: 3,
              fontWeight: 'bold',
              fontSize: 12,
              textTransform: 'uppercase',
              letterSpacing: 1.2,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 1,
              '&:hover': { bgcolor: 'grey.800' },
              '&:disabled': { bgcolor: 'action.disabledBackground', color: 'action.disabled' }
            }}
          >
            {isSubmitting ? <CircularProgress size={16} color="inherit" /> : <><SendIcon sx={{ fontSize: 16 }} /> <span>Initialize Publication</span></>}
          </Button>
        </Box>
      </Modal>
    </Box>
  );
};
