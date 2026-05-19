/**
 * @file AdminLayout.tsx
 * @description 後台管理版面 / Admin management layout
 * @description_en Provides the shared navigation shell for management pages
 * @description_zh 提供後台管理頁面的共用導覽與內容版面
 */
import {
  AppBar,
  Box,
  Button,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Tooltip,
  Typography,
  useTheme,
} from '@mui/material';
import ApartmentIcon from '@mui/icons-material/Apartment';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import BadgeIcon from '@mui/icons-material/Badge';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import DashboardIcon from '@mui/icons-material/Dashboard';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import LogoutIcon from '@mui/icons-material/Logout';
import ManageAccountsIcon from '@mui/icons-material/ManageAccounts';
import ManageSearchIcon from '@mui/icons-material/ManageSearch';
import PaymentsIcon from '@mui/icons-material/Payments';
import PeopleAltIcon from '@mui/icons-material/PeopleAlt';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import StorefrontIcon from '@mui/icons-material/Storefront';
import { useState } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { NotificationBell } from '@features/notification/components/NotificationBell';
import { useAuthStore } from '@shared/store/authStore';
import { ThemeModeToggle } from '@shared/components';

const drawerWidth = 272;
const drawerCollapsedWidth = 88;

const navGroups = [
  {
    title: '總覽',
    items: [
      { label: '後台首頁', path: '/admin/dashboard', icon: <DashboardIcon /> },
      { label: '進入收銀台', path: '/pos/register', icon: <StorefrontIcon /> },
    ],
  },
  {
    title: 'POS 管理',
    items: [
      { label: '商品管理', path: '/admin/pos/products', icon: <RestaurantMenuIcon /> },
      { label: '分類管理', path: '/admin/pos/categories', icon: <RestaurantMenuIcon /> },
      { label: '訂單列表', path: '/admin/pos/orders', icon: <ReceiptLongIcon /> },
      { label: '退款處理', path: '/admin/pos/refunds', icon: <ReceiptLongIcon /> },
      { label: '會員管理', path: '/admin/pos/members', icon: <PeopleAltIcon /> },
      { label: '促銷規則', path: '/admin/pos/promotions', icon: <LocalOfferIcon /> },
    ],
  },
  {
    title: '庫存',
    items: [
      { label: '庫存總覽', path: '/admin/inventory/overview', icon: <Inventory2Icon /> },
      { label: '進貨驗收', path: '/admin/inventory/receiving', icon: <LocalShippingIcon /> },
      { label: '盤點單', path: '/admin/inventory/stock-takes', icon: <Inventory2Icon /> },
      { label: '調撥管理', path: '/admin/inventory/transfers', icon: <LocalShippingIcon /> },
    ],
  },
  {
    title: '營運',
    items: [
      { label: '發票作業', path: '/admin/operations/invoices', icon: <ReceiptLongIcon /> },
      { label: '發票字軌', path: '/admin/operations/invoice-tracks', icon: <ReceiptLongIcon /> },
      { label: '稅別設定', path: '/admin/operations/tax-classes', icon: <ReceiptLongIcon /> },
      { label: '班次管理', path: '/admin/operations/shifts', icon: <BadgeIcon /> },
      { label: 'Z 報表', path: '/admin/operations/z-reports', icon: <ReceiptLongIcon /> },
      { label: '對帳', path: '/admin/operations/reconciliation', icon: <PaymentsIcon /> },
      { label: '支付方式', path: '/admin/operations/pay-methods', icon: <PaymentsIcon /> },
      { label: '金流設定', path: '/admin/operations/gateways', icon: <PaymentsIcon /> },
    ],
  },
  {
    title: '組織',
    items: [
      { label: '公司', path: '/admin/organization/company', icon: <ApartmentIcon /> },
      { label: '部門', path: '/admin/organization/departments', icon: <ApartmentIcon /> },
      { label: '職位', path: '/admin/organization/positions', icon: <BadgeIcon /> },
      { label: '員工', path: '/admin/organization/employees', icon: <BadgeIcon /> },
    ],
  },
  {
    title: '權限與流程',
    items: [
      { label: '角色權限', path: '/admin/access/roles', icon: <ManageAccountsIcon /> },
      { label: '帳號管理', path: '/admin/access/users', icon: <ManageAccountsIcon /> },
      { label: '稽核紀錄', path: '/admin/access/audit-logs', icon: <ManageSearchIcon /> },
      { label: '流程定義', path: '/admin/workflow/definitions', icon: <AssignmentTurnedInIcon /> },
      { label: '我的待辦', path: '/admin/workflow/tasks', icon: <AssignmentTurnedInIcon /> },
    ],
  },
  {
    title: '人事',
    items: [
      { label: '假別', path: '/admin/leave/types', icon: <CalendarMonthIcon /> },
      { label: '請假餘額', path: '/admin/leave/balances', icon: <CalendarMonthIcon /> },
      { label: '請假申請', path: '/admin/leave/request', icon: <CalendarMonthIcon /> },
      { label: '請假日曆', path: '/admin/leave/calendar', icon: <CalendarMonthIcon /> },
    ],
  },
];

