/**
 * @file PosLayout.tsx
 * @description POS 作業主版面 / POS workspace layout
 * @description_en Provides responsive navigation, status header and cart slots
 * @description_zh 提供響應式導覽、狀態列與購物車掛載區域
 */
import React, { useMemo, useState } from 'react';
import {
  Box, Collapse, Drawer, List, ListItem, ListItemIcon, ListItemText, IconButton,
  useTheme, useMediaQuery, Typography, Avatar, Divider, ListItemButton,
  Paper, Button, BottomNavigation, BottomNavigationAction, Badge as MuiBadge
} from '@mui/material';
import {
  PointOfSale, Receipt, Settings, Replay, LockOutlined, Menu as MenuIcon,
  ShoppingCart, Wifi, Circle, KeyboardArrowDown, KeyboardArrowRight
} from '@mui/icons-material';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@shared/store/authStore';
import { readPosSession } from '@features/pos-orders/posSession';
import { useCartStore } from '@features/pos-orders/store/cartStore';

const SIDEBAR_EXPANDED_WIDTH = 240;
const CART_WIDTH = 340;

const ROLE_LABELS: Record<string, string> = {
  STORE_MANAGER: '店長',
  CASHIER: '收銀員',
  ADMIN: '系統管理員',
  MANAGER: '管理員',
};

