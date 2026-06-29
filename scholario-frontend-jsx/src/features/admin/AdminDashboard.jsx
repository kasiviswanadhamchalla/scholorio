import { useRestQuery, useRestMutation } from '../../hooks/useRest';
import React, { useState } from 'react';


import { Link, useNavigate } from 'react-router-dom';
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
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Badge,
  Alert
} from '@mui/material';
import ShieldIcon from '@mui/icons-material/Shield';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import PeopleIcon from '@mui/icons-material/People';
import AnalyticsIcon from '@mui/icons-material/Analytics';
import LockIcon from '@mui/icons-material/Lock';
import WarningIcon from '@mui/icons-material/Warning';
import BusinessIcon from '@mui/icons-material/Business';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import LocalLibraryIcon from '@mui/icons-material/LocalLibrary';
import LockOpenIcon from '@mui/icons-material/LockOpen';
import PersonIcon from '@mui/icons-material/Person';
import { Modal } from '../../components/Modal';
import { CustomSelect } from '../../components/CustomSelect';









const parseDate = (d) => {
  if (!d) return 'N/A';
  if (Array.isArray(d)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = d;
    return new Date(year, month - 1, day, hour, minute, second).toLocaleDateString();
  }
  const date = new Date(d);
  return isNaN(date.getTime()) ? 'N/A' : date.toLocaleDateString();
};

const StatCard = ({ icon: Icon, label, value, color, bg }) => (
  <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', borderRadius: 2, border: '1px solid', borderColor: 'divider', boxShadow: 'none', width: '100%' }}>
    <CardContent sx={{ p: 2.5, display: 'flex', alignItems: 'center', gap: 2, flexGrow: 1, '&:last-child': { pb: 2.5 } }}>
      <Box sx={{ p: 1.5, borderRadius: 1.5, bgcolor: bg, color: color, display: 'flex', flexShrink: 0 }}>
        <Icon sx={{ fontSize: 24 }} />
      </Box>
      <Box sx={{ minWidth: 0, flexGrow: 1 }}>
        <Typography 
          variant="caption" 
          fontWeight="bold" 
          color="text.disabled" 
          sx={{ 
            textTransform: 'uppercase', 
            letterSpacing: 1, 
            display: 'block'
          }}
        >
          {label}
        </Typography>
        <Typography variant="h6" fontWeight="bold" color="text.primary" sx={{ mt: 0.25, letterSpacing: -0.5, lineHeight: 1.2 }}>
          {value}
        </Typography>
      </Box>
    </CardContent>
  </Card>
);