/**
 * 後台導覽項目 / Admin navigation item
 */
const AdminNavItem = ({
  label,
  path,
  icon,
  collapsed = false,
}: {
  label: string;
  path: string;
  icon: React.ReactNode;
  collapsed?: boolean;
}) => {
  const location = useLocation();
  const theme = useTheme();
  const selected = location.pathname === path;
  const isLightMode = theme.palette.mode === 'light';
  const selectedColor = isLightMode ? '#7C2D12' : '#FFFFFF';
  const selectedBg = isLightMode ? 'rgba(255, 109, 0, 0.16)' : 'rgba(255, 109, 0, 0.22)';
  const selectedHoverBg = isLightMode ? 'rgba(255, 109, 0, 0.22)' : 'rgba(255, 109, 0, 0.28)';

  const navButton = (
    <ListItemButton
      component={Link}
      to={path}
      selected={selected}
      aria-label={label}
      sx={{
        borderRadius: 1,
        minHeight: 44,
        mb: 0.5,
        px: collapsed ? 1 : 2,
        justifyContent: collapsed ? 'center' : 'flex-start',
        color: selected ? selectedColor : 'text.secondary',
        '&.Mui-selected': {
          bgcolor: selectedBg,
          color: selectedColor,
          border: '1px solid rgba(255, 109, 0, 0.52)',
        },
        '&.Mui-selected:hover': {
          bgcolor: selectedHoverBg,
        },
        '&:hover': {
          bgcolor: selected ? selectedHoverBg : (isLightMode ? 'rgba(112,72,232,0.06)' : 'rgba(255,255,255,0.05)'),
        },
      }}
    >
      <ListItemIcon
        sx={{
          minWidth: collapsed ? 0 : 40,
          color: 'inherit',
          justifyContent: 'center',
        }}
      >
        {icon}
      </ListItemIcon>
      {!collapsed && (
        <ListItemText primary={label} slotProps={{ primary: { fontWeight: selected ? 800 : 600 } }} />
      )}
    </ListItemButton>
  );

  if (!collapsed) {
    return navButton;
  }

  return (
    <Tooltip title={label} placement="right" arrow>
      {navButton}
    </Tooltip>
  );
};

