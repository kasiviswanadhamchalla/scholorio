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
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Divider
} from '@mui/material';
import Building2Icon from '@mui/icons-material/Business';
import AddIcon from '@mui/icons-material/Add';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import GroupIcon from '@mui/icons-material/Group';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import { Modal } from '../../components/Modal';
import { CustomSelect } from '../../components/CustomSelect';













export const DepartmentsManagement = () => {
  const [isDeptModalOpen, setIsDeptModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isLinkModalOpen, setIsLinkModalOpen] = useState(false);
  
  const [deptName, setDeptName] = useState('');
  const [deptCode, setDeptCode] = useState('');
  const [selectedDeptId, setSelectedDeptId] = useState('');
  
  const [selectedFaculty, setSelectedFaculty] = useState('');
  const [selectedDept, setSelectedDept] = useState('');

  const { data: deptData, loading: deptLoading, refetch: refetchDepts } = useRestQuery('/api/member/departments', 'getDepartments');
  const { data: facultyData, loading: facultyLoading, refetch: refetchFaculty } = useRestQuery('/api/member/users', 'getFacultyList');

  const [createDept] = useRestMutation('/api/member/departments', 'POST', 'createDepartment');
  const [updateDept] = useRestMutation((v) => `/api/member/departments/${v.id}`, 'PUT', 'updateDepartment');
  const [deleteDept] = useRestMutation((v) => `/api/member/departments/${v.id}`, 'DELETE', 'deleteDepartment');
  const [linkFaculty] = useRestMutation((v) => `/api/member/users/${v.facultyId}/link-department?departmentId=${v.departmentId}`, 'POST', 'linkFacultyToDepartment');

  const handleCreateDept = async () => {
    if (!deptName || !deptCode) return;
    try {
      await createDept({ variables: { input: { name: deptName, code: deptCode } } });
      setIsDeptModalOpen(false);
      setDeptName('');
      setDeptCode('');
      refetchDepts();
    } catch (err) {
      console.error(err);
    }
  };

  const handleEditDept = (dept) => {
    setSelectedDeptId(dept.id);
    setDeptName(dept.name);
    setDeptCode(dept.code);
    setIsEditModalOpen(true);
  };

  const handleUpdateDept = async () => {
    if (!deptName || !deptCode || !selectedDeptId) return;
    try {
      await updateDept({ 
        variables: { 
          id: selectedDeptId, 
          input: { name: deptName, code: deptCode } 
        } 
      });
      setIsEditModalOpen(false);
      setDeptName('');
      setDeptCode('');
      setSelectedDeptId('');
      refetchDepts();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDeleteDept = async (id) => {
    if (!window.confirm('Are you sure you want to decommission this department? All faculty links will be severed.')) return;
    try {
      await deleteDept({ variables: { id } });
      refetchDepts();
    } catch (err) {
      console.error(err);
    }
  };

  const handleLinkFaculty = async () => {
    if (!selectedFaculty || !selectedDept) return;
    try {
      await linkFaculty({ variables: { facultyId: selectedFaculty, departmentId: selectedDept } });
      setIsLinkModalOpen(false);
      setSelectedFaculty('');
      setSelectedDept('');
      refetchFaculty();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexDirection: { xs: 'column', sm: 'row' }, gap: 2 }}>
        <Box sx={{ alignSelf: 'flex-start' }}>
          <Typography variant="h4" fontWeight={900} color="text.primary" sx={{ letterSpacing: -1, textTransform: 'uppercase', fontFamily: 'Outfit, sans-serif' }}>
            Department Registry
          </Typography>
          <Typography variant="body2" color="text.secondary" fontWeight={500}>
            Manage academic units and faculty assignments
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1.5, width: { xs: '100%', sm: 'auto' } }}>
          <Button 
            variant="outlined" 
            startIcon={<PersonAddIcon />}
            onClick={() => setIsLinkModalOpen(true)}
            sx={{ 
              borderRadius: 2, 
              py: 1.25, 
              px: 2.5, 
              borderColor: 'divider', 
              color: 'text.secondary',
              fontWeight: 'bold', 
              textTransform: 'none', 
              fontSize: 11,
              '&:hover': { borderColor: 'primary.main', bgcolor: 'transparent' }
            }}
          >
            Assign Faculty
          </Button>
          <Button 
            variant="contained" 
            startIcon={<AddIcon />}
            onClick={() => setIsDeptModalOpen(true)}
            sx={{ 
              borderRadius: 2, 
              py: 1.25, 
              px: 2.5, 
              bgcolor: 'grey.900', 
              color: 'white',
              fontWeight: 'bold', 
              textTransform: 'none', 
              fontSize: 11,
              '&:hover': { bgcolor: 'grey.800' }
            }}
          >
            New Department
          </Button>
        </Box>
      </Box>

      {/* Main Grid Split */}
      <Grid container spacing={4}>
        {/* Table of Departments */}
        <Grid item xs={12} md={8}>
          <Card sx={{ borderRadius: 2, border: '1px solid', borderColor: 'divider', boxShadow: 'none', overflow: 'hidden' }}>
            <Box sx={{ px: 3, py: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'grey.50' }}>
              <Typography variant="subtitle2" fontWeight={850} sx={{ textTransform: 'uppercase', letterSpacing: 0.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                <Building2Icon color="action" fontSize="small" /> Registered Departments
              </Typography>
              <Chip 
                label={`${deptData?.getDepartments.length || 0} Total`} 
                size="small" 
                sx={{ fontWeight: 'black', fontSize: 10, borderRadius: 1 }} 
              />
            </Box>
            
            <TableContainer>
              <Table sx={{ minWidth: 500 }}>
                <TableHead sx={{ bgcolor: 'grey.50' }}>
                  <TableRow sx={{ '& th': { fontSize: 10, fontWeight: 'black', color: 'text.secondary', textTransform: 'uppercase', letterSpacing: 1.2 } }}>
                    <TableCell sx={{ pl: 4 }}>Name</TableCell>
                    <TableCell>Code</TableCell>
                    <TableCell>Faculty Count</TableCell>
                    <TableCell align="right" sx={{ pr: 4 }}>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody sx={{ fontFamily: 'monospace' }}>
                  {deptLoading ? (
                    <TableRow>
                      <TableCell colSpan={4} align="center" sx={{ py: 6 }}>
                        <CircularProgress size={24} />
                      </TableCell>
                    </TableRow>
                  ) : deptData?.getDepartments.map((dept) => (
                    <TableRow key={dept.id} sx={{ '&:hover': { bgcolor: 'grey.50' } }}>
                      <TableCell sx={{ pl: 4, py: 2, fontWeight: 'bold', color: 'text.primary', fontFamily: 'sans-serif', fontSize: 13 }}>
                        {dept.name}
                      </TableCell>
                      <TableCell sx={{ fontSize: 12, color: 'text.secondary', fontWeight: 500 }}>{dept.code}</TableCell>
                      <TableCell sx={{ fontSize: 12, color: 'text.secondary', fontWeight: 500 }}>
                        {facultyData?.getFacultyList.filter((f) => f.department?.id === dept.id).length || 0}
                      </TableCell>
                      <TableCell align="right" sx={{ pr: 4 }}>
                        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1.5 }}>
                          <Button 
                            onClick={() => handleEditDept(dept)}
                            sx={{ 
                              fontSize: 10, 
                              fontWeight: 'black', 
                              textTransform: 'uppercase',
                              p: 0,
                              minWidth: 0,
                              color: 'primary.main',
                              '&:hover': { bgcolor: 'transparent', textDecoration: 'underline' }
                            }}
                          >
                            Edit
                          </Button>
                          <Button 
                            onClick={() => handleDeleteDept(dept.id)}
                            sx={{ 
                              fontSize: 10, 
                              fontWeight: 'black', 
                              textTransform: 'uppercase',
                              p: 0,
                              minWidth: 0,
                              color: 'error.main',
                              '&:hover': { bgcolor: 'transparent', textDecoration: 'underline' }
                            }}
                          >
                            Delete
                          </Button>
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </Grid>

        {/* Faculty Directory Panel */}
        <Grid item xs={12} md={4}>
          <Card sx={{ borderRadius: 2, border: '1px solid', borderColor: 'divider', boxShadow: 'none' }}>
            <Box sx={{ px: 3, py: 2, display: 'flex', alignItems: 'center', gap: 1, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'grey.50' }}>
              <GroupIcon color="action" fontSize="small" />
              <Typography variant="subtitle2" fontWeight={850} sx={{ textTransform: 'uppercase', letterSpacing: 0.5 }}>
                Faculty Directory
              </Typography>
            </Box>
            
            <CardContent sx={{ p: 2 }}>
              {facultyLoading ? (
                <Box sx={{ py: 6, display: 'flex', justifyContent: 'center' }}>
                  <CircularProgress size={20} />
                </Box>
              ) : (
                <List sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                  {facultyData?.getFacultyList.map((faculty) => (
                    <Box 
                      key={faculty.id} 
                      sx={{ 
                        p: 2, 
                        border: '1px solid', 
                        borderColor: 'grey.100', 
                        borderRadius: 1.5, 
                        bgcolor: 'grey.50',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 1
                      }}
                    >
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 1 }}>
                        <Box sx={{ minWidth: 0 }}>
                          <Typography variant="body2" fontWeight="bold" noWrap>
                            {faculty.fullName}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            @{faculty.username}
                          </Typography>
                        </Box>
                        {faculty.department ? (
                          <Chip 
                            label={faculty.department.name} 
                            size="small" 
                            color="success" 
                            variant="outlined"
                            sx={{ fontWeight: 'black', borderRadius: 1, fontSize: 8, height: 16 }} 
                          />
                        ) : (
                          <Chip 
                            label="Unassigned" 
                            size="small" 
                            color="error" 
                            variant="outlined"
                            sx={{ fontWeight: 'black', borderRadius: 1, fontSize: 8, height: 16 }} 
                          />
                        )}
                      </Box>
                    </Box>
                  ))}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Provision Dept Modal */}
      <Modal 
        isOpen={isDeptModalOpen} 
        onClose={() => setIsDeptModalOpen(false)} 
        title="Provision Department" 
        subtitle="Register a new academic unit"
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5 }}>
              Department Name
            </Typography>
            <TextField 
              fullWidth
              value={deptName}
              onChange={(e) => setDeptName(e.target.value)}
              placeholder="e.g. Physics"
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 1.5, bgcolor: 'grey.50', fontWeight: 600, fontSize: 14 } }}
            />
          </Box>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5 }}>
              Registry Code
            </Typography>
            <TextField 
              fullWidth
              value={deptCode}
              onChange={(e) => setDeptCode(e.target.value)}
              placeholder="e.g. PHY_01"
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 1.5, bgcolor: 'grey.50', fontWeight: 600, fontSize: 14, fontFamily: 'monospace' } }}
            />
          </Box>
          <Button 
            onClick={handleCreateDept}
            disabled={!deptName || !deptCode}
            variant="contained"
            sx={{ py: 1.5, borderRadius: 2, bgcolor: 'grey.900', color: 'white', fontWeight: 'bold', fontSize: 12, textTransform: 'uppercase', letterSpacing: 1.2 }}
          >
            Create Department
          </Button>
        </Box>
      </Modal>

      {/* Edit Dept Modal */}
      <Modal 
        isOpen={isEditModalOpen} 
        onClose={() => {
          setIsEditModalOpen(false);
          setDeptName('');
          setDeptCode('');
          setSelectedDeptId('');
        }} 
        title="Update Department" 
        subtitle="Modify unit registry data"
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5 }}>
              Department Name
            </Typography>
            <TextField 
              fullWidth
              value={deptName}
              onChange={(e) => setDeptName(e.target.value)}
              placeholder="e.g. Physics"
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 1.5, bgcolor: 'grey.50', fontWeight: 600, fontSize: 14 } }}
            />
          </Box>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.5 }}>
              Registry Code
            </Typography>
            <TextField 
              fullWidth
              value={deptCode}
              onChange={(e) => setDeptCode(e.target.value)}
              placeholder="e.g. PHY_01"
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 1.5, bgcolor: 'grey.50', fontWeight: 600, fontSize: 14, fontFamily: 'monospace' } }}
            />
          </Box>
          <Button 
            onClick={handleUpdateDept}
            disabled={!deptName || !deptCode}
            variant="contained"
            sx={{ py: 1.5, borderRadius: 2, bgcolor: 'primary.main', color: 'white', fontWeight: 'bold', fontSize: 12, textTransform: 'uppercase', letterSpacing: 1.2 }}
          >
            Update Department
          </Button>
        </Box>
      </Modal>

      {/* Assign Faculty Modal */}
      <Modal 
        isOpen={isLinkModalOpen} 
        onClose={() => setIsLinkModalOpen(false)} 
        title="Assign Faculty" 
        subtitle="Link a faculty member to a department"
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5 }}>
          <CustomSelect 
            label="Faculty Member"
            options={facultyData?.getFacultyList.map((f) => ({ id: f.id, name: `${f.fullName} (@${f.username})` })) || []}
            value={selectedFaculty}
            onChange={setSelectedFaculty}
            placeholder="Select faculty..."
          />
          <CustomSelect 
            label="Target Department"
            options={deptData?.getDepartments.map((d) => ({ id: d.id, name: d.name })) || []}
            value={selectedDept}
            onChange={setSelectedDept}
            placeholder="Select department..."
          />
          <Button 
            onClick={handleLinkFaculty}
            disabled={!selectedFaculty || !selectedDept}
            variant="contained"
            sx={{ py: 1.5, borderRadius: 2, bgcolor: 'primary.main', color: 'white', fontWeight: 'bold', fontSize: 12, textTransform: 'uppercase', letterSpacing: 1.2 }}
          >
            Confirm Assignment
          </Button>
        </Box>
      </Modal>
    </Box>
  );
};