export const AdminDashboard = () => {
  const navigate = useNavigate();
  const [isRoleModalOpen, setIsRoleModalOpen] = useState(false);
  const [isLockdownModalOpen, setIsLockdownModalOpen] = useState(false);

  // Form states
  const [selectedUser, setSelectedUser] = useState('');
  const [selectedRole, setSelectedRole] = useState('');

  const { data: allUsersData, loading: usersLoading, error: usersError, refetch: refetchUsers } = useRestQuery('/api/member/users', 'getAllUsers');
  const { data: unassignedData, refetch: refetchUnassigned } = useRestQuery('/api/member/unassigned', 'getUnassignedUsers');

  const [assignRole] = useRestMutation((v) => `/api/member/users/${v.userId}/assign-role?role=${v.role}`, 'POST', 'assignRole');

  const handleAssignRole = async () => {
    if (!selectedUser || !selectedRole) return;
    try {
      await assignRole({ variables: { userId: selectedUser, role: selectedRole } });
      setIsRoleModalOpen(false);
      setSelectedUser('');
      setSelectedRole('');
      refetchUnassigned();
      refetchUsers();
      alert('Role assigned successfully');
    } catch (err) {
      console.error('Failed to assign role:', err);
    }
  };

  if (usersError) {
    return (
      <Alert 
        severity="error" 
        icon={<ShieldIcon sx={{ fontSize: 24 }} />}
        sx={{ 
          borderRadius: 2, 
          bgcolor: 'error.lighter', 
          color: 'error.dark',
          border: '1px solid',
          borderColor: 'error.light',
          '& .MuiAlert-icon': { color: 'error.main', display: 'flex', alignItems: 'center' }
        }}
      >
        <Typography variant="subtitle2" fontWeight="black" sx={{ textTransform: 'uppercase', display: 'block', mb: 0.5, letterSpacing: 0.5 }}>
          SYSTEM_ERROR_LINK_FAILURE
        </Typography>
        <Typography variant="body2" sx={{ fontSize: 13, fontWeight: 500 }}>
          Unable to synchronize with identity database. Member directory offline.
        </Typography>
      </Alert>
    );
  }

  const users = allUsersData?.getAllUsers || [];
  const totalUsers = users.length;
  const librarianCount = users.filter(u => u.roles.includes('LIBRARIAN') || u.roles.includes('ASSISTANT_LIBRARIAN')).length;
  const memberCount = users.filter(u => u.roles.includes('MEMBER')).length;
  const unassignedCount = users.filter(u => u.roles.includes('UNASSIGNED')).length;

  const userOptions = unassignedData?.getUnassignedUsers.map(u => ({ id: u.id, name: `${u.fullName} (@${u.username})` })) || [];
  const roleOptions = [
    { id: 'SUPER_ADMIN', name: 'Administrator' },
    { id: 'LIBRARIAN', name: 'Librarian' },
    { id: 'ASSISTANT_LIBRARIAN', name: 'Assistant Librarian' },
    { id: 'MEMBER', name: 'Member' }
  ];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      {/* Header */}
      <Card sx={{ borderRadius: 2, border: '1px solid', borderColor: 'divider', boxShadow: 'none' }}>
        <CardContent sx={{ p: { xs: 3, sm: 4 }, '&:last-child': { pb: { xs: 3, sm: 4 } }, display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box sx={{ p: 1.5, bgcolor: 'grey.900', color: 'white', borderRadius: 1.5, display: 'flex' }}>
              <AnalyticsIcon sx={{ fontSize: 24 }} />
            </Box>
            <Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Typography variant="h6" fontWeight="black" color="text.primary" sx={{ letterSpacing: -0.5, textTransform: 'uppercase', fontFamily: 'Outfit, sans-serif' }}>
                  System Control Hub
                </Typography>
                <Chip 
                  label="Live" 
                  size="small" 
                  color="success" 
                  sx={{ 
                    borderRadius: 1, 
                    fontWeight: 'black', 
                    fontSize: 9, 
                    height: 16,
                    px: 0.5,
                    '& .MuiChip-label': { px: 1}
                  }} 
                />
              </Box>
              {/* <Typography variant="body2" color="text.secondary" fontWeight="bold" sx={{ fontFamily: 'monospace', fontSize: 11, mt: 0.25 }}>
                Node ID: SCHOLARIO-PRD-01 // Global Oversight Mode
              </Typography> */}
            </Box>
          </Box>
          
          {/* <Button 
            variant="contained" 
            color="error"
            startIcon={<LockIcon />}
            onClick={() => setIsLockdownModalOpen(true)}
            sx={{ 
              borderRadius: 3.5, 
              py: 1.5, 
              px: 3, 
              fontWeight: 'bold',
              textTransform: 'uppercase',
              fontSize: 10,
              letterSpacing: 1.2,
              boxShadow: '0 4px 12px rgba(225, 29, 72, 0.2)',
              '&:hover': { bgcolor: 'error.dark' }
            }}
          >
            Emergency Lockdown
          </Button> */}
        </CardContent>
      </Card>

      {/* Metrics Row */}
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }} sx={{ display: 'flex' }}>
          <StatCard icon={PersonIcon} label="Total Registered Users" value={usersLoading ? '...' : totalUsers} color="#1e293b" bg="#f8fafc" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }} sx={{ display: 'flex' }}>
          <StatCard icon={LocalLibraryIcon} label="Librarians" value={usersLoading ? '...' : librarianCount} color="#15803d" bg="#f0fdf4" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }} sx={{ display: 'flex' }}>
          <StatCard icon={PeopleIcon} label="Members" value={usersLoading ? '...' : memberCount} color="#0284c7" bg="#e0f2fe" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }} sx={{ display: 'flex' }}>
          <StatCard icon={LockOpenIcon} label="Unassigned Registrations" value={usersLoading ? '...' : unassignedCount} color="#d97706" bg="#fef3c7" />
        </Grid>
      </Grid>

      {/* Split Views */}
      <Grid container spacing={4}>
        {/* Table of Registered Users */}
        <Grid size={{ xs: 12, lg: 8 }}>
          <Card sx={{ borderRadius: 2, border: '1px solid', borderColor: 'divider', boxShadow: 'none', overflow: 'hidden' }}>
            <Box sx={{ px: 3, py: 2.5, display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'grey.50' }}>
              <Typography variant="subtitle2" fontWeight={850} sx={{ textTransform: 'uppercase', letterSpacing: 0.5 }}>
                Registered User Directory
              </Typography>
            </Box>
            
            <TableContainer>
              <Table sx={{ minWidth: 600 }}>
                <TableHead sx={{ bgcolor: 'grey.50' }}>
                  <TableRow sx={{ '& th': { fontSize: 10, fontWeight: 'black', color: 'text.secondary', textTransform: 'uppercase', letterSpacing: 1.2 } }}>
                    <TableCell sx={{ pl: 4 }}>User Entity</TableCell>
                    <TableCell>Username</TableCell>
                    <TableCell>Authorization Tier</TableCell>
                    <TableCell>Registration Date</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody sx={{ fontFamily: 'monospace' }}>
                  {usersLoading ? (
                    <TableRow>
                      <TableCell colSpan={4} align="center" sx={{ py: 6 }}>
                        <CircularProgress size={24} />
                      </TableCell>
                    </TableRow>
                  ) : users.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={4} align="center" sx={{ py: 6, fontStyle: 'italic', color: 'text.secondary' }}>
                        No Registered Users Found
                      </TableCell>
                    </TableRow>
                  ) : users.map((u) => (
                    <TableRow key={u.id} sx={{ '&:hover': { bgcolor: 'grey.50' } }}>
                      <TableCell sx={{ pl: 4, py: 2 }}>
                        <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                          <Typography variant="body2" fontWeight="bold" color="text.primary" sx={{ fontFamily: 'sans-serif', fontSize: 13 }}>
                            {u.fullName}
                          </Typography>
                          <Typography variant="caption" color="text.disabled" sx={{ fontSize: 10, fontFamily: 'sans-serif' }}>
                            {u.email}
                          </Typography>
                        </Box>
                      </TableCell>
                      <TableCell sx={{ fontSize: 12, color: 'text.secondary', fontWeight: 500, fontFamily: 'sans-serif' }}>
                        @{u.username}
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                          {u.roles.map((role) => (
                            <Chip 
                              key={role}
                              label={role} 
                              size="small" 
                              color={role === 'SUPER_ADMIN' ? 'error' : role === 'LIBRARIAN' ? 'success' : role === 'MEMBER' ? 'primary' : 'default'}
                              sx={{ 
                                borderRadius: 1, 
                                fontWeight: 'black', 
                                fontSize: 8, 
                                height: 18,
                                textTransform: 'uppercase'
                              }} 
                            />
                          ))}
                        </Box>
                      </TableCell>
                      <TableCell sx={{ fontSize: 11, color: 'text.secondary', fontWeight: 500, fontFamily: 'sans-serif' }}>
                        {parseDate(u.createdAt)}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </Grid>

        {/* Side panels */}
        <Grid size={{ xs: 12, lg: 4 }} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Card sx={{ borderRadius: 2, border: '1px solid', borderColor: 'divider', boxShadow: 'none' }}>
            <Box sx={{ p: 3, borderBottom: '1px solid', borderColor: 'divider', display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <LockIcon color="action" fontSize="small" />
              <Typography variant="subtitle2" fontWeight={850} sx={{ textTransform: 'uppercase', letterSpacing: 0.5 }}>
                Infrastructure Master
              </Typography>
            </Box>
            
            <CardContent sx={{ p: 2.5, '&:last-child': { pb: 2.5 } }}>
              <List disablePadding sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                <ListItem disablePadding>
                  <ListItemButton 
                    onClick={() => navigate('/admin/departments')}
                    sx={{ 
                      p: 2, 
                      borderRadius: 2, 
                      border: '1px solid', 
                      borderColor: 'grey.100',
                      '&:hover': { borderColor: 'primary.main', bgcolor: 'transparent', '& .MuiSvgIcon-root': { color: 'primary.main' } } 
                    }}
                  >
                    <ListItemIcon sx={{ minWidth: 36, color: 'text.secondary' }}>
                      <BusinessIcon fontSize="small" />
                    </ListItemIcon>
                    <ListItemText 
                      primary={
                        <Typography sx={{ fontSize: 12, fontWeight: 'bold', textTransform: 'uppercase', letterSpacing: 0.5 }}>
                          Department Master
                        </Typography>
                      }
                      secondary={
                        <Typography variant="body2" sx={{ fontSize: 10, fontWeight: 500, color: 'text.secondary' }}>
                          Provision academic units
                        </Typography>
                      }
                    />
                    <ChevronRightIcon sx={{ color: 'text.disabled' }} />
                  </ListItemButton>
                </ListItem>
                <ListItem disablePadding>
                  <ListItemButton 
                    onClick={() => setIsRoleModalOpen(true)}
                    sx={{ 
                      p: 2, 
                      borderRadius: 2, 
                      border: '1px solid', 
                      borderColor: 'grey.100',
                      '&:hover': { borderColor: 'primary.main', bgcolor: 'transparent', '& .MuiSvgIcon-root': { color: 'primary.main' } } 
                    }}
                  >
                    <ListItemIcon sx={{ minWidth: 36, color: 'text.secondary' }}>
                      <VerifiedUserIcon fontSize="small" />
                    </ListItemIcon>
                    <ListItemText 
                      primary={
                        <Typography sx={{ fontSize: 12, fontWeight: 'bold', textTransform: 'uppercase', letterSpacing: 0.5 }}>
                          Role Authorization
                        </Typography>
                      }
                      secondary={
                        <Typography variant="body2" sx={{ fontSize: 10, fontWeight: 500, color: 'text.secondary' }}>
                          Verify node permissions
                        </Typography>
                      }
                    />
                    {unassignedData?.getUnassignedUsers.length > 0 ? (
                      <Badge 
                        badgeContent={unassignedData.getUnassignedUsers.length} 
                        color="error" 
                        sx={{ mr: 2, '& .MuiBadge-badge': { fontSize: 9, fontWeight: 'black', height: 16, minWidth: 16 } }} 
                      />
                    ) : null}
                    <ChevronRightIcon sx={{ color: 'text.disabled' }} />
                  </ListItemButton>
                </ListItem>
              </List>
            </CardContent>
          </Card>

          {/* <Card sx={{ bgcolor: 'grey.900', color: 'white', borderRadius: 2, p: 1 }}>
            <CardContent sx={{ p: 3, '&:last-child': { pb: 3 }, display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'error.light' }}>
                <ShieldIcon fontSize="small" />
                <Typography variant="caption" fontWeight="bold" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
                  Protocol Status
                </Typography>
              </Box>
              <Typography variant="body2" sx={{ fontFamily: 'monospace', color: 'grey.400', lineHeight: 1.6, fontSize: 12 }}>
                [ACTIVE] Encryption: AES-256-GCM<br/>
                [ACTIVE] Node Auth: OAuth 2.0<br/>
                [STABLE] Connection: Global Telemetry
              </Typography>
            </CardContent>
          </Card> */}
        </Grid>
      </Grid>

      {/* Role Authorization Modal */}
      <Modal 
        isOpen={isRoleModalOpen} 
        onClose={() => setIsRoleModalOpen(false)} 
        title="Role Authorization" 
        subtitle="Validate and assign permissions to unassigned users"
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5 }}>
          <CustomSelect 
            label="Pending Entity"
            options={userOptions}
            value={selectedUser}
            onChange={setSelectedUser}
            placeholder="Search pending nodes..."
          />
          <CustomSelect 
            label="Authorization Tier"
            options={roleOptions}
            value={selectedRole}
            onChange={setSelectedRole}
            placeholder="Select access level..."
          />
          <Button 
            fullWidth
            onClick={handleAssignRole}
            disabled={!selectedUser || !selectedRole}
            variant="contained"
            sx={{ 
              py: 1.5, 
              borderRadius: 2, 
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
            Authorize Access
          </Button>
        </Box>
      </Modal>

      {/* Lockdown Modal */}
      <Modal 
        isOpen={isLockdownModalOpen} 
        onClose={() => setIsLockdownModalOpen(false)} 
        title="CRITICAL: System Lockdown" 
        subtitle="Executing this protocol will sever all active user connections"
      >
        <Box sx={{ textAlign: 'center', display: 'flex', flexDirection: 'column', gap: 3, py: 2 }}>
          <Box sx={{ p: 2, bgcolor: 'error.lighter', color: 'error.main', borderRadius: '50%', display: 'inline-flex', alignSelf: 'center' }}>
            <WarningAmberIcon sx={{ fontSize: 40 }} />
          </Box>
          <Typography variant="body2" color="text.secondary" fontWeight={500} sx={{ px: 2 }}>
            You are about to initiate a <strong>Global System Lockdown</strong>. This action is irreversible via standard protocols and will require manual infrastructure restart.
          </Typography>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid size={{ xs: 6 }}>
              <Button 
                fullWidth
                variant="outlined"
                onClick={() => setIsLockdownModalOpen(false)}
                sx={{ py: 1.5, borderRadius: 2, fontWeight: 'bold', textTransform: 'uppercase', fontSize: 11, borderColor: 'divider', color: 'text.secondary' }}
              >
                Abort
              </Button>
            </Grid>
            <Grid size={{ xs: 6 }}>
              <Button 
                fullWidth
                variant="contained"
                color="error"
                onClick={() => { alert('LOCKDOWN_EXECUTED'); setIsLockdownModalOpen(false); }}
                sx={{ py: 1.5, borderRadius: 2, fontWeight: 'bold', textTransform: 'uppercase', fontSize: 11 }}
              >
                Confirm Lockdown
              </Button>
            </Grid>
          </Grid>
        </Box>
      </Modal>
    </Box>
  );
};