const AdminSidebar = ({
  collapsed = false,
  onToggle,
}: {
  collapsed?: boolean;
  onToggle?: () => void;
}) => {
  const theme = useTheme();
  const isLightMode = theme.palette.mode === 'light';
  const sidebarWidth = collapsed ? drawerCollapsedWidth : drawerWidth;

  return (
    <Box
      sx={{
        width: sidebarWidth,
        height: '100%',
        px: collapsed ? 1.25 : 2,
        py: 2.5,
        bgcolor: 'background.paper',
        overflowY: 'auto',
        overflowX: 'hidden',
        transition: theme.transitions.create(['width', 'padding'], {
          duration: theme.transitions.duration.shorter,
        }),
      }}
    >
      <Box
        sx={{
          px: collapsed ? 0 : 1,
          mb: 3,
          display: 'flex',
          alignItems: 'center',
          justifyContent: collapsed ? 'center' : 'space-between',
          gap: 1,
        }}
      >
        {!collapsed && (
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="h5" fontWeight={900}>
              Titanium POS
            </Typography>
            <Typography color="text.secondary" fontWeight={700}>
              後台管理
            </Typography>
          </Box>
        )}
        {onToggle && (
          <Tooltip title={collapsed ? '展開側邊欄' : '收合側邊欄'} placement="right" arrow>
            <IconButton
              aria-label={collapsed ? '展開側邊欄' : '收合側邊欄'}
              onClick={onToggle}
              sx={{
                width: 44,
                height: 44,
                border: isLightMode ? '1px solid rgba(17,24,39,0.12)' : '1px solid rgba(255,255,255,0.14)',
                color: 'text.secondary',
                bgcolor: isLightMode ? 'rgba(17,24,39,0.03)' : 'rgba(255,255,255,0.04)',
                '&:hover': {
                  bgcolor: isLightMode ? 'rgba(112,72,232,0.08)' : 'rgba(255,255,255,0.08)',
                },
              }}
            >
              {collapsed ? <ChevronRightIcon /> : <ChevronLeftIcon />}
            </IconButton>
          </Tooltip>
        )}
      </Box>

      {navGroups.map((group) => (
        <Box key={group.title} sx={{ mb: collapsed ? 1.25 : 2.5 }}>
          {!collapsed && (
            <Typography
              sx={{
                px: 1,
                mb: 1,
                fontSize: 13,
                fontWeight: 900,
                color: isLightMode ? '#6B7280' : '#D4D6E2',
              }}
            >
              {group.title}
            </Typography>
          )}
          <List dense disablePadding>
            {group.items.map((item) => (
              <AdminNavItem key={item.path} {...item} collapsed={collapsed} />
            ))}
          </List>
        </Box>
      ))}
    </Box>
  );
};

/**
 * 後台版面 / Admin layout
 */
export default function AdminLayout() {
  const logout = useAuthStore((state) => state.logout);
  const theme = useTheme();
  const isLightMode = theme.palette.mode === 'light';
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const currentDrawerWidth = sidebarCollapsed ? drawerCollapsedWidth : drawerWidth;

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', bgcolor: 'background.default' }}>
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          width: currentDrawerWidth,
          flexShrink: 0,
          transition: theme.transitions.create('width', {
            duration: theme.transitions.duration.shorter,
          }),
          '& .MuiDrawer-paper': {
            width: currentDrawerWidth,
            boxSizing: 'border-box',
            borderRight: isLightMode ? '1px solid rgba(17,24,39,0.08)' : '1px solid rgba(255,255,255,0.08)',
            transition: theme.transitions.create('width', {
              duration: theme.transitions.duration.shorter,
            }),
          },
        }}
      >
        <AdminSidebar
          collapsed={sidebarCollapsed}
          onToggle={() => setSidebarCollapsed((current) => !current)}
        />
      </Drawer>

      <Box sx={{ flex: 1, minWidth: 0 }}>
        <AppBar
          position="sticky"
          color="transparent"
          elevation={0}
          sx={{
            bgcolor: isLightMode ? 'rgba(255,255,255,0.92)' : 'rgba(23, 26, 33, 0.94)',
            borderBottom: isLightMode ? '1px solid rgba(17,24,39,0.08)' : '1px solid rgba(255,255,255,0.08)',
            backdropFilter: 'blur(12px)',
          }}
        >
          <Toolbar sx={{ minHeight: 72, gap: 1.5 }}>
            <Typography variant="h6" fontWeight={900} sx={{ flexGrow: 1 }}>
              管理後台
            </Typography>
            <ThemeModeToggle />
            <NotificationBell />
            <Button component={Link} to="/pos/register" variant="outlined" color="secondary">
              收銀台
            </Button>
            <Button
              component={Link}
              to="/login"
              onClick={logout}
              variant="contained"
              color="secondary"
              startIcon={<LogoutIcon />}
            >
              登出
            </Button>
          </Toolbar>
        </AppBar>

        <Box component="main" sx={{ p: { xs: 2, md: 4 }, maxWidth: 1440 }}>
          <Box sx={{ display: { xs: 'block', md: 'none' }, mb: 2 }}>
            <AdminSidebar />
          </Box>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
