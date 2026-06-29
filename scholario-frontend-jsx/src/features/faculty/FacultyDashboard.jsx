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
  TextField, 
  CircularProgress,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Chip,
  Alert
} from '@mui/material';
import BookIcon from '@mui/icons-material/Book';
import SchoolIcon from '@mui/icons-material/School';
import StarIcon from '@mui/icons-material/Star';
import PeopleIcon from '@mui/icons-material/People';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import WarningIcon from '@mui/icons-material/Warning';
import SendIcon from '@mui/icons-material/Send';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import DashboardIcon from '@mui/icons-material/Dashboard';
import RefreshIcon from '@mui/icons-material/Refresh';
import { Modal } from '../../components/Modal';
import { CustomSelect } from '../../components/CustomSelect';









const StatCard = ({ icon: Icon, label, value, color, bg }) => (
  <Card sx={{ borderRadius: 2, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none' }}>
    <CardContent sx={{ p: 3, display: 'flex', alignItems: 'center', gap: 2.5, '&:last-child': { pb: 3 } }}>
      <Box sx={{ p: 1.5, borderRadius: 1.5, bgcolor: bg, color: color, display: 'flex' }}>
        <Icon sx={{ fontSize: 28 }} />
      </Box>
      <Box>
        <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
          {label}
        </Typography>
        <Typography variant="h5" fontWeight={900} color="text.primary" sx={{ mt: 0.25, letterSpacing: -0.5 }}>
          {value}
        </Typography>
      </Box>
    </CardContent>
  </Card>
);

export const FacultyDashboard = () => {
  const navigate = useNavigate();
  const { data: profileData } = useRestQuery('/api/member/profile', 'getMyProfile');
  const facultyId = profileData?.getMyProfile?.id;

  const { data: statsData, loading: statsLoading } = useRestQuery('/api/member/dashboard', 'getFacultyStats', {
    variables: { facultyId },
    skip: !facultyId
  });

  const { data: deptData, loading: deptLoading } = useRestQuery('/api/member/departments', 'getDepartments');
  const [createBook] = useRestMutation('/api/catalog', 'POST', 'createBook', {
    refetchQueries: ['GetFacultyStats'],
  });

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [isbn, setIsbn] = useState('');
  const [description, setDescription] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleCreateBook = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await createBook({
        variables: {
          input: { title, isbn, description }
        }
      });
      setIsModalOpen(false);
      setTitle('');
      setIsbn('');
      setDescription('');
      navigate('/faculty/books');
    } catch (err) {
      console.error('Error creating book:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  const bookCount = statsData?.getBooksByFaculty.length || 0;
  const courseCount = statsData?.getCoursesByFaculty.length || 0;
  const publishedCount = statsData?.getBooksByFaculty.filter((b) => b.state.type === 'PUBLISHED').length || 0;
  const studentImpact = statsData?.getFacultyPerformance?.totalStudentEngagement || 0;
  const notifications = statsData?.getMyNotifications || [];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'flex-end' }, gap: 2 }}>
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 0.5 }}>
            <Box sx={{ p: 1, bgcolor: 'primary.main', color: 'white', borderRadius: 1.5, display: 'flex' }}>
              <DashboardIcon sx={{ fontSize: 20 }} />
            </Box>
            <Typography variant="h5" fontWeight={900} color="text.primary" sx={{ letterSpacing: -0.5, textTransform: 'uppercase', fontFamily: 'Outfit, sans-serif' }}>
              Academic Command
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary" fontWeight="bold" sx={{ fontFamily: 'monospace', fontSize: 11 }}>
            FACULTY_ID: {facultyId?.substring(0, 8) || 'SYNCING'} // PUBLICATION_OVERWATCH
          </Typography>
        </Box>
        
        <Button 
          variant="contained" 
          startIcon={<AddCircleIcon />}
          onClick={() => setIsModalOpen(true)}
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
          Draft New Resource
        </Button>
      </Box>

      {/* Stats row */}
      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard icon={BookIcon} label="Authored Books" value={statsLoading ? '...' : bookCount} color="#2563eb" bg="#eff6ff" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard icon={SchoolIcon} label="Active Courses" value={statsLoading ? '...' : courseCount} color="#4f46e5" bg="#f5f3ff" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard icon={StarIcon} label="Published Works" value={statsLoading ? '...' : publishedCount} color="#059669" bg="#ecfdf5" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard icon={PeopleIcon} label="Student Impact" value={statsLoading ? '...' : studentImpact} color="#d97706" bg="#fef3c7" />
        </Grid>
      </Grid>

      {/* Bottom Main Content split */}
      <Grid container spacing={4}>
        {/* Activity log */}
        <Grid item xs={12} lg={8}>
          <Card sx={{ borderRadius: 2, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none' }}>
            <Box sx={{ p: 3, borderBottom: '1px solid', borderColor: 'grey.50', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Typography variant="subtitle2" fontWeight={800} sx={{ textTransform: 'uppercase', letterSpacing: 1.2, display: 'flex', alignItems: 'center', gap: 1 }}>
                <AccessTimeIcon color="primary" fontSize="small" /> Recent Activity Registry
              </Typography>
              <Button 
                variant="text" 
                sx={{ 
                  color: 'text.secondary', 
                  fontSize: 10, 
                  fontWeight: 800, 
                  letterSpacing: 1.2,
                  '&:hover': { bgcolor: 'transparent', color: 'text.primary' }
                }}
              >
                View All Archive
              </Button>
            </Box>
            
            <Box sx={{ p: 2.5 }}>
              {notifications.length === 0 ? (
                <Box sx={{ py: 6, textAlign: 'center', bgcolor: 'grey.50', border: '1px dashed', borderColor: 'divider', borderRadius: 1.5 }}>
                  <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
                    No recent activity detected
                  </Typography>
                </Box>
              ) : (
                <List disablePadding>
                  {notifications.slice(0, 5).map((notif, idx) => (
                    <ListItem 
                      key={notif.id} 
                      disableGutters
                      sx={{ 
                        mb: idx === notifications.length - 1 ? 0 : 1.5,
                        '&:last-child': { mb: 0 }
                      }}
                    >
                      <ListItemButton 
                        sx={{ 
                          p: 2, 
                          borderRadius: 2, 
                          bgcolor: 'grey.50', 
                          border: '1px solid', 
                          borderColor: 'grey.100',
                          '&:hover': {
                            bgcolor: 'white',
                            borderColor: 'primary.lighter',
                          }
                        }}
                      >
                        <ListItemIcon sx={{ minWidth: 40 }}>
                          <Box sx={{ p: 1, borderRadius: 1.5, bgcolor: 'white', border: '1px solid', borderColor: 'grey.200', color: 'text.secondary', display: 'flex' }}>
                            {notif.type === 'BOOK' ? <BookIcon fontSize="small" /> : <WarningIcon fontSize="small" />}
                          </Box>
                        </ListItemIcon>
                        <ListItemText 
                          primary={notif.message}
                          primaryTypographyProps={{ fontSize: 13, fontWeight: 'bold', color: 'text.primary' }}
                          secondary={`Type: ${notif.type} // ${new Date(notif.createdAt).toLocaleTimeString()}`}
                          secondaryTypographyProps={{ fontSize: 10, fontWeight: 'bold', color: 'text.disabled', sx: { fontStyle: 'monospace', textTransform: 'uppercase', mt: 0.5 } }}
                        />
                        <ChevronRightIcon sx={{ color: 'text.disabled' }} />
                      </ListItemButton>
                    </ListItem>
                  ))}
                </List>
              )}
            </Box>
          </Card>
        </Grid>

        {/* Right side links */}
        <Grid item xs={12} lg={4} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Card sx={{ bgcolor: 'grey.900', color: 'white', borderRadius: 2, p: 1 }}>
            <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
              <Typography variant="caption" fontWeight="bold" sx={{ display: 'block', color: 'grey.500', textTransform: 'uppercase', letterSpacing: 1.5, mb: 2 }}>
                Infrastructure Hub
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                {[
                  { label: 'Publication Engine', to: '/faculty/books' },
                  { label: 'Curriculum Master', to: '/faculty/courses' },
                  { label: 'Identity Matrix', to: '/faculty/settings' },
                ].map((hubLink) => (
                  <Button
                    key={hubLink.to}
                    onClick={() => navigate(hubLink.to)}
                    fullWidth
                    endIcon={<ChevronRightIcon sx={{ color: 'grey.600' }} />}
                    sx={{
                      justifyContent: 'space-between',
                      textTransform: 'uppercase',
                      textAlign: 'left',
                      py: 2,
                      px: 2.5,
                      bgcolor: 'grey.800',
                      borderRadius: 2,
                      color: 'grey.300',
                      fontWeight: 700,
                      fontSize: 11,
                      letterSpacing: 1.2,
                      '&:hover': {
                        bgcolor: 'grey.750',
                        color: 'white',
                        '& .MuiSvgIcon-root': { color: 'primary.light' }
                      }
                    }}
                  >
                    {hubLink.label}
                  </Button>
                ))}
              </Box>
            </CardContent>
          </Card>

          <Alert 
            severity="info" 
            icon={<WarningIcon fontSize="small" />}
            sx={{ 
              borderRadius: 2, 
              bgcolor: 'primary.lighter', 
              color: 'primary.dark',
              border: '1px solid',
              borderColor: 'primary.light',
              '& .MuiAlert-icon': { color: 'primary.main', display: 'flex', alignItems: 'center' }
            }}
          >
            <Typography variant="caption" fontWeight="black" sx={{ textTransform: 'uppercase', display: 'block', mb: 0.5, letterSpacing: 0.5 }}>
              Policy Alert
            </Typography>
            <Typography variant="body2" sx={{ fontSize: 11, fontWeight: 500, lineHeight: 1.4 }}>
              All academic resources must undergo peer-review before global publication.
            </Typography>
          </Alert>
        </Grid>
      </Grid>

      {/* Modal for drafting book */}
      <Modal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
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
                  borderRadius: 1.5,
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
                  borderRadius: 1.5,
                  bgcolor: 'grey.50',
                  fontWeight: 500,
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
              borderRadius: 2,
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
