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
  Paper, Button, BottomNavigation, BottomNavigationAction, Badge as MuiBadge,
  Tooltip
} from '@mui/material';
import {
  PointOfSale, Receipt, Settings, Replay, LockOutlined, Menu as MenuIcon,
  ShoppingCart, Wifi, Circle, KeyboardArrowDown, KeyboardArrowRight
} from '@mui/icons-material';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@shared/store/authStore';
import { ThemeModeToggle } from '@shared/components';
import { readPosSession } from '@features/pos-orders/posSession';
import { useCartStore } from '@features/pos-orders/store/cartStore';

const SIDEBAR_EXPANDED_WIDTH = 240;
const SIDEBAR_COLLAPSED_WIDTH = 88;
const CART_WIDTH = 340;
const DESKTOP_STATUS_BAR_HEIGHT = 96;

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
  const isLightMode = theme.palette.mode === 'light';
  
  const [mobileOpen, setMobileOpen] = useState(false);
  const [cartOpen, setCartOpen] = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [expandedGroups, setExpandedGroups] = useState<Record<string, boolean>>({});
  const desktopSidebarWidth = sidebarCollapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH;
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

  const handleSidebarCollapseToggle = () => {
    setSidebarCollapsed((current) => !current);
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

  const renderSidebarContent = (collapsed = false, allowCollapseToggle = false) => (
    <Box
      data-testid="pos-sidebar"
      data-collapsed={collapsed ? 'true' : 'false'}
      style={{
        width: collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH,
        minWidth: collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH,
        maxWidth: collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH,
      }}
      sx={{
        height: '100%',
        width: collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH,
        minWidth: collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH,
        maxWidth: collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH,
        display: 'flex',
        flexDirection: 'column',
        bgcolor: 'background.paper',
        overflowX: 'hidden',
        overflowY: 'auto',
        transition: theme.transitions.create('width', {
          duration: theme.transitions.duration.shorter,
        }),
      }}
    >
      <Box
        sx={{
          p: collapsed ? 1.5 : 2.5,
          display: 'flex',
          alignItems: 'center',
          justifyContent: collapsed ? 'center' : 'space-between',
          gap: 1.5,
        }}
      >
        {!collapsed && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, minWidth: 0 }}>
            <Avatar sx={{ bgcolor: 'rgba(112,72,232,0.18)', color: '#B2C6FF', width: 40, height: 40 }} variant="rounded">
              <PointOfSale />
            </Avatar>
            <Box sx={{ minWidth: 0 }}>
              <Typography variant="h6" color="text.primary" fontWeight={900} sx={{ lineHeight: 1.1 }} noWrap>
                Titanium POS
              </Typography>
              <Typography variant="caption" color="text.secondary" fontWeight={700} noWrap>
                咖啡門市收銀台
              </Typography>
            </Box>
          </Box>
        )}
        {allowCollapseToggle && (
          <Tooltip title={collapsed ? '展開側邊欄' : '收合側邊欄'} placement="right" arrow>
            <IconButton
              aria-label={collapsed ? '展開側邊欄' : '收合側邊欄'}
              onClick={handleSidebarCollapseToggle}
              sx={{
                width: 44,
                height: 44,
                border: isLightMode ? '1px solid rgba(17,24,39,0.12)' : '1px solid rgba(255,255,255,0.12)',
                bgcolor: isLightMode ? 'rgba(17,24,39,0.04)' : 'rgba(255,255,255,0.04)',
                color: 'text.secondary',
                '&:hover': {
                  bgcolor: isLightMode ? 'rgba(112,72,232,0.08)' : 'rgba(178,198,255,0.1)',
                },
              }}
            >
              <MenuIcon data-testid="pos-sidebar-menu-toggle-icon" />
            </IconButton>
          </Tooltip>
        )}
      </Box>
      {!collapsed && (
        <Box sx={{
          mx: 2,
          p: 2,
          display: 'flex',
          alignItems: 'center',
          gap: 2,
          mb: 2,
          borderRadius: 3,
          bgcolor: isLightMode ? 'rgba(112,72,232,0.06)' : 'rgba(255,255,255,0.04)',
          border: isLightMode ? '1px solid rgba(112,72,232,0.12)' : '1px solid rgba(255,255,255,0.05)'
        }}>
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="subtitle1" fontWeight={900} noWrap>{terminalLabel}</Typography>
            <Typography variant="body2" color="text.secondary" noWrap>{roleLabel} · {operatorName}</Typography>
          </Box>
        </Box>
      )}
      <List sx={{ flexGrow: 1, px: collapsed ? 1.25 : 2 }}>
        {menuItems.map((item) => {
          const isActive = isItemActive(item);
          const isExpanded = Boolean(item.children && (expandedGroups[item.text] ?? isActive));
          const navButton = (
            <ListItemButton
              aria-label={item.text}
              onClick={() => handleMenuNavigate(item, isExpanded)}
              selected={isActive}
              sx={{
                borderRadius: 2,
                minHeight: 56,
                px: collapsed ? 1 : 2,
                justifyContent: collapsed ? 'center' : 'flex-start',
                bgcolor: isActive ? 'primary.main' : 'transparent',
                background: isActive ? 'linear-gradient(90deg, #7048E8 0%, #4D329A 100%)' : 'transparent',
                '&:hover': {
                  bgcolor: isActive ? 'primary.dark' : (isLightMode ? 'rgba(112,72,232,0.06)' : 'rgba(255,255,255,0.05)'),
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
              <ListItemIcon sx={{ color: isActive ? 'white' : 'text.secondary', minWidth: collapsed ? 0 : 40, justifyContent: 'center' }}>
                {item.icon}
              </ListItemIcon>
              {!collapsed && (
                <ListItemText
                  primary={item.text}
                  primaryTypographyProps={{
                    fontWeight: isActive ? 700 : 500,
                    color: isActive ? 'white' : 'text.secondary'
                  }}
                />
              )}
              {!collapsed && item.children && (
                isExpanded
                  ? <KeyboardArrowDown fontSize="small" />
                  : <KeyboardArrowRight fontSize="small" />
              )}
            </ListItemButton>
          );

          return (
            <Box key={item.text} sx={{ mb: 1 }}>
              <ListItem disablePadding>
                {collapsed ? (
                  <Tooltip title={item.text} placement="right" arrow>
                    {navButton}
                  </Tooltip>
                ) : navButton}
              </ListItem>
              {!collapsed && item.children && (
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
                              bgcolor: childActive ? 'rgba(255,109,0,0.16)' : (isLightMode ? 'rgba(17,24,39,0.03)' : 'rgba(255,255,255,0.03)'),
                              color: childActive ? 'secondary.main' : 'text.secondary',
                              '&:hover': {
                                bgcolor: childActive ? 'rgba(255,109,0,0.22)' : (isLightMode ? 'rgba(17,24,39,0.07)' : 'rgba(255,255,255,0.07)'),
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
      <Divider sx={{ borderColor: isLightMode ? 'rgba(17,24,39,0.1)' : 'rgba(255,255,255,0.1)' }} />
      <Box sx={{ p: collapsed ? 1.25 : 2 }}>
        <Tooltip title={collapsed ? '管理後台' : ''} placement="right" arrow disableHoverListener={!collapsed}>
          <Button
            aria-label="管理後台"
            variant="outlined"
            fullWidth
            startIcon={collapsed ? undefined : <Settings />}
            onClick={handleOpenAdmin}
            sx={{
                mt: 1.5,
                minHeight: 52,
                minWidth: 0,
                px: collapsed ? 0 : 2,
                color: 'text.primary',
                borderColor: 'rgba(178,198,255,0.32)',
                bgcolor: isLightMode ? 'rgba(112,72,232,0.06)' : 'rgba(178,198,255,0.08)',
                '&:hover': { bgcolor: isLightMode ? 'rgba(112,72,232,0.1)' : 'rgba(178,198,255,0.14)', borderColor: 'rgba(178,198,255,0.48)' }
            }}
          >
            {collapsed ? <Settings /> : '管理後台'}
          </Button>
        </Tooltip>
        <Tooltip title={collapsed ? '鎖定終端' : ''} placement="right" arrow disableHoverListener={!collapsed}>
          <Button
            aria-label="鎖定終端"
            variant="outlined"
            fullWidth
            startIcon={collapsed ? undefined : <LockOutlined />}
            onClick={handleLockTerminal}
            sx={{ 
                mt: 2, 
                minHeight: 56,
                minWidth: 0,
                px: collapsed ? 0 : 2,
                color: 'text.secondary', 
                borderColor: 'rgba(255,255,255,0.2)',
                bgcolor: isLightMode ? 'rgba(17,24,39,0.03)' : 'rgba(0,0,0,0.2)',
                '&:hover': { bgcolor: isLightMode ? 'rgba(17,24,39,0.07)' : 'rgba(0,0,0,0.4)', borderColor: 'rgba(255,255,255,0.3)' }
            }}
          >
            {collapsed ? <LockOutlined /> : '鎖定終端'}
          </Button>
        </Tooltip>
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
            <ThemeModeToggle />
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
        <Box
          component="aside"
          data-testid="pos-desktop-sidebar-shell"
          style={{
            width: desktopSidebarWidth,
            minWidth: desktopSidebarWidth,
            maxWidth: desktopSidebarWidth,
          }}
          sx={{
            width: desktopSidebarWidth,
            minWidth: desktopSidebarWidth,
            maxWidth: desktopSidebarWidth,
            flexShrink: 0,
            height: '100vh',
            bgcolor: 'background.paper',
            borderRight: isLightMode ? '1px solid rgba(17,24,39,0.08)' : '1px solid rgba(255,255,255,0.05)',
            overflow: 'hidden',
            transition: theme.transitions.create('width', {
              duration: theme.transitions.duration.shorter,
            }),
          }}
        >
          {renderSidebarContent(sidebarCollapsed, true)}
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
            {renderSidebarContent(false, false)}
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
           <Box sx={{ height: DESKTOP_STATUS_BAR_HEIGHT, display: 'flex', justifyContent: 'flex-end', alignItems: 'center', px: { lg: 3, xl: 5 }, gap: { lg: 1.5, xl: 2.5 }, minWidth: 0 }}>
             <Box sx={{ mr: 'auto', minWidth: 0 }}>
                <Typography variant="h6" fontWeight={900} noWrap sx={{ maxWidth: { lg: 300, xl: 460 } }}>{storeLabel}</Typography>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: { lg: 300, xl: 460 } }}>營業班次 · {terminalLabel}</Typography>
              </Box>
              <ThemeModeToggle />
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'success.main', bgcolor: 'rgba(35, 193, 107, 0.1)', px: 2, py: 0.5, borderRadius: 5, minHeight: 44, flexShrink: 0 }}>
                 <Wifi fontSize="small" />
                 <Typography variant="caption" fontWeight="bold">線上</Typography>
              </Box>
              <Button
                aria-label="進入後台"
                variant="outlined"
                color="secondary"
                startIcon={<Settings />}
                onClick={handleOpenAdmin}
                sx={{ minHeight: 50, px: 2.25, fontWeight: 900, flexShrink: 0, whiteSpace: 'nowrap' }}
              >
                進入後台
              </Button>
              <Box
                sx={{
                  position: 'relative',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1.5,
                  bgcolor: isLightMode ? 'rgba(17,24,39,0.06)' : 'rgba(255,255,255,0.04)',
                  px: 1.75,
                  py: 0.75,
                  ml: { lg: 1.25, xl: 1.75 },
                  borderRadius: 2,
                  minHeight: 50,
                  maxWidth: { lg: 230, xl: 260 },
                  minWidth: 0,
                  flexShrink: 0,
                  '&::before': {
                    content: '""',
                    position: 'absolute',
                    left: { lg: -18, xl: -22 },
                    top: 10,
                    bottom: 10,
                    width: '1px',
                    bgcolor: isLightMode ? 'rgba(17,24,39,0.14)' : 'rgba(255,255,255,0.12)',
                  },
                }}
              >
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
        <Box sx={{ p: isSmallScreen ? 2 : 4, pt: isSmallScreen ? 2 : 0, height: !isSmallScreen ? `calc(100vh - ${DESKTOP_STATUS_BAR_HEIGHT}px)` : '100%' }}>
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
