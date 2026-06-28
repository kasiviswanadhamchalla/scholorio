import { useRestQuery, useRestMutation } from '../../hooks/useRest';
import React, { useState } from 'react';


import { 
  Box, 
  Typography, 
  Grid, 
  Card, 
  CardContent, 
  Button, 
  TextField, 
  CircularProgress,
  IconButton,
  Alert
} from '@mui/material';
import SchoolIcon from '@mui/icons-material/School';
import AddIcon from '@mui/icons-material/Add';
import BookIcon from '@mui/icons-material/Book';
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined';
import LinkIcon from '@mui/icons-material/Link';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { Modal } from '../../components/Modal';
import { CustomSelect } from '../../components/CustomSelect';











export const CourseManagement = () => {
  const [isCourseModalOpen, setIsCourseModalOpen] = useState(false);
  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);
  const [selectedCourseId, setSelectedCourseId] = useState(null);

  // Form states for new course
  const [courseCode, setCourseCode] = useState('');
  const [courseTitle, setCourseTitle] = useState('');
  const [courseDesc, setCourseDesc] = useState('');

  // Form states for assigning book
  const [selectedBookId, setSelectedBookId] = useState('');

  const { data: profileData } = useRestQuery('/api/member/profile', 'getMyProfile');
  const facultyId = profileData?.getMyProfile?.id;

  const { data: coursesData, loading: coursesLoading, refetch: refetchCourses } = useRestQuery('/api/member/dashboard', 'getCoursesByFaculty', {
    variables: { facultyId },
    skip: !facultyId
  });

  const { data: booksData } = useRestQuery('/api/catalog', 'getAllBooks');

  const [createCourse] = useRestMutation('/api/member/dashboard', 'POST', 'createCourse');
  const [assignBook] = useRestMutation('/api/member/dashboard', 'POST', 'assignBookToCourse');

  const handleCreateCourse = async () => {
    if (!courseCode || !courseTitle) return;
    try {
      await createCourse({
        variables: {
          input: {
            courseCode,
            title: courseTitle,
            description: courseDesc,
            facultyId
          }
        }
      });
      setIsCourseModalOpen(false);
      setCourseCode('');
      setCourseTitle('');
      setCourseDesc('');
      refetchCourses();
    } catch (err) {
      console.error(err);
    }
  };

  const handleAssignBook = async () => {
    if (!selectedCourseId || !selectedBookId) return;
    try {
      await assignBook({
        variables: {
          input: {
            courseId: selectedCourseId,
            bookId: selectedBookId,
            mandatory: true
          }
        }
      });
      setIsAssignModalOpen(false);
      setSelectedBookId('');
      alert('Book assigned to course successfully');
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h4" fontWeight={900} color="text.primary" sx={{ letterSpacing: -1, textTransform: 'uppercase', fontFamily: 'Outfit, sans-serif' }}>
            Academic Courses
          </Typography>
          <Typography variant="body2" color="text.secondary" fontWeight={500}>
            Manage your courses and learning materials
          </Typography>
        </Box>
        <Button 
          variant="contained" 
          startIcon={<AddIcon />}
          onClick={() => setIsCourseModalOpen(true)}
          sx={{ 
            borderRadius: 3.5, 
            py: 1.25, 
            px: 3, 
            bgcolor: 'primary.main', 
            color: 'white',
            fontWeight: 'bold',
            textTransform: 'none',
            fontSize: 12,
            boxShadow: '0 4px 14px rgba(99, 102, 241, 0.2)',
            '&:hover': { bgcolor: 'primary.dark' }
          }}
        >
          Initialize Course
        </Button>
      </Box>

      {/* Grid of Courses */}
      <Grid container spacing={3}>
        {coursesLoading ? (
          <Grid item xs={12}>
            <Box sx={{ py: 10, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
              <CircularProgress size={40} />
              <Typography variant="body2" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
                Syncing Course Registry...
              </Typography>
            </Box>
          </Grid>
        ) : coursesData?.getCoursesByFaculty.length === 0 ? (
          <Grid item xs={12}>
            <Box sx={{ py: 10, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, border: '2px dashed', borderColor: 'divider', borderRadius: 5, bgcolor: 'white', color: 'text.disabled' }}>
              <SchoolIcon sx={{ fontSize: 48 }} />
              <Typography variant="body2" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 1 }}>
                No active courses registered
              </Typography>
            </Box>
          </Grid>
        ) : (
          coursesData?.getCoursesByFaculty.map((course) => (
            <Grid item xs={12} sm={6} md={4} key={course.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', borderRadius: 4, border: '1px solid', borderColor: 'grey.100', boxShadow: 'none', transition: 'all 0.2s', '&:hover': { borderColor: 'primary.lighter', boxShadow: 2 } }}>
                <CardContent sx={{ p: 3, flexGrow: 1, display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <Chip 
                      label={course.courseCode} 
                      size="small" 
                      sx={{ 
                        borderRadius: 1, 
                        fontWeight: 'black', 
                        bgcolor: 'grey.900', 
                        color: 'white',
                        fontSize: 9,
                        height: 20
                      }} 
                    />
                    <IconButton size="small" sx={{ color: 'text.secondary', '&:hover': { color: 'error.main' } }}>
                      <DeleteOutlinedIcon sx={{ fontSize: 18 }} />
                    </IconButton>
                  </Box>
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="h6" fontWeight="bold" color="text.primary" sx={{ letterSpacing: -0.5, lineHeight: 1.25, mb: 1, textTransform: 'uppercase' }}>
                      {course.title}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" fontWeight={500} sx={{ lineClamp: 2, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden', lineHeight: 1.5 }}>
                      {course.description || 'No description provided.'}
                    </Typography>
                  </Box>
                </CardContent>
                <Box 
                  sx={{ 
                    p: 2, 
                    bgcolor: 'grey.50', 
                    borderTop: '1px solid', 
                    borderColor: 'grey.100',
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    borderBottomLeftRadius: 'inherit',
                    borderBottomRightRadius: 'inherit'
                  }}
                >
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'text.secondary' }}>
                    <BookIcon sx={{ fontSize: 16 }} />
                    <Typography variant="caption" fontWeight="bold" sx={{ textTransform: 'uppercase', letterSpacing: 1.1 }}>
                      Materials
                    </Typography>
                  </Box>
                  <Button 
                    size="small"
                    variant="text"
                    onClick={() => {
                      setSelectedCourseId(course.id);
                      setIsAssignModalOpen(true);
                    }}
                    startIcon={<LinkIcon sx={{ fontSize: 14 }} />}
                    sx={{ 
                      fontSize: 10, 
                      fontWeight: 'bold', 
                      textTransform: 'uppercase',
                      color: 'primary.main',
                      '&:hover': { bgcolor: 'transparent', textDecoration: 'underline' }
                    }}
                  >
                    Assign Book
                  </Button>
                </Box>
              </Card>
            </Grid>
          ))
        )}
      </Grid>

      {/* Create Course Modal */}
      <Modal 
        isOpen={isCourseModalOpen} 
        onClose={() => setIsCourseModalOpen(false)} 
        title="Initialize Course" 
        subtitle="Provision a new academic module in the global registry"
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5, pl: 0.5 }}>
              Course Code
            </Typography>
            <TextField 
              required
              fullWidth
              value={courseCode}
              onChange={(e) => setCourseCode(e.target.value)}
              placeholder="e.g. CS101"
              sx={{
                '& .MuiOutlinedInput-root': {
                  borderRadius: 3,
                  bgcolor: 'grey.50',
                  fontWeight: 600,
                  fontSize: 14,
                  fontFamily: 'monospace',
                  textTransform: 'uppercase',
                  '& fieldset': { borderColor: 'divider' },
                }
              }}
            />
          </Box>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5, pl: 0.5 }}>
              Course Title
            </Typography>
            <TextField 
              required
              fullWidth
              value={courseTitle}
              onChange={(e) => setCourseTitle(e.target.value)}
              placeholder="e.g. Introduction to Neural Networks"
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
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5, pl: 0.5 }}>
              Description
            </Typography>
            <TextField 
              fullWidth
              multiline
              rows={4}
              value={courseDesc}
              onChange={(e) => setCourseDesc(e.target.value)}
              placeholder="Module objectives and coverage..."
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
          <Button 
            fullWidth
            onClick={handleCreateCourse}
            disabled={!courseCode || !courseTitle}
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
            Finalize Module Registry
          </Button>
        </Box>
      </Modal>

      {/* Assign Book Modal */}
      <Modal 
        isOpen={isAssignModalOpen} 
        onClose={() => setIsAssignModalOpen(false)} 
        title="Link Materials" 
        subtitle="Associate published books with this course module"
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5 }}>
          <CustomSelect 
            label="Target Resource"
            options={booksData?.getAllBooks.map((b) => ({ id: b.id, name: `${b.title} (ISBN: ${b.isbn})` })) || []}
            value={selectedBookId}
            onChange={setSelectedBookId}
            placeholder="Search published books..."
          />
          
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
              Security Policy
            </Typography>
            <Typography variant="body2" sx={{ fontSize: 11, fontWeight: 500, lineHeight: 1.4 }}>
              Only published and validated resources should be assigned to active courses.
            </Typography>
          </Alert>

          <Button 
            fullWidth
            onClick={handleAssignBook}
            disabled={!selectedBookId}
            variant="contained"
            sx={{ 
              py: 1.5, 
              borderRadius: 3.5, 
              bgcolor: 'primary.main', 
              color: 'white',
              fontWeight: 'bold', 
              fontSize: 12,
              textTransform: 'uppercase',
              letterSpacing: 1.2,
              boxShadow: '0 4px 14px rgba(99, 102, 241, 0.2)',
              '&:hover': { bgcolor: 'primary.dark' }
            }}
          >
            Authorize Material Link
          </Button>
        </Box>
      </Modal>
    </Box>
  );
};
