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
  Paper
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import LogoutIcon from '@mui/icons-material/Logout';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';

const drawerWidth = 260;

export const PortalLayout = ({ title, navItems }) => {
  const { logout, username, role, allRoles, switchRole } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
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
                  primary={item.label} 
                  primaryTypographyProps={{ 
                    fontSize: 14, 
                    fontWeight: isActive ? 600 : 500 
                  }} 
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>

      {/* Sidebar Footer (Role Switch & Logout) */}
      <Box sx={{ p: 2, borderTop: '1px solid', borderColor: 'divider' }}>
        {allRoles.length > 1 && (
          <Box sx={{ mb: 2, px: 1 }}>
            <Typography 
              variant="caption" 
              fontWeight="bold" 
              color="text.disabled" 
              sx={{ display: 'block', mb: 1, letterSpacing: 1.2, textTransform: 'uppercase', fontSize: 10 }}
            >
              Switch Portal
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
              {allRoles.map((r) => (
                <Button
                  key={r}
                  variant={r === role ? 'contained' : 'text'}
                  color="primary"
                  size="small"
                  disabled={r === role}
                  onClick={() => {
                    switchRole(r);
                    setMobileOpen(false);
                  }}
                  startIcon={<SwapHorizIcon sx={{ fontSize: 14 }} />}
                  sx={{
                    justifyContent: 'flex-start',
                    textTransform: 'uppercase',
                    py: 0.75,
                    px: 1.5,
                    fontSize: 10,
                    fontWeight: 700,
                    borderRadius: 2,
                    boxShadow: 'none',
                    '&:hover': {
                      boxShadow: 'none',
                    }
                  }}
                >
                  {r} Portal
                </Button>
              ))}
            </Box>
          </Box>
        )}
        
        <ListItem disablePadding>
          <ListItemButton
            onClick={logout}
            sx={{
              borderRadius: 3,
              py: 1.25,
              px: 2,
              color: 'error.main',
              '&:hover': {
                bgcolor: 'error.lighter',
                color: 'error.dark',
              },
            }}
          >
            <ListItemIcon sx={{ minWidth: 36, color: 'inherit' }}>
              <LogoutIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText 
              primary="Logout" 
              primaryTypographyProps={{ fontSize: 14, fontWeight: 600 }} 
            />
          </ListItemButton>
        </ListItem>
      </Box>
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
              <Typography variant="h6" fontWeight="bold" sx={{ fontFamily: 'Outfit, sans-serif' }}>
                {title}
              </Typography>
            </Box>

            {/* Profile Info */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
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
                  boxShadow: '0 4px 10px rgba(99, 102, 241, 0.2)'
                }}
              >
                {username ? username[0].toUpperCase() : 'U'}
              </Avatar>
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
            alignItems: 'center' 
          }}
        >
          <Box sx={{ width: '100%', maxWidth: 1200 }}>
            <Outlet />
          </Box>
        </Box>
      </Box>
    </Box>
  );
};