interface NavItem {
  text: string;
  icon: React.ReactNode;
  path: string;
  children?: NavItem[];
}

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
  const [expandedGroups, setExpandedGroups] = useState<Record<string, boolean>>({});
  const navigate = useNavigate();
  const location = useLocation();
  const logout = useAuthStore((state) => state.logout);
  const authUser = useAuthStore((state) => state.user);
  const cartItemCount = useCartStore((state) => state.itemCount());
  const posSession = useMemo(() => readPosSession(), []);
  const storeLabel = posSession?.storeName?.trim() || 'POS 門店';
  const terminalLabel = posSession?.terminalName?.trim()
    || posSession?.terminalCode?.trim()
    || '未命名終端';
  const operatorName = posSession?.username?.trim() || authUser?.username || '未登入';
  const roleLabel = posSession?.role ? ROLE_LABELS[posSession.role] ?? posSession.role : 'POS 作業';
  const operatorInitial = operatorName.slice(0, 1).toUpperCase();

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleCartToggle = () => {
    setCartOpen(!cartOpen);
  };

  const handleOpenAdmin = () => {
    navigate('/admin/dashboard');
  };

  const handleLockTerminal = () => {
    localStorage.removeItem('pos-session');
    logout();
    navigate('/pos/login', { replace: true });
  };

  const menuItems: NavItem[] = [
    { text: '收銀台', icon: <PointOfSale />, path: '/pos/register' },
    {
      text: '訂單',
      icon: <Receipt />,
      path: '/pos/orders',
      children: [
        { text: '訂單列表', icon: <Receipt />, path: '/pos/orders' },
        { text: '退款處理', icon: <Replay />, path: '/pos/refunds' },
      ],
    },
  ];
  const mobileDockItems = menuItems;

  const isItemActive = (item: NavItem) =>
    location.pathname.includes(item.path)
    || Boolean(item.children?.some((child) => location.pathname.includes(child.path)));

  const isChildActive = (item: NavItem) => location.pathname.includes(item.path);

  const handleMenuNavigate = (item: NavItem, isExpanded?: boolean) => {
    if (item.children) {
      setExpandedGroups((prev) => ({ ...prev, [item.text]: !isExpanded }));
      if (isExpanded && isItemActive(item)) return;
    }
    navigate(item.path);
    if (isMobile) setMobileOpen(false);
  };

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
          <Typography variant="subtitle1" fontWeight={900}>{terminalLabel}</Typography>
          <Typography variant="body2" color="text.secondary">{roleLabel} · {operatorName}</Typography>
        </Box>
      </Box>
      <List sx={{ flexGrow: 1, px: 2 }}>
        {menuItems.map((item) => {
          const isActive = isItemActive(item);
          const isExpanded = Boolean(item.children && (expandedGroups[item.text] ?? isActive));
          return (
            <Box key={item.text} sx={{ mb: 1 }}>
              <ListItem disablePadding>
                <ListItemButton
                  onClick={() => handleMenuNavigate(item, isExpanded)}
                  selected={isActive}
                  sx={{
                    borderRadius: 2,
                    minHeight: 56,
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
                      fontWeight: isActive ? 700 : 500,
                      color: isActive ? 'white' : 'text.secondary'
                    }}
                  />
                  {item.children && (
                    isExpanded
                      ? <KeyboardArrowDown fontSize="small" />
                      : <KeyboardArrowRight fontSize="small" />
                  )}
                </ListItemButton>
              </ListItem>
              {item.children && (
                <Collapse in={isExpanded} timeout="auto" unmountOnExit>
                  <List disablePadding sx={{ pt: 0.75 }}>
                    {item.children.map((child) => {
                      const childActive = isChildActive(child);
                      return (
                        <ListItem disablePadding key={child.text} sx={{ pl: 3.25, mb: 0.5 }}>
                          <ListItemButton
                            onClick={() => handleMenuNavigate(child)}
                            selected={childActive}
                            sx={{
                              borderRadius: 2,
                              minHeight: 48,
                              bgcolor: childActive ? 'rgba(255,109,0,0.16)' : 'rgba(255,255,255,0.03)',
                              color: childActive ? 'secondary.main' : 'text.secondary',
                              '&:hover': {
                                bgcolor: childActive ? 'rgba(255,109,0,0.22)' : 'rgba(255,255,255,0.07)',
                              },
                              '&.Mui-selected': {
                                bgcolor: 'rgba(255,109,0,0.16)',
                              },
                            }}
                          >
                            <ListItemText
                              primary={child.text}
                              primaryTypographyProps={{
                                fontSize: '0.92rem',
                                fontWeight: childActive ? 800 : 600,
                                color: childActive ? 'secondary.main' : 'text.secondary',
                              }}
                            />
                          </ListItemButton>
                        </ListItem>
                      );
                    })}
                  </List>
                </Collapse>
              )}
            </Box>
          );
        })}
      </List>
      <Divider sx={{ borderColor: 'rgba(255,255,255,0.1)' }} />
      <Box sx={{ p: 2 }}>
        <ListItem disablePadding>
           <ListItemButton sx={{ borderRadius: 2, minHeight: 56 }}>
             <ListItemIcon sx={{ minWidth: 40, color: 'text.secondary' }}><Settings /></ListItemIcon>
             <ListItemText primary="支援" sx={{ color: 'text.secondary' }} />
           </ListItemButton>
        </ListItem>
        <Button
            variant="outlined"
            fullWidth
            startIcon={<Settings />}
            onClick={handleOpenAdmin}
            sx={{
                mt: 1.5,
                minHeight: 52,
                color: 'text.primary',
                borderColor: 'rgba(178,198,255,0.32)',
                bgcolor: 'rgba(178,198,255,0.08)',
                '&:hover': { bgcolor: 'rgba(178,198,255,0.14)', borderColor: 'rgba(178,198,255,0.48)' }
            }}
        >
          管理後台
        </Button>
        <Button
            variant="outlined"
            fullWidth
            startIcon={<LockOutlined />}
            onClick={handleLockTerminal}
            sx={{ 
                mt: 2, 
                minHeight: 56,
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
            <Button
              aria-label="進入後台"
              variant="outlined"
              color="secondary"
              size="small"
              startIcon={<Settings />}
              onClick={handleOpenAdmin}
              sx={{
                minHeight: 40,
                px: { xs: 1.25, sm: 1.75 },
                fontWeight: 900,
                whiteSpace: 'nowrap',
                '& .MuiButton-startIcon': { mr: { xs: 0, sm: 0.75 } },
              }}
            >
              <Box component="span" sx={{ display: { xs: 'none', sm: 'inline' } }}>
                進入後台
              </Box>
            </Button>
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
                <MuiBadge
                  badgeContent={cartItemCount}
                  color="secondary"
                  invisible={cartItemCount === 0}
                  overlap="circular"
                >
                  <ShoppingCart />
                </MuiBadge>
              </IconButton>
            )}
            <Avatar sx={{ width: 36, height: 36, ml: 1 }}>{operatorInitial}</Avatar>
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
          pb: isTablet ? '72px' : (isMobile ? '76px' : 0)
      }}>
        {/* 桌面狀態列 / Desktop status bar */}
        {!isSmallScreen && (
           <Box sx={{ height: 64, display: 'flex', justifyContent: 'flex-end', alignItems: 'center', px: { lg: 2, xl: 4 }, gap: { lg: 1.25, xl: 2.5 }, minWidth: 0 }}>
             <Box sx={{ mr: 'auto', minWidth: 0 }}>
                <Typography variant="h6" fontWeight={900} noWrap sx={{ maxWidth: { lg: 220, xl: 420 } }}>{storeLabel}</Typography>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: { lg: 220, xl: 420 } }}>營業班次 · {terminalLabel}</Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'success.main', bgcolor: 'rgba(35, 193, 107, 0.1)', px: { lg: 1.25, xl: 2 }, py: 0.5, borderRadius: 5, flexShrink: 0 }}>
                 <Wifi fontSize="small" />
                 <Typography variant="caption" fontWeight="bold">線上</Typography>
              </Box>
              <Button
                aria-label="進入後台"
                variant="outlined"
                color="secondary"
                startIcon={<Settings />}
                onClick={handleOpenAdmin}
                sx={{ minHeight: 48, px: { lg: 1.5, xl: 2 }, fontWeight: 900, flexShrink: 0, whiteSpace: 'nowrap' }}
              >
                進入後台
              </Button>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, bgcolor: 'rgba(255,255,255,0.04)', px: 1.25, py: 0.75, borderRadius: 2, minHeight: 48, maxWidth: { lg: 190, xl: 240 }, minWidth: 0, flexShrink: 0 }}>
                <Avatar sx={{ width: 32, height: 32, cursor: 'pointer', flexShrink: 0 }}>{operatorInitial}</Avatar>
                <Box sx={{ minWidth: 0 }}>
                  <Typography variant="body2" fontWeight={800} sx={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>操作員 {operatorName}</Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <Circle sx={{ fontSize: 8, color: 'success.main', flexShrink: 0 }} />
                    <Typography variant="caption" color="text.secondary" sx={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{roleLabel}</Typography>
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
            value={menuItems.findIndex(i => isItemActive(i))}
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

      {/* 手機底部快速導覽 / Mobile quick dock */}
      {isMobile && (
        <Paper sx={{ position: 'fixed', bottom: 0, left: 0, right: 0, zIndex: 1200 }} elevation={6}>
          <BottomNavigation
            showLabels
            value={mobileDockItems.findIndex(i => isItemActive(i))}
            onChange={(_event, newValue) => {
              if (newValue >= 0) navigate(mobileDockItems[newValue].path);
            }}
            sx={{
              height: 76,
              bgcolor: 'background.paper',
              borderTop: '1px solid rgba(255,255,255,0.08)',
              '& .MuiBottomNavigationAction-root': {
                minWidth: 0,
                px: 0.5,
                pt: 1,
                pb: 1.25,
              },
              '& .MuiSvgIcon-root': {
                fontSize: 24,
              },
              '& .MuiBottomNavigationAction-label': {
                mt: 0.25,
                fontSize: '0.72rem',
                fontWeight: 800,
              },
            }}
          >
            {mobileDockItems.map((item) => (
              <BottomNavigationAction
                key={item.text}
                label={item.text}
                icon={item.icon}
                sx={{
                  color: 'text.secondary',
                  '&.Mui-selected': {
                    color: 'secondary.main',
                  },
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
