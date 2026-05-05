import React, { useState } from 'react';
import { 
  Box, Drawer, List, ListItem, ListItemIcon, ListItemText, IconButton, 
  useTheme, useMediaQuery, Typography, Avatar, Divider, ListItemButton, 
  Paper, Button, BottomNavigation, BottomNavigationAction 
} from '@mui/material';
import { 
  Dashboard, PointOfSale, Receipt, Inventory, Settings, 
  LockOutlined, Menu as MenuIcon, ShoppingCart, Wifi 
} from '@mui/icons-material';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

const SIDEBAR_EXPANDED_WIDTH = 240;
const CART_WIDTH = 340;

const PosLayout: React.FC = () => {
  const theme = useTheme();
  // md 是 900px, lg 是 1200px
  const isMobile = useMediaQuery(theme.breakpoints.down('sm')); // 小於 600px 視為手機
  const isTablet = useMediaQuery(theme.breakpoints.between('sm', 'lg')); // 600px 到 1200px 視為平板
  const isSmallScreen = useMediaQuery(theme.breakpoints.down('lg')); // 小於 1200px 的統稱
  
  const [mobileOpen, setMobileOpen] = useState(false);
  const [cartOpen, setCartOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleCartToggle = () => {
    setCartOpen(!cartOpen);
  };

  const menuItems = [
    { text: 'Dashboard', icon: <Dashboard />, path: '/pos/dashboard' },
    { text: 'Register', icon: <PointOfSale />, path: '/pos/register' },
    { text: 'Orders', icon: <Receipt />, path: '/pos/orders' },
    { text: 'Inventory', icon: <Inventory />, path: '/pos/inventory' },
    { text: 'Settings', icon: <Settings />, path: '/pos/settings' },
  ];

  const sidebarContent = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: 'background.paper' }}>
      <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
        <Typography variant="h5" color="primary.main" fontWeight="bold" sx={{ px: 1 }}>
          Titanium POS
        </Typography>
      </Box>
      <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
        <Avatar sx={{ bgcolor: 'primary.main', width: 40, height: 40 }} variant="rounded">
           <PointOfSale />
        </Avatar>
        <Box>
          <Typography variant="subtitle1" fontWeight="bold">Terminal 01</Typography>
          <Typography variant="body2" color="text.secondary">Main Floor</Typography>
        </Box>
      </Box>
      <List sx={{ flexGrow: 1, px: 2 }}>
        {menuItems.map((item) => {
          const isActive = location.pathname.includes(item.path);
          return (
            <ListItem disablePadding key={item.text} sx={{ mb: 1 }}>
              <ListItemButton
                onClick={() => navigate(item.path)}
                selected={isActive}
                sx={{
                  borderRadius: 2,
                  minHeight: 48, // Touch target optimization
                  bgcolor: isActive ? 'primary.main' : 'transparent',
                  background: isActive ? 'linear-gradient(90deg, #7048E8 0%, #4D329A 100%)' : 'transparent',
                  '&:hover': {
                    bgcolor: isActive ? 'primary.dark' : 'rgba(255,255,255,0.05)',
                  },
                  '&.Mui-selected': {
                     bgcolor: 'primary.main',
                     color: 'white',
                     '&:hover': {
                        bgcolor: 'primary.dark',
                     }
                  }
                }}
              >
                <ListItemIcon sx={{ color: isActive ? 'white' : 'text.secondary', minWidth: 40 }}>
                  {item.icon}
                </ListItemIcon>
                <ListItemText 
                  primary={item.text} 
                  primaryTypographyProps={{ 
                    fontWeight: isActive ? 600 : 400,
                    color: isActive ? 'white' : 'text.secondary'
                  }} 
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>
      <Divider sx={{ borderColor: 'rgba(255,255,255,0.1)' }} />
      <Box sx={{ p: 2 }}>
        <ListItem disablePadding>
           <ListItemButton sx={{ borderRadius: 2, minHeight: 48 }}>
             <ListItemIcon sx={{ minWidth: 40, color: 'text.secondary' }}><Settings /></ListItemIcon>
             <ListItemText primary="Support" sx={{ color: 'text.secondary' }} />
           </ListItemButton>
        </ListItem>
        <Button
            variant="outlined"
            fullWidth
            startIcon={<LockOutlined />}
            sx={{ 
                mt: 2, 
                minHeight: 48, // Touch target optimization
                color: 'text.secondary', 
                borderColor: 'rgba(255,255,255,0.2)',
                bgcolor: 'rgba(0,0,0,0.2)',
                '&:hover': { bgcolor: 'rgba(0,0,0,0.4)', borderColor: 'rgba(255,255,255,0.3)' } 
            }}
        >
          LOCK TERMINAL
        </Button>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', width: '100vw', flexGrow: 1, height: '100vh', overflow: 'hidden', bgcolor: 'background.default' }}>
      
      {/* Top Header - For Mobile and Tablet */}
      {isSmallScreen && (
        <Paper elevation={4} sx={{ position: 'fixed', top: 0, left: 0, right: 0, height: 64, display: 'flex', alignItems: 'center', px: 2, zIndex: 1100, borderRadius: 0, justifyContent: 'space-between' }}>
          {isMobile ? (
            <IconButton color="inherit" onClick={handleDrawerToggle} sx={{ p: 1.5 }}>
              <MenuIcon />
            </IconButton>
          ) : (
             <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                 <Avatar sx={{ bgcolor: 'primary.main', width: 32, height: 32 }} variant="rounded">
                    <PointOfSale fontSize="small" />
                 </Avatar>
                 <Typography variant="h6" fontWeight="bold">Titanium</Typography>
             </Box>
          )}

          {(!isMobile && isTablet) && (
              <Box sx={{ flexGrow: 1 }} />
          )}
          
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {location.pathname.includes('/register') && (
              <IconButton 
                  color={cartOpen ? 'primary' : 'inherit'} 
                  onClick={handleCartToggle}
                  sx={{ 
                    p: 1.5, // optimal touch target ≈ 48px
                    bgcolor: cartOpen ? 'rgba(112, 72, 232, 0.1)' : 'transparent',
                  }}
              >
                <ShoppingCart />
              </IconButton>
            )}
            <Avatar sx={{ width: 36, height: 36, ml: 1 }} />
          </Box>
        </Paper>
      )}

      {/* Sidebar Overlay (Only for Desktop or Mobile Drawer) */}
      {!isSmallScreen ? (
        <Box sx={{ width: SIDEBAR_EXPANDED_WIDTH, flexShrink: 0 }}>
          <Drawer
            variant="permanent"
            sx={{
              '& .MuiDrawer-paper': { 
                boxSizing: 'border-box', 
                width: SIDEBAR_EXPANDED_WIDTH,
                borderRight: '1px solid rgba(255,255,255,0.05)',
                bgcolor: 'background.paper',
                overflowX: 'hidden'
              },
            }}
          >
            {sidebarContent}
          </Drawer>
        </Box>
      ) : isMobile && (
         <Drawer
            variant="temporary"
            open={mobileOpen}
            onClose={handleDrawerToggle}
            ModalProps={{ keepMounted: true }}
            sx={{
               '& .MuiDrawer-paper': { 
                  boxSizing: 'border-box', 
                  width: SIDEBAR_EXPANDED_WIDTH,
                  borderRight: '1px solid rgba(255,255,255,0.05)',
                  bgcolor: 'background.paper',
               },
            }}
         >
            {sidebarContent}
         </Drawer>
      )}

      {/* Main Content Area */}
      <Box sx={{ 
          flexGrow: 1, 
          height: '100vh', 
          overflowY: 'auto', 
          pt: isSmallScreen ? '64px' : 0, 
          pb: isTablet ? '72px' : (isMobile ? '64px' : 0) // Leave space for BottomNavigation
      }}>
        {/* Top Header / Status bar inside main area on Desktop */}
        {!isSmallScreen && (
           <Box sx={{ height: 64, display: 'flex', justifyContent: 'flex-end', alignItems: 'center', px: 4, gap: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'success.main', bgcolor: 'rgba(35, 193, 107, 0.1)', px: 2, py: 0.5, borderRadius: 5 }}>
                 <Wifi fontSize="small" />
                 <Typography variant="caption" fontWeight="bold">ONLINE</Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">Operator #042</Typography>
              <Avatar sx={{ width: 36, height: 36, cursor: 'pointer' }} />
           </Box>
        )}
        <Box sx={{ p: isSmallScreen ? 2 : 4, pt: isSmallScreen ? 2 : 0, height: !isSmallScreen ? 'calc(100vh - 64px)' : '100%' }}>
            <Outlet />
        </Box>
      </Box>

      {/* Right Cart Area (Desktop only - always open) */}
      {!isSmallScreen && location.pathname.includes('/register') && (
         <Box sx={{ 
             width: CART_WIDTH, 
             minWidth: CART_WIDTH, 
             bgcolor: 'background.paper', 
             borderLeft: '1px solid rgba(255,255,255,0.05)',
             height: '100vh',
             display: 'flex',
             flexDirection: 'column'
         }}>
             <div id="cart-root" style={{ height: '100%' }}></div>
         </Box>
      )}

      {/* Cart Drawer for Medium / Small screens */}
      {isSmallScreen && location.pathname.includes('/register') && (
         <Drawer 
            anchor={isTablet ? 'right' : 'bottom'} 
            open={cartOpen} 
            onClose={handleCartToggle} 
            sx={{ 
              '& .MuiDrawer-paper': { 
                width: isTablet ? 360 : '100%',
                height: isTablet ? '100vh' : '85vh', 
                borderTopLeftRadius: isTablet ? 0 : 16, 
                borderTopRightRadius: isTablet ? 0 : 16, 
                bgcolor: 'background.paper',
                boxSizing: 'border-box',
                pt: isSmallScreen && isTablet ? '64px' : 0 // avoid topbar overlapping in tablet
              } 
            }}>
            <div id={isTablet ? "cart-root" : "cart-root-mobile"} style={{ height: '100%' }}></div>
         </Drawer>
      )}

      {/* Bottom Navigation for Tablet Context (Horizontal layout space saving) */}
      {isTablet && (
        <Paper sx={{ position: 'fixed', bottom: 0, left: 0, right: 0, zIndex: 1200 }} elevation={6}>
          <BottomNavigation
            showLabels
            value={menuItems.findIndex(i => location.pathname.includes(i.path))}
            onChange={(_event, newValue) => {
              navigate(menuItems[newValue].path);
            }}
            sx={{
               height: 72, // Larger touch target
               '& .MuiBottomNavigationAction-root': {
                  minWidth: 'auto',
                  padding: '6px 0 10px 0',
               }
            }}
          >
            {menuItems.map((item) => (
              <BottomNavigationAction 
                key={item.text} 
                label={item.text} 
                icon={item.icon} 
                sx={{
                   color: 'text.secondary',
                   '&.Mui-selected': { 
                      color: 'primary.main',
                   }
                }}
              />
            ))}
          </BottomNavigation>
        </Paper>
      )}
    </Box>
  );
};

export default PosLayout;
