/**
 * @file PosLayout.tsx
 * @description POS 作業主版面 / POS workspace layout
 * @description_en Provides responsive navigation, status header and cart slots
 * @description_zh 提供響應式導覽、狀態列與購物車掛載區域
 */
import React, { useState } from 'react';
import { 
  Box, Drawer, List, ListItem, ListItemIcon, ListItemText, IconButton, 
  useTheme, useMediaQuery, Typography, Avatar, Divider, ListItemButton, 
  Paper, Button, BottomNavigation, BottomNavigationAction 
} from '@mui/material';
import {
  PointOfSale, Receipt, Inventory, Settings, Category, LocalCafe,
  LockOutlined, Menu as MenuIcon, ShoppingCart, Wifi, Circle
} from '@mui/icons-material';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

const SIDEBAR_EXPANDED_WIDTH = 240;
const CART_WIDTH = 340;

const PosLayout: React.FC = () => {
  const theme = useTheme();
  // ========================================
  // 響應式斷點 / Responsive Breakpoints
  // ========================================
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isTablet = useMediaQuery(theme.breakpoints.between('sm', 'lg'));
  const isSmallScreen = useMediaQuery(theme.breakpoints.down('lg'));
  
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
    { text: '收銀台', icon: <PointOfSale />, path: '/pos/register' },
    { text: '訂單', icon: <Receipt />, path: '/pos/orders' },
    { text: '商品', icon: <LocalCafe />, path: '/pos/products' },
    { text: '分類', icon: <Category />, path: '/pos/categories' },
    { text: '庫存', icon: <Inventory />, path: '/pos/inventory' },
  ];

  const sidebarContent = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: 'background.paper' }}>
      <Box sx={{ p: 2.5, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <Avatar sx={{ bgcolor: 'rgba(112,72,232,0.18)', color: '#B2C6FF', width: 40, height: 40 }} variant="rounded">
          <PointOfSale />
        </Avatar>
        <Box>
          <Typography variant="h6" color="text.primary" fontWeight={900} sx={{ lineHeight: 1.1 }}>
            Titanium POS
          </Typography>
          <Typography variant="caption" color="text.secondary" fontWeight={700}>
            咖啡門市收銀台
          </Typography>
        </Box>
      </Box>
      <Box sx={{ mx: 2, p: 2, display: 'flex', alignItems: 'center', gap: 2, mb: 2, borderRadius: 3, bgcolor: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.05)' }}>
        <Box>
          <Typography variant="subtitle1" fontWeight={900}>終端機 01</Typography>
          <Typography variant="body2" color="text.secondary">一樓主區 · A 班</Typography>
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
                  minHeight: 48,
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
             <ListItemText primary="支援" sx={{ color: 'text.secondary' }} />
           </ListItemButton>
        </ListItem>
        <Button
            variant="outlined"
            fullWidth
            startIcon={<LockOutlined />}
            sx={{ 
                mt: 2, 
                minHeight: 48,
                color: 'text.secondary', 
                borderColor: 'rgba(255,255,255,0.2)',
                bgcolor: 'rgba(0,0,0,0.2)',
                '&:hover': { bgcolor: 'rgba(0,0,0,0.4)', borderColor: 'rgba(255,255,255,0.3)' } 
            }}
        >
          鎖定終端
        </Button>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', width: '100vw', flexGrow: 1, height: '100vh', overflow: 'hidden', bgcolor: 'background.default' }}>
      
      {/* 行動與平板頂部列 / Mobile and tablet top header */}
      {isSmallScreen && (
        <Paper elevation={4} sx={{ position: 'fixed', top: 0, left: 0, right: 0, height: 64, display: 'flex', alignItems: 'center', px: 2, zIndex: 1100, borderRadius: 0, justifyContent: 'space-between' }}>
          {isMobile ? (
            <IconButton aria-label="開啟導覽選單" color="inherit" onClick={handleDrawerToggle} sx={{ p: 1.5 }}>
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
                  aria-label={cartOpen ? '關閉購物車' : '開啟購物車'}
                  color={cartOpen ? 'primary' : 'inherit'} 
                  onClick={handleCartToggle}
                  sx={{ 
                    p: 1.5,
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

      {/* 側邊導覽 / Sidebar navigation */}
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

      {/* 主要內容區 / Main content area */}
      <Box sx={{ 
          flexGrow: 1, 
          height: '100vh', 
          overflowY: 'auto', 
          pt: isSmallScreen ? '64px' : 0, 
          pb: isTablet ? '72px' : (isMobile ? '64px' : 0)
      }}>
        {/* 桌面狀態列 / Desktop status bar */}
        {!isSmallScreen && (
           <Box sx={{ height: 64, display: 'flex', justifyContent: 'flex-end', alignItems: 'center', px: 4, gap: 3 }}>
              <Box sx={{ mr: 'auto' }}>
                <Typography variant="h6" fontWeight={900}>信義旗艦店</Typography>
                <Typography variant="caption" color="text.secondary">營業班次 · 終端機 01</Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'success.main', bgcolor: 'rgba(35, 193, 107, 0.1)', px: 2, py: 0.5, borderRadius: 5 }}>
                 <Wifi fontSize="small" />
                 <Typography variant="caption" fontWeight="bold">線上</Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, bgcolor: 'rgba(255,255,255,0.04)', px: 1.5, py: 0.75, borderRadius: 3 }}>
                <Avatar sx={{ width: 32, height: 32, cursor: 'pointer' }} />
                <Box>
                  <Typography variant="body2" fontWeight={800}>操作員 #042</Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <Circle sx={{ fontSize: 8, color: 'success.main' }} />
                    <Typography variant="caption" color="text.secondary">值勤中</Typography>
                  </Box>
                </Box>
              </Box>
           </Box>
        )}
        <Box sx={{ p: isSmallScreen ? 2 : 4, pt: isSmallScreen ? 2 : 0, height: !isSmallScreen ? 'calc(100vh - 64px)' : '100%' }}>
            <Outlet />
        </Box>
      </Box>

      {/* 桌面購物車區 / Desktop cart area */}
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

      {/* 行動購物車抽屜 / Mobile cart drawer */}
      {isSmallScreen && location.pathname.includes('/register') && (
         <Drawer 
            anchor={isTablet ? 'right' : 'bottom'} 
            open={cartOpen} 
            onClose={handleCartToggle} 
            ModalProps={{ keepMounted: true }}
            sx={{ 
              '& .MuiDrawer-paper': { 
                width: isTablet ? 360 : '100%',
                height: isTablet ? '100vh' : '85vh', 
                borderTopLeftRadius: isTablet ? 0 : 16, 
                borderTopRightRadius: isTablet ? 0 : 16, 
                bgcolor: 'background.paper',
                boxSizing: 'border-box',
                pt: isSmallScreen && isTablet ? '64px' : 0
              } 
            }}>
            <div id={isTablet ? "cart-root" : "cart-root-mobile"} style={{ height: '100%' }}></div>
         </Drawer>
      )}

      {/* 平板底部導覽 / Tablet bottom navigation */}
      {isTablet && (
        <Paper sx={{ position: 'fixed', bottom: 0, left: 0, right: 0, zIndex: 1200 }} elevation={6}>
          <BottomNavigation
            showLabels
            value={menuItems.findIndex(i => location.pathname.includes(i.path))}
            onChange={(_event, newValue) => {
              navigate(menuItems[newValue].path);
            }}
            sx={{
               height: 72,
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
