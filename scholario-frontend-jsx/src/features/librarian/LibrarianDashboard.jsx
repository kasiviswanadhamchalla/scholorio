import { useRestQuery, useRestMutation } from '../../hooks/useRest';
import React, { useState } from 'react';


import { useNavigate } from 'react-router-dom';
import { 
  Box, 
  Grid, 
  Card, 
  CardContent, 
  Typography, 
  Button, 
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Alert
} from '@mui/material';
import BookIcon from '@mui/icons-material/Book';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import AddIcon from '@mui/icons-material/Add';
import DownloadIcon from '@mui/icons-material/Download';
import NorthEastIcon from '@mui/icons-material/NorthEast';
import SyncIcon from '@mui/icons-material/Sync';
import LibraryAddIcon from '@mui/icons-material/LibraryAdd';
import HistoryIcon from '@mui/icons-material/History';
import { Modal } from '../../components/Modal';
import { CustomSelect } from '../../components/CustomSelect';













const StatCard = ({ title, value, icon, color, bg }) => (
  <Card sx={{ borderRadius: 4, border: '1px solid', borderColor: 'divider', boxShadow: 'none' }}>
    <CardContent sx={{ p: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center', '&:last-child': { pb: 3 } }}>
      <Box>
        <Typography variant="body2" color="text.secondary" fontWeight={500} sx={{ mb: 0.5 }}>
          {title}
        </Typography>
        <Typography variant="h5" fontWeight="bold" color="text.primary">
          {value}
        </Typography>
      </Box>
      <Box sx={{ p: 1.5, borderRadius: 3, bgcolor: bg, color: color, display: 'flex' }}>
        {icon}
      </Box>
    </CardContent>
  </Card>
);

const ActionCard = ({ title, description, icon: Icon, color, onClick }) => {
  const colorScheme = {
    indigo: {
      bg: 'primary.main',
      hover: 'primary.dark',
      shadow: 'rgba(99, 102, 241, 0.2)'
    },
    emerald: {
      bg: 'success.main',
      hover: 'success.dark',
      shadow: 'rgba(16, 185, 129, 0.2)'
    }
  }[color];

  return (
    <Button
      onClick={onClick}
      fullWidth
      variant="contained"
      sx={{
        bgcolor: colorScheme.bg,
        color: 'white',
        borderRadius: 4.5,
        p: 2.5,
        boxShadow: `0 8px 24px ${colorScheme.shadow}`,
        textTransform: 'none',
        display: 'flex',
        alignItems: 'center',
        gap: 2,
        justifyContent: 'flex-start',
        '&:hover': {
          bgcolor: colorScheme.hover,
          boxShadow: `0 12px 28px ${colorScheme.shadow}`,
        }
      }}
    >
      <Box sx={{ p: 1.5, bg: 'rgba(255,255,255,0.2)', bgcolor: 'rgba(255,255,255,0.2)', borderRadius: 2.5, display: 'flex', color: 'white' }}>
        <Icon sx={{ fontSize: 24 }} />
      </Box>
      <Box sx={{ flexGrow: 1, textAlign: 'left' }}>
        <Typography variant="subtitle1" fontWeight="bold" sx={{ color: 'white', lineHeight: 1.2 }}>
          {title}
        </Typography>
        <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.85)', display: 'block', mt: 0.25, fontWeight: 500 }}>
          {description}
        </Typography>
      </Box>
      <NorthEastIcon sx={{ opacity: 0.6, fontSize: 18 }} />
    </Button>
  );
};

export const LibrarianDashboard = () => {
  const navigate = useNavigate();
  const [isIssueModalOpen, setIsIssueModalOpen] = useState(false);
  const [isReturnModalOpen, setIsReturnModalOpen] = useState(false);
  const [selectedMember, setSelectedMember] = useState('');
  const [selectedBook, setSelectedBook] = useState('');
  const [selectedIssueId, setSelectedIssueId] = useState('');

  const [issueMutation] = useRestMutation('/api/lending/issue', 'POST', 'issueBook');
  const [returnMutation] = useRestMutation('/api/lending/return', 'POST', 'returnBook');
  
  const { loading, error, data, refetch } = useRestQuery('/api/lending/due-dates', 'getDueDates');
  const { data: membersData } = useRestQuery('/api/member/users', 'getStudentList');
  const { data: booksData } = useRestQuery('/api/catalog', 'getAllBooks');
  const { data: statsData } = useRestQuery('/api/member/dashboard', 'getLibrarianStats');

  const handleIssue = async () => {
    if (!selectedBook || !selectedMember) return;
    try {
      await issueMutation({ 
        variables: { bookId: selectedBook, userId: selectedMember } 
      });
      setIsIssueModalOpen(false);
      setSelectedBook('');
      setSelectedMember('');
      refetch();
    } catch (err) {
      console.error('Failed to issue book:', err);
    }
  };

  const handleReturn = async () => {
    if (!selectedIssueId || !selectedMember) return;
    try {
      await returnMutation({ 
        variables: { issueId: selectedIssueId, userId: selectedMember } 
      });
      setIsReturnModalOpen(false);
      setSelectedIssueId('');
      setSelectedMember('');
      refetch();
    } catch (err) {
      console.error('Failed to return book:', err);
    }
  };

  const memberOptions = membersData?.getStudentList.map((s) => ({ id: s.id, name: s.fullName })) || [];
  const bookOptions = booksData?.getAllBooks.map((b) => ({ id: b.id, name: b.title })) || [];
  
  const activeIssuesForMember = data?.getDueDates.filter(
    (issue) => issue.userId === selectedMember && issue.state.type !== 'RETURNED'
  ) || [];

  const issueOptions = activeIssuesForMember.map((issue) => {
    const book = booksData?.getAllBooks.find((b) => b.id === issue.bookId);
    return {
      id: issue.id,
      name: book ? `${book.title} (Due: ${new Date(issue.dueDate).toLocaleDateString()})` : `Issue #${issue.id.substring(0, 8)}`
    };
  });

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={950} color="text.primary" sx={{ letterSpacing: -1, fontFamily: 'Outfit, sans-serif' }}>
            Librarian Hub
          </Typography>
          <Typography variant="body2" color="text.secondary" fontWeight={500}>
            Manage circulation, track assets, and handle member requests.
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1.5, width: { xs: '100%', sm: 'auto' } }}>
          <Button
            variant="outlined"
            startIcon={<DownloadIcon />}
            sx={{ 
              borderRadius: 3.5, 
              py: 1, 
              px: 2, 
              fontWeight: 'bold',
              textTransform: 'none',
              borderColor: 'divider',
              color: 'text.secondary',
              '&:hover': { borderColor: 'primary.main', bgcolor: 'transparent' }
            }}
          >
            Export Logs
          </Button>
          <Button 
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/librarian/stock')}
            sx={{ 
              borderRadius: 3.5, 
              py: 1, 
              px: 2.5,
              bgcolor: 'grey.900',
              fontWeight: 'bold',
              textTransform: 'none',
              '&:hover': { bgcolor: 'grey.800' }
            }}
          >
            Add Stock
          </Button>
        </Box>
      </Box>

      {/* Stats row */}
      {/* Stats row */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: '1fr 1fr 1fr 1fr' }, gap: 3 }}>
        <StatCard 
          title="Active Issues" 
          value={statsData?.getLibrarianStats.activeIssues.toString() || "0"} 
          color="#4f46e5" bg="#e0e7ff"
          icon={<BookIcon />} 
        />
        <StatCard 
          title="Overdue" 
          value={statsData?.getLibrarianStats.overdueIssues.toString() || "0"} 
          color="#e11d48" bg="#ffe4e6"
          icon={<WarningAmberIcon />} 
        />
        <StatCard 
          title="Returned (Today)" 
          value={statsData?.getLibrarianStats.returnedToday.toString() || "0"} 
          color="#10b981" bg="#d1fae5"
          icon={<CheckCircleIcon />} 
        />
        <StatCard 
          title="Reservations" 
          value={statsData?.getLibrarianStats.activeReservations.toString() || "0"} 
          color="#d97706" bg="#fef3c7"
          icon={<AccessTimeIcon />} 
        />
      </Box>

      {/* Split layout */}
      <Grid container spacing={4}>
        {/* Circulation Desk Actions */}
        <Grid item xs={12} lg={4} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5, pl: 0.5 }}>
              Circulation Desk
            </Typography>
            <ActionCard 
              title="Issue Book" 
              description="Record a new book loan to a member" 
              icon={LibraryAddIcon} 
              color="indigo" 
              onClick={() => setIsIssueModalOpen(true)}
            />
            <ActionCard 
              title="Confirm Return" 
              description="Process a returned book and update inventory" 
              icon={SyncIcon} 
              color="emerald" 
              onClick={() => setIsReturnModalOpen(true)}
            />
          </Box>
          
          <Alert 
            severity="info" 
            icon={<HistoryIcon fontSize="small" />}
            sx={{ 
              borderRadius: 4.5, 
              bgcolor: 'primary.lighter', 
              color: 'primary.dark',
              border: '1px solid',
              borderColor: 'primary.light',
              p: 2.5,
              '& .MuiAlert-icon': { color: 'primary.main', display: 'flex', alignItems: 'center' }
            }}
          >
            <Typography variant="caption" fontWeight="black" sx={{ textTransform: 'uppercase', display: 'block', mb: 0.5, letterSpacing: 0.5 }}>
              Quick Tip
            </Typography>
            <Typography variant="body2" sx={{ fontSize: 13, fontWeight: 500, lineHeight: 1.45 }}>
              Always verify the book condition before confirming a return to maintain accurate resource tracking.
            </Typography>
          </Alert>
        </Grid>

        {/* Live activity log */}
        <Grid item xs={12} lg={8}>
          <Card sx={{ borderRadius: 4, border: '1px solid', borderColor: 'divider', boxShadow: 'none', overflow: 'hidden' }}>
            <Box sx={{ px: 3, py: 2.5, display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'grey.50' }}>
              <Typography variant="subtitle2" fontWeight={850} sx={{ textTransform: 'uppercase', letterSpacing: 0.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                <HistoryIcon color="primary" fontSize="small" /> Recent Circulation
              </Typography>
              <Chip label="Live Log" size="small" color="primary" sx={{ fontWeight: 800, borderRadius: 2, fontSize: 10 }} />
            </Box>

            <TableContainer>
              <Table sx={{ minWidth: 600 }}>
                <TableHead sx={{ bgcolor: 'grey.50' }}>
                  <TableRow sx={{ '& th': { fontSize: 10, fontWeight: 'black', color: 'text.secondary', textTransform: 'uppercase', letterSpacing: 1.2 } }}>
                    <TableCell sx={{ pl: 4 }}>Book</TableCell>
                    <TableCell>Member</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Due Date</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody sx={{ fontFamily: 'monospace' }}>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={4} align="center" sx={{ py: 6 }}>
                        <CircularProgress size={24} />
                      </TableCell>
                    </TableRow>
                  ) : error ? (
                    <TableRow>
                      <TableCell colSpan={4} align="center" sx={{ py: 6, color: 'error.main', fontStyle: 'italic' }}>
                        Failed to load activity log.
                      </TableCell>
                    </TableRow>
                  ) : data?.getDueDates.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={4} align="center" sx={{ py: 6, fontStyle: 'italic', color: 'text.secondary' }}>
                        No recent activity found.
                      </TableCell>
                    </TableRow>
                  ) : (
                    data?.getDueDates.map((issue) => {
                      const book = booksData?.getAllBooks.find((b) => b.id === issue.bookId);
                      const member = membersData?.getStudentList.find((s) => s.id === issue.userId);
                      return (
                        <TableRow key={issue.id} sx={{ '&:hover': { bgcolor: 'grey.50' } }}>
                          <TableCell sx={{ pl: 4, py: 2 }}>
                            <Typography variant="body2" fontWeight="bold" color="text.primary" sx={{ fontFamily: 'sans-serif', fontSize: 13 }}>
                              {book?.title || `Book #${issue.bookId}`}
                            </Typography>
                            <Typography variant="caption" color="text.disabled" sx={{ display: 'block', fontSize: 10 }}>
                              ID: {issue.id.substring(0, 8)}...
                            </Typography>
                          </TableCell>
                          <TableCell sx={{ fontSize: 12, color: 'text.secondary', fontWeight: 500, fontFamily: 'sans-serif' }}>
                            {member?.fullName || `User #${issue.userId}`}
                          </TableCell>
                          <TableCell>
                            <Chip 
                              label={issue.state.type} 
                              size="small" 
                              color={
                                issue.state.type === 'RETURNED' ? 'success' : 
                                issue.state.type === 'OVERDUE' ? 'error' : 'warning'
                              }
                              sx={{ borderRadius: 1.5, fontWeight: 'bold', fontSize: 9, height: 18 }}
                            />
                          </TableCell>
                          <TableCell sx={{ fontSize: 12, color: 'text.primary', fontWeight: 'bold' }}>
                            {new Date(issue.dueDate).toLocaleDateString()}
                          </TableCell>
                        </TableRow>
                      );
                    })
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </Grid>
      </Grid>

      {/* Issue Book Modal */}
      <Modal
        isOpen={isIssueModalOpen}
        onClose={() => setIsIssueModalOpen(false)}
        title="Issue New Book"
        subtitle="Create a new lending record for a member"
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5 }}>
          <CustomSelect 
            label="Select Member"
            options={memberOptions}
            value={selectedMember}
            onChange={setSelectedMember}
            placeholder="Search for a member..."
          />
          <CustomSelect 
            label="Select Book"
            options={bookOptions}
            value={selectedBook}
            onChange={setSelectedBook}
            placeholder="Search for a book..."
          />
          <Button 
            onClick={handleIssue}
            disabled={!selectedMember || !selectedBook}
            variant="contained"
            sx={{ 
              py: 1.5, 
              borderRadius: 3.5, 
              bgcolor: 'primary.main', 
              color: 'white', 
              fontWeight: 'bold', 
              fontSize: 13,
              textTransform: 'none',
              boxShadow: '0 4px 14px rgba(99, 102, 241, 0.2)',
              '&:hover': { bgcolor: 'primary.dark' },
              '&:disabled': { bgcolor: 'action.disabledBackground', color: 'action.disabled' }
            }}
          >
            Issue Book
          </Button>
        </Box>
      </Modal>

      {/* Confirm Return Modal */}
      <Modal
        isOpen={isReturnModalOpen}
        onClose={() => setIsReturnModalOpen(false)}
        title="Confirm Return"
        subtitle="Process a book return and clear the active issue"
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5 }}>
          <CustomSelect 
            label="Select Member"
            options={memberOptions}
            value={selectedMember}
            onChange={(val) => {
              setSelectedMember(val);
              setSelectedIssueId('');
            }}
            placeholder="Search for a member..."
          />
          <CustomSelect 
            label="Active Issue"
            options={issueOptions}
            value={selectedIssueId}
            onChange={setSelectedIssueId}
            placeholder={selectedMember ? "Select an active loan..." : "Select a member first"}
          />
          <Button 
            onClick={handleReturn}
            disabled={!selectedMember || !selectedIssueId}
            variant="contained"
            color="success"
            sx={{ 
              py: 1.5, 
              borderRadius: 3.5, 
              fontWeight: 'bold', 
              fontSize: 13,
              textTransform: 'none',
              boxShadow: '0 4px 14px rgba(16, 185, 129, 0.2)',
              '&:hover': { bgcolor: 'success.dark' },
              '&:disabled': { bgcolor: 'action.disabledBackground', color: 'action.disabled' }
            }}
          >
            Confirm Return
          </Button>
        </Box>
      </Modal>
    </Box>
  );
};
