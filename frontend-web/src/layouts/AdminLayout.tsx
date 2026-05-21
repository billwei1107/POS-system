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
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import AccountTreeIcon from '@mui/icons-material/AccountTree';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import AssessmentIcon from '@mui/icons-material/Assessment';
import BadgeIcon from '@mui/icons-material/Badge';
import BalanceIcon from '@mui/icons-material/Balance';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import CategoryIcon from '@mui/icons-material/Category';
import ConfirmationNumberIcon from '@mui/icons-material/ConfirmationNumber';
import CurrencyExchangeIcon from '@mui/icons-material/CurrencyExchange';
import DashboardIcon from '@mui/icons-material/Dashboard';
import EventAvailableIcon from '@mui/icons-material/EventAvailable';
import EventNoteIcon from '@mui/icons-material/EventNote';
import FactCheckIcon from '@mui/icons-material/FactCheck';
import HistoryIcon from '@mui/icons-material/History';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import LogoutIcon from '@mui/icons-material/Logout';
import MenuIcon from '@mui/icons-material/Menu';
import ManageAccountsIcon from '@mui/icons-material/ManageAccounts';
import PaymentsIcon from '@mui/icons-material/Payments';
import PeopleAltIcon from '@mui/icons-material/PeopleAlt';
import PercentIcon from '@mui/icons-material/Percent';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import RequestQuoteIcon from '@mui/icons-material/RequestQuote';
import ScheduleIcon from '@mui/icons-material/Schedule';
import SchemaIcon from '@mui/icons-material/Schema';
import SettingsSuggestIcon from '@mui/icons-material/SettingsSuggest';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import StorefrontIcon from '@mui/icons-material/Storefront';
import WorkIcon from '@mui/icons-material/Work';
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
      { label: '分類管理', path: '/admin/pos/categories', icon: <CategoryIcon /> },
      { label: '訂單列表', path: '/admin/pos/orders', icon: <ReceiptLongIcon /> },
      { label: '退款處理', path: '/admin/pos/refunds', icon: <CurrencyExchangeIcon /> },
      { label: '會員管理', path: '/admin/pos/members', icon: <PeopleAltIcon /> },
      { label: '促銷規則', path: '/admin/pos/promotions', icon: <LocalOfferIcon /> },
    ],
  },
  {
    title: '庫存',
    items: [
      { label: '庫存總覽', path: '/admin/inventory/overview', icon: <Inventory2Icon /> },
      { label: '進貨驗收', path: '/admin/inventory/receiving', icon: <LocalShippingIcon /> },
      { label: '盤點單', path: '/admin/inventory/stock-takes', icon: <FactCheckIcon /> },
      { label: '調撥管理', path: '/admin/inventory/transfers', icon: <SwapHorizIcon /> },
    ],
  },
  {
    title: '營運',
    items: [
      { label: '發票作業', path: '/admin/operations/invoices', icon: <RequestQuoteIcon /> },
      { label: '發票字軌', path: '/admin/operations/invoice-tracks', icon: <ConfirmationNumberIcon /> },
      { label: '稅別設定', path: '/admin/operations/tax-classes', icon: <PercentIcon /> },
      { label: '班次管理', path: '/admin/operations/shifts', icon: <ScheduleIcon /> },
      { label: 'Z 報表', path: '/admin/operations/z-reports', icon: <AssessmentIcon /> },
      { label: '對帳', path: '/admin/operations/reconciliation', icon: <BalanceIcon /> },
      { label: '支付方式', path: '/admin/operations/pay-methods', icon: <PaymentsIcon /> },
      { label: '金流設定', path: '/admin/operations/gateways', icon: <SettingsSuggestIcon /> },
    ],
  },
  {
    title: '組織',
    items: [
      { label: '公司', path: '/admin/organization/company', icon: <ApartmentIcon /> },
      { label: '部門', path: '/admin/organization/departments', icon: <AccountTreeIcon /> },
      { label: '職位', path: '/admin/organization/positions', icon: <WorkIcon /> },
      { label: '員工', path: '/admin/organization/employees', icon: <BadgeIcon /> },
    ],
  },
  {
    title: '權限與流程',
    items: [
      { label: '角色權限', path: '/admin/access/roles', icon: <AdminPanelSettingsIcon /> },
      { label: '帳號管理', path: '/admin/access/users', icon: <ManageAccountsIcon /> },
      { label: '稽核紀錄', path: '/admin/access/audit-logs', icon: <HistoryIcon /> },
      { label: '流程定義', path: '/admin/workflow/definitions', icon: <SchemaIcon /> },
      { label: '我的待辦', path: '/admin/workflow/tasks', icon: <AssignmentTurnedInIcon /> },
    ],
  },
  {
    title: '人事',
    items: [
      { label: '假別', path: '/admin/leave/types', icon: <EventNoteIcon /> },
      { label: '請假餘額', path: '/admin/leave/balances', icon: <AccountBalanceWalletIcon /> },
      { label: '請假申請', path: '/admin/leave/request', icon: <EventAvailableIcon /> },
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
              <MenuIcon data-testid="admin-sidebar-menu-toggle-icon" />
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
