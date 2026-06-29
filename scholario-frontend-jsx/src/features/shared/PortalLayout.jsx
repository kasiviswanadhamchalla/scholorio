import React, { useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { 
  Box, 
  Drawer, 
  AppBar, 
  Toolbar, 
  IconButton, 
  Typography, 
  Avatar, 
  Button, 
  Divider, 
  List, 
  ListItem, 
  ListItemButton, 
  ListItemIcon, 
  ListItemText,
  Paper,
  Menu,
  MenuItem
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import LogoutIcon from '@mui/icons-material/Logout';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';

const drawerWidth = 260;

export const PortalLayout = ({ title, navItems }) => {
  const { logout, username, role, allRoles, switchRole } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [profileAnchor, setProfileAnchor] = useState(null);
  const location = useLocation();

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleProfileOpen = (event) => {
    setProfileAnchor(event.currentTarget);
  };

  const handleProfileClose = () => {
    setProfileAnchor(null);
  };

  const sidebarContent = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* Brand Header */}
      <Box sx={{ p: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography 
          variant="h5" 
          fontWeight={900} 
          sx={{ 
            color: 'primary.main', 
            letterSpacing: -0.5,
            fontFamily: 'Outfit, sans-serif'
          }}
        >
          Scholario
        </Typography>
      </Box>

      <Divider sx={{ opacity: 0.6, mx: 2, mb: 2 }} />

      {/* Navigation List */}
      <List sx={{ flexGrow: 1, px: 2, py: 0, '& .MuiListItem-root': { mb: 0.5 } }}>
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.to;
          return (
            <ListItem key={item.to} disablePadding>
              <ListItemButton
                component={NavLink}
                to={item.to}
                onClick={() => setMobileOpen(false)}
                sx={{
                  borderRadius: 3,
                  py: 1.25,
                  px: 2,
                  bgcolor: isActive ? 'primary.main' : 'transparent',
                  color: isActive ? 'white' : 'text.secondary',
                  '&:hover': {
                    bgcolor: isActive ? 'primary.dark' : 'action.hover',
                    color: isActive ? 'white' : 'text.primary',
                    '& .MuiListItemIcon-root': {
                      color: isActive ? 'white' : 'primary.main',
                    }
                  },
                  transition: 'all 0.2s ease-in-out',
                }}
              >
                <ListItemIcon 
                  sx={{ 
                    minWidth: 36, 
                    color: isActive ? 'white' : 'text.secondary',
                    transition: 'color 0.2s',
                  }}
                >
                  <Icon fontSize="small" />
                </ListItemIcon>
                <ListItemText 
                  primary={
                    <Typography sx={{ fontSize: 14, fontWeight: isActive ? 600 : 500 }}>
                      {item.label}
                    </Typography>
                  }
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>

      {/* Sidebar Footer space */}
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'grey.50' }}>
      {/* Mobile Drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{ keepMounted: true }} // Better open performance on mobile.
        sx={{
          display: { xs: 'block', lg: 'none' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth, borderRight: '1px solid', borderColor: 'divider' },
        }}
      >
        {sidebarContent}
      </Drawer>

      {/* Desktop Permanent Drawer */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', lg: 'block' },
          width: drawerWidth,
          flexShrink: 0,
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth, borderRight: '1px solid', borderColor: 'divider' },
        }}
        open
      >
        {sidebarContent}
      </Drawer>

      {/* Main Content Area */}
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        {/* App Bar / Header */}
        <AppBar
          position="sticky"
          elevation={0}
          sx={{
            bgcolor: 'white',
            borderBottom: '1px solid',
            borderColor: 'divider',
            color: 'text.primary',
            zIndex: (theme) => theme.zIndex.drawer - 1,
          }}
        >
          <Toolbar sx={{ px: { xs: 2, sm: 4 }, minHeight: 64, justifySelf: 'stretch', display: 'flex', justifyContent: 'space-between' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <IconButton
                color="inherit"
                aria-label="open drawer"
                edge="start"
                onClick={handleDrawerToggle}
                sx={{ mr: 1, display: { lg: 'none' } }}
              >
                <MenuIcon />
              </IconButton>
              <Typography 
                variant="h6" 
                fontWeight={900} 
                sx={{ 
                  fontFamily: 'Outfit, sans-serif', 
                  letterSpacing: 0.5,
                  color: 'primary.main',
                  textTransform: 'uppercase'
                }}
              >
                SCHOLARIO
              </Typography>
            </Box>

            {/* Profile Dropdown Trigger */}
            <Box>
              <Box 
                onClick={handleProfileOpen}
                sx={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: 1.5, 
                  cursor: 'pointer',
                  p: 0.75,
                  px: 1.5,
                  borderRadius: 3,
                  transition: 'bgcolor 0.2s',
                  '&:hover': {
                    bgcolor: 'grey.100'
                  }
                }}
              >
                <Box sx={{ textAlign: 'right', display: { xs: 'none', sm: 'block' } }}>
                  <Typography variant="subtitle2" fontWeight="bold" sx={{ lineHeight: 1.2 }}>
                    {username}
                  </Typography>
                  <Typography 
                    variant="caption" 
                    fontWeight="black" 
                    color="text.disabled"
                    sx={{ textTransform: 'uppercase', letterSpacing: 1, fontSize: 9 }}
                  >
                    {role}
                  </Typography>
                </Box>
                <Avatar 
                  sx={{ 
                    bgcolor: 'primary.main', 
                    fontWeight: 'bold', 
                    fontSize: 15,
                    boxShadow: '0 4px 10px rgba(99, 102, 241, 0.2)',
                    width: 36,
                    height: 36
                  }}
                >
                  {username ? username[0].toUpperCase() : 'U'}
                </Avatar>
              </Box>

              {/* Profile Menu Options */}
              <Menu
                anchorEl={profileAnchor}
                open={Boolean(profileAnchor)}
                onClose={handleProfileClose}
                slotProps={{
                  paper: {
                    sx: {
                      borderRadius: 2.5,
                      mt: 1,
                      minWidth: 220,
                      boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
                      border: '1px solid',
                      borderColor: 'divider',
                      p: 0.5
                    }
                  }
                }}
                transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
              >
                <Box sx={{ px: 2, py: 1.5 }}>
                  <Typography variant="subtitle2" fontWeight={850} color="text.primary">
                    {username}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" fontWeight={600} sx={{ display: 'block', mt: 0.25 }}>
                    Role: <span style={{ color: '#4f46e5', fontWeight: 'bold' }}>{role}</span>
                  </Typography>
                </Box>
                
                <Divider sx={{ my: 0.5 }} />

                {/* Dashboard / Role switcher */}
                {allRoles.length > 1 && (
                  <>
                    <Box sx={{ px: 2, pt: 1, pb: 0.5 }}>
                      <Typography variant="caption" fontWeight="black" color="text.disabled" sx={{ textTransform: 'uppercase', letterSpacing: 0.8, fontSize: 9 }}>
                        Switch Dashboard
                      </Typography>
                    </Box>
                    {allRoles.map((r) => (
                      <MenuItem 
                        key={r}
                        disabled={r === role}
                        onClick={() => {
                          switchRole(r);
                          handleProfileClose();
                        }}
                        sx={{
                          borderRadius: 1.5,
                          fontSize: 12,
                          fontWeight: r === role ? 'bold' : 600,
                          py: 1,
                          px: 2,
                          display: 'flex',
                          alignItems: 'center',
                          gap: 1.5,
                          '&:hover': {
                            bgcolor: 'grey.100',
                            color: 'primary.main'
                          }
                        }}
                      >
                        <SwapHorizIcon fontSize="small" />
                        {r} Dashboard
                      </MenuItem>
                    ))}
                    <Divider sx={{ my: 0.5 }} />
                  </>
                )}

                {/* Logout Option */}
                <MenuItem 
                  onClick={() => {
                    logout();
                    handleProfileClose();
                  }}
                  sx={{
                    borderRadius: 1.5,
                    fontSize: 12,
                    fontWeight: 800,
                    py: 1,
                    px: 2,
                    color: 'error.main',
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1.5,
                    '&:hover': {
                      bgcolor: 'error.lighter',
                      color: 'error.dark'
                    }
                  }}
                >
                  <LogoutIcon fontSize="small" />
                  Logout
                </MenuItem>
              </Menu>
            </Box>
          </Toolbar>
        </AppBar>

        {/* Main Content Page Container */}
        <Box 
          component="main" 
          sx={{ 
            flexGrow: 1, 
            p: { xs: 2, sm: 4 }, 
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'stretch',
            bgcolor: '#f1f5f9' // Premium Slate-100 Background
          }}
        >
          <Box sx={{ width: '100%', maxWidth: 1200, mx: 'auto' }} className="animate-fade-in">
            <Outlet />
          </Box>
        </Box>
      </Box>
    </Box>
  );
};
