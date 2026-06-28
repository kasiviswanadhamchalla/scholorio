import { useRestQuery, useRestMutation } from '../../hooks/useRest';
import React, { useState } from 'react';


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
  Alert
} from '@mui/material';
import BookIcon from '@mui/icons-material/Book';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { Modal } from '../../components/Modal';





export const LibrarianStock = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [isStockModalOpen, setIsStockModalOpen] = useState(false);
  
  const [title, setTitle] = useState('');
  const [isbn, setIsbn] = useState('');
  const [description, setDescription] = useState('');

  const { data, loading, refetch } = useRestQuery('/api/catalog', 'getAllBooks');
  const [createBook, { loading: creating }] = useRestMutation('/api/catalog', 'POST', 'createBook');

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
          Sort Registry
        </Button>
      </Box>

      {/* Table Section */}
      <TableContainer component={Paper} sx={{ borderRadius: 4, border: '1px solid', borderColor: 'divider', boxShadow: 'none', overflow: 'hidden' }}>
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
                    <Box sx={{ p: 1, borderRadius: 2, bgcolor: 'grey.50', border: '1px solid', borderColor: 'grey.200', color: 'text.secondary', display: 'flex' }}>
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
                  {new Date(book.createdAt).toLocaleDateString()}
                </TableCell>
                <TableCell align="right" sx={{ pr: 4 }}>
                  <IconButton size="small" sx={{ borderRadius: 2 }}>
                    <MoreVertIcon sx={{ fontSize: 16 }} />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

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
                  borderRadius: 3,
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
                  borderRadius: 3,
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
              borderRadius: 3, 
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
              borderRadius: 3.5,
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
