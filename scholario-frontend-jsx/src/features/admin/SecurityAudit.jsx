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
  InputAdornment,
  Divider,
  Alert
} from '@mui/material';
import ShieldIcon from '@mui/icons-material/Shield';
import RefreshIcon from '@mui/icons-material/Refresh';
import FilterListIcon from '@mui/icons-material/FilterList';
import SearchIcon from '@mui/icons-material/Search';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import InfoIcon from '@mui/icons-material/Info';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';

import { Modal } from '../../components/Modal';





export const SecurityAudit = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedViolation, setSelectedViolation] = useState(null);
  
  const { data, loading, refetch } = useRestQuery('/api/audit/reports', 'getViolationReports', {
    username: searchTerm || undefined
  });

  const { data: patternData, loading: patternLoading } = useRestQuery('/api/audit/analyze-patterns', 'analyzeUsagePatterns');

  const violations = data?.getViolationReports || [];
  const patterns = patternData?.analyzeUsagePatterns || [];

  const getSeverityColor = (severity) => {
    switch (severity) {
      case 'CRITICAL': return 'error';
      case 'HIGH': return 'warning';
      case 'MEDIUM': return 'info';
      default: return 'default';
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h4" fontWeight={900} color="text.primary" sx={{ letterSpacing: -1, textTransform: 'uppercase', fontFamily: 'Outfit, sans-serif' }}>
            Security Audit Log
          </Typography>
          <Typography variant="body2" color="text.secondary" fontWeight={500}>
            Global infrastructure telemetry & violation monitoring
          </Typography>
        </Box>
        <Button 
          variant="outlined" 
          startIcon={<RefreshIcon sx={{ animation: loading ? 'spin 1.5s linear infinite' : 'none' }} />}
          onClick={() => refetch()}
          sx={{ 
            borderRadius: 3.5, 
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
          Refresh Telemetry
        </Button>
      </Box>

      {/* Main Grid */}
      <Grid container spacing={4}>
        {/* Left Side: Stats and Filters */}
        <Grid size={{ xs: 12, lg: 3 }} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Card sx={{ borderRadius: 2, bgcolor: 'grey.900', color: 'white', border: 'none', p: 1 }}>
            <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
              <Typography variant="caption" fontWeight="bold" sx={{ display: 'block', color: 'grey.500', textTransform: 'uppercase', letterSpacing: 1.5, mb: 3 }}>
                Security Overview
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                <Box>
                  <Typography variant="h5" fontWeight={900} color="white" sx={{ lineHeight: 1 }}>
                    {violations.length}
                  </Typography>
                  <Typography variant="caption" fontWeight="bold" color="grey.500" sx={{ textTransform: 'uppercase', mt: 0.5, display: 'block' }}>
                    Total Incidents
                  </Typography>
                </Box>
                <Divider sx={{ bgcolor: 'grey.800' }} />
                <Box>
                  <Typography variant="h5" fontWeight={900} color="error.main" sx={{ lineHeight: 1 }}>
                    {violations.filter((v) => v.severity === 'CRITICAL').length}
                  </Typography>
                  <Typography variant="caption" fontWeight="bold" color="grey.500" sx={{ textTransform: 'uppercase', mt: 0.5, display: 'block' }}>
                    Critical Alerts
                  </Typography>
                </Box>
                <Divider sx={{ bgcolor: 'grey.800' }} />
                <Box>
                  <Typography variant="h5" fontWeight={900} color="success.main" sx={{ lineHeight: 1 }}>
                    {violations.filter((v) => v.resolved).length}
                  </Typography>
                  <Typography variant="caption" fontWeight="bold" color="grey.500" sx={{ textTransform: 'uppercase', mt: 0.5, display: 'block' }}>
                    Resolved Nodes
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>

          <Card sx={{ borderRadius: 2, border: '1px solid', borderColor: 'divider', boxShadow: 'none', p: 1 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Typography variant="caption" fontWeight="black" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 1.2, display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                <FilterListIcon fontSize="small" /> Search Filters
              </Typography>
              <TextField 
                fullWidth
                size="small"
                placeholder="Search Subject..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
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
                  '& .MuiOutlinedInput-root': {
                    borderRadius: 1.5,
                    bgcolor: 'grey.50',
                    fontSize: 12,
                    fontWeight: 600,
                    '& fieldset': { borderColor: 'transparent' },
                    '&:hover fieldset': { borderColor: 'divider' }
                  }
                }}
              />
            </CardContent>
          </Card>
        </Grid>

        {/* Right Side: Logs Table & Patterns */}
        <Grid size={{ xs: 12, lg: 9 }} sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
          <Card sx={{ borderRadius: 2, border: '1px solid', borderColor: 'divider', boxShadow: 'none', overflow: 'hidden' }}>
            <Box sx={{ px: 3, py: 2, display: 'flex', alignItems: 'center', gap: 1.5, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'grey.50' }}>
              <ShieldIcon color="error" fontSize="small" />
              <Typography variant="subtitle2" fontWeight={850} sx={{ textTransform: 'uppercase', letterSpacing: 0.5 }}>
                Active Violation Registry
              </Typography>
            </Box>
            
            <TableContainer>
              <Table sx={{ minWidth: 600 }}>
                <TableHead sx={{ bgcolor: 'grey.50' }}>
                  <TableRow sx={{ '& th': { fontSize: 10, fontWeight: 'black', color: 'text.secondary', textTransform: 'uppercase', letterSpacing: 1.2 } }}>
                    <TableCell sx={{ pl: 4 }}>Subject</TableCell>
                    <TableCell>Incident Type</TableCell>
                    <TableCell>Severity</TableCell>
                    <TableCell>Timestamp</TableCell>
                    <TableCell align="right" sx={{ pr: 4 }}>Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody sx={{ fontFamily: 'monospace' }}>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center" sx={{ py: 6 }}>
                        <CircularProgress size={24} />
                      </TableCell>
                    </TableRow>
                  ) : violations.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center" sx={{ py: 6, fontStyle: 'italic', color: 'text.secondary' }}>
                        No Security Breaches Detected
                      </TableCell>
                    </TableRow>
                  ) : violations.map((v) => (
                    <TableRow key={v.id} sx={{ '&:hover': { bgcolor: 'grey.50' } }}>
                      <TableCell sx={{ pl: 4, py: 2 }}>
                        <Typography variant="body2" fontWeight="bold" color="text.primary" sx={{ fontFamily: 'sans-serif', fontSize: 13 }}>
                          {v.username}
                        </Typography>
                        <Typography variant="caption" color="text.disabled" sx={{ fontSize: 10, display: 'block', mt: 0.25 }}>
                          Node ID: {String(v.id).substring(0, 8)}
                        </Typography>
                      </TableCell>
                      <TableCell sx={{ fontSize: 11, color: 'text.secondary', fontWeight: 'bold', textTransform: 'uppercase' }}>
                        {v.type.replace(/_/g, ' ')}
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={v.severity} 
                          size="small" 
                          color={getSeverityColor(v.severity)}
                          sx={{ 
                            borderRadius: 1, 
                            fontWeight: 'black', 
                            fontSize: 9, 
                            height: 18,
                            textTransform: 'uppercase'
                          }} 
                        />
                      </TableCell>
                      <TableCell sx={{ fontSize: 11, color: 'text.secondary', fontWeight: 500 }}>
                        {new Date(v.detectedAt).toLocaleString()}
                      </TableCell>
                      <TableCell align="right" sx={{ pr: 4 }}>
                        <IconButton 
                          size="small" 
                          onClick={() => setSelectedViolation(v)}
                          sx={{ 
                            borderRadius: 2, 
                            color: 'text.secondary',
                            '&:hover': { color: 'primary.main', bgcolor: 'primary.lighter' }
                          }}
                        >
                          <InfoIcon sx={{ fontSize: 18 }} />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>

          {/* Usage Pattern Analysis */}
          <Card sx={{ borderRadius: 4, border: '1px solid', borderColor: 'divider', boxShadow: 'none' }}>
            <Box sx={{ px: 3, py: 2, display: 'flex', alignItems: 'center', gap: 1.5, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'grey.50' }}>
              <VerifiedUserIcon color="primary" fontSize="small" />
              <Typography variant="subtitle2" fontWeight={850} sx={{ textTransform: 'uppercase', letterSpacing: 0.5 }}>
                Usage Pattern Analysis
              </Typography>
            </Box>
            
            <CardContent sx={{ p: 3 }}>
              {patternLoading ? (
                <Box sx={{ py: 4, display: 'flex', justifyContent: 'center' }}>
                  <CircularProgress size={20} />
                </Box>
              ) : patterns.length === 0 ? (
                <Box sx={{ py: 4, textAlign: 'center', bgcolor: 'grey.50', border: '1px dashed', borderColor: 'divider', borderRadius: 3 }}>
                  <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
                    No Suspicious Patterns Detected
                  </Typography>
                </Box>
              ) : (
                <Grid container spacing={3}>
                  {patterns.map((p) => (
                    <Grid size={{ xs: 12, sm: 6 }} key={p.id}>
                      <Box 
                        sx={{ 
                          p: 2.5, 
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
                          <Typography variant="body2" fontWeight="bold" sx={{ fontFamily: 'sans-serif' }}>
                            {p.username}
                          </Typography>
                          <Chip 
                            label={p.severity} 
                            size="small" 
                            color={getSeverityColor(p.severity)}
                            sx={{ borderRadius: 1, fontWeight: 'bold', fontSize: 8, height: 16 }} 
                          />
                        </Box>
                        <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic', fontWeight: 500, mt: 0.5, lineHeight: 1.4 }}>
                          "{p.description}"
                        </Typography>
                        <Typography variant="caption" color="text.disabled" sx={{ display: 'block', mt: 1, fontWeight: 'bold' }}>
                          {new Date(p.detectedAt).toLocaleDateString()}
                        </Typography>
                      </Box>
                    </Grid>
                  ))}
                </Grid>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Forensics Modal */}
      <Modal
        isOpen={!!selectedViolation}
        onClose={() => setSelectedViolation(null)}
        title="Incident Forensics"
        subtitle="Detailed violation breakdown and resolution protocol"
      >
        {selectedViolation && (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5 }}>
            <Alert 
              severity={getSeverityColor(selectedViolation.severity)}
              icon={<WarningAmberIcon sx={{ fontSize: 24 }} />}
              sx={{ 
                borderRadius: 1.5, 
                border: '1px solid',
                alignItems: 'flex-start',
                '& .MuiAlert-icon': { mt: 0.5 }
              }}
            >
              <Typography variant="subtitle2" fontWeight="black" sx={{ textTransform: 'uppercase', display: 'block', mb: 0.5, letterSpacing: 0.5 }}>
                Security Breach Detected
              </Typography>
              <Typography variant="body2" sx={{ fontSize: 12, fontWeight: 500, lineHeight: 1.4 }}>
                {selectedViolation.description}
              </Typography>
            </Alert>

            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <Box sx={{ bg: 'grey.50', bgcolor: 'grey.50', p: 2, borderRadius: 1.5, border: '1px solid', borderColor: 'divider' }}>
                  <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', display: 'block', mb: 0.5 }}>
                    Subject
                  </Typography>
                  <Typography variant="body2" fontWeight="bold">
                    @{selectedViolation.username}
                  </Typography>
                </Box>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <Box sx={{ bg: 'grey.50', bgcolor: 'grey.50', p: 2, borderRadius: 1.5, border: '1px solid', borderColor: 'divider' }}>
                  <Typography variant="caption" fontWeight="bold" color="text.disabled" sx={{ textTransform: 'uppercase', display: 'block', mb: 0.5 }}>
                    Incident Type
                  </Typography>
                  <Typography variant="body2" fontWeight="bold" sx={{ textTransform: 'uppercase' }}>
                    {selectedViolation.type.replace(/_/g, ' ')}
                  </Typography>
                </Box>
              </Grid>
            </Grid>

            <Box sx={{ p: 3, bgcolor: 'grey.900', color: 'white', borderRadius: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3, color: 'primary.light' }}>
                <VerifiedUserIcon fontSize="small" />
                <Typography variant="caption" fontWeight="bold" sx={{ textTransform: 'uppercase', letterSpacing: 1.2 }}>
                  Resolution Protocol
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                <Button 
                  fullWidth
                  variant="contained" 
                  color="error"
                  onClick={() => { alert('Isolating Node...'); setSelectedViolation(null); }}
                  sx={{ py: 1.25, borderRadius: 2, fontWeight: 'bold', textTransform: 'uppercase', fontSize: 10, letterSpacing: 1 }}
                >
                  Isolate Subject Node
                </Button>
                <Button 
                  fullWidth
                  variant="contained" 
                  color="inherit"
                  onClick={() => { alert('Clearing Incident...'); setSelectedViolation(null); }}
                  sx={{ py: 1.25, borderRadius: 2, fontWeight: 'bold', textTransform: 'uppercase', fontSize: 10, letterSpacing: 1, bgcolor: 'grey.800', '&:hover': { bgcolor: 'grey.700' } }}
                >
                  Mark as False Positive
                </Button>
              </Box>
            </Box>
          </Box>
        )}
      </Modal>
    </Box>
  );
};
