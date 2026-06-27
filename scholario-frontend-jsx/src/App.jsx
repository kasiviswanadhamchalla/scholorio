import React from 'react';
import { ApolloProvider } from '@apollo/client/react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { client } from './graphql/client';
import { AuthProvider, useAuth } from './features/auth/AuthContext';
import { PortalLayout } from './features/shared/PortalLayout';
import { FacultyDashboard } from './features/faculty/FacultyDashboard';
import { BookManagement } from './features/faculty/BookManagement';
import { CourseManagement } from './features/faculty/CourseManagement';
import { FacultySettings } from './features/faculty/FacultySettings';
import { AdminDashboard } from './features/admin/AdminDashboard';
import { DepartmentsManagement } from './features/admin/DepartmentsManagement';
import { SecurityAudit } from './features/admin/SecurityAudit';
import { AdminSettings } from './features/admin/AdminSettings';
import { StudentDashboard } from './features/student/StudentDashboard';
import { StudentSearch } from './features/student/StudentSearch';
import { StudentCourses } from './features/student/StudentCourses';
import { StudentSettings } from './features/student/StudentSettings';
import { LibrarianDashboard } from './features/librarian/LibrarianDashboard';
import { LibrarianStock } from './features/librarian/LibrarianStock';
import { LibrarianSettings } from './features/librarian/LibrarianSettings';
import { UnassignedPage } from './features/auth/UnassignedPage';

// MUI Icons representing the original Lucide icons
import DashboardIcon from '@mui/icons-material/Dashboard';
import BookIcon from '@mui/icons-material/Book';
import SchoolIcon from '@mui/icons-material/School';
import SettingsIcon from '@mui/icons-material/Settings';
import SecurityIcon from '@mui/icons-material/Security';
import BusinessIcon from '@mui/icons-material/Business';
import LocalLibraryIcon from '@mui/icons-material/LocalLibrary';
import SearchIcon from '@mui/icons-material/Search';

// Import bootstrap CSS (secondary component source)
import 'bootstrap/dist/css/bootstrap.min.css';
import { Box, Typography } from '@mui/material';

const RoleProtectedRoute = ({ allowedRoles, children }) => {
  const { authenticated, role } = useAuth();
  
  if (!authenticated) return null; // Keycloak redirects to login anyway
  if (role && !allowedRoles.includes(role)) {
    return <Navigate to="/" replace />;
  }
  
  return <>{children}</>;
};

const FacultyPortal = () => {
  const navItems = [
    { icon: DashboardIcon, label: 'Dashboard', to: '/faculty/dashboard' },
    { icon: BookIcon, label: 'My Books', to: '/faculty/books' },
    { icon: SchoolIcon, label: 'My Courses', to: '/faculty/courses' },
    { icon: SettingsIcon, label: 'Settings', to: '/faculty/settings' },
  ];
  return (
    <RoleProtectedRoute allowedRoles={['FACULTY']}>
      <PortalLayout title="Faculty Portal" navItems={navItems} />
    </RoleProtectedRoute>
  );
};

const AdminPortal = () => {
  const navItems = [
    { icon: DashboardIcon, label: 'Dashboard', to: '/admin/dashboard' },
    { icon: SecurityIcon, label: 'Security', to: '/admin/security' },
    { icon: BusinessIcon, label: 'Departments', to: '/admin/departments' },
    { icon: SettingsIcon, label: 'Settings', to: '/admin/settings' },
  ];
  return (
    <RoleProtectedRoute allowedRoles={['ADMIN']}>
      <PortalLayout title="Admin Portal" navItems={navItems} />
    </RoleProtectedRoute>
  );
};

const StudentPortal = () => {
  const navItems = [
    { icon: DashboardIcon, label: 'My Library', to: '/student/dashboard' },
    { icon: SearchIcon, label: 'Search Books', to: '/student/search' },
    { icon: SchoolIcon, label: 'My Courses', to: '/student/courses' },
    { icon: SettingsIcon, label: 'Settings', to: '/student/settings' },
  ];
  return (
    <RoleProtectedRoute allowedRoles={['STUDENT']}>
      <PortalLayout title="Student Portal" navItems={navItems} />
    </RoleProtectedRoute>
  );
};

const LibrarianPortal = () => {
  const navItems = [
    { icon: LocalLibraryIcon, label: 'Circulation', to: '/librarian/dashboard' },
    { icon: SearchIcon, label: 'Manage Stock', to: '/librarian/stock' },
    { icon: SettingsIcon, label: 'Settings', to: '/librarian/settings' },
  ];
  return (
    <RoleProtectedRoute allowedRoles={['LIBRARIAN']}>
      <PortalLayout title="Librarian Portal" navItems={navItems} />
    </RoleProtectedRoute>
  );
};

function App() {
  return (
    <ApolloProvider client={client}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/faculty" element={<FacultyPortal />}>
              <Route index element={<Navigate to="dashboard" replace />} />
              <Route path="dashboard" element={<FacultyDashboard />} />
              <Route path="books" element={<BookManagement />} />
              <Route path="courses" element={<CourseManagement />} />
              <Route path="settings" element={<FacultySettings />} />
            </Route>

            <Route path="/admin" element={<AdminPortal />}>
              <Route index element={<Navigate to="dashboard" replace />} />
              <Route path="dashboard" element={<AdminDashboard />} />
              <Route path="security" element={<SecurityAudit />} />
              <Route path="departments" element={<DepartmentsManagement />} />
              <Route path="settings" element={<AdminSettings />} />
            </Route>

            <Route path="/student" element={<StudentPortal />}>
              <Route index element={<Navigate to="dashboard" replace />} />
              <Route path="dashboard" element={<StudentDashboard />} />
              <Route path="search" element={<StudentSearch />} />
              <Route path="courses" element={<StudentCourses />} />
              <Route path="settings" element={<StudentSettings />} />
            </Route>

            <Route path="/librarian" element={<LibrarianPortal />}>
              <Route index element={<Navigate to="dashboard" replace />} />
              <Route path="dashboard" element={<LibrarianDashboard />} />
              <Route path="stock" element={<LibrarianStock />} />
              <Route path="settings" element={<LibrarianSettings />} />
            </Route>

            <Route path="/unassigned" element={<UnassignedPage />} />

            {/* Role-based redirection at root */}
            <Route path="/" element={<RootRedirect />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ApolloProvider>
  );
}

const RootRedirect = () => {
  const { role } = useAuth();
  if (role === 'ADMIN') return <Navigate to="/admin" replace />;
  if (role === 'FACULTY') return <Navigate to="/faculty" replace />;
  if (role === 'LIBRARIAN') return <Navigate to="/librarian" replace />;
  if (role === 'STUDENT') return <Navigate to="/student" replace />;
  if (role === 'UNASSIGNED') return <Navigate to="/unassigned" replace />;
  
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh' }}>
      <Typography variant="body1" color="text.secondary" fontWeight={500}>
        Redirecting to your portal...
      </Typography>
    </Box>
  );
};

export default App;
