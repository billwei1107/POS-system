/**
 * @file AdminDashboardPage.tsx
 * @description 後台首頁 / Admin dashboard page
 * @description_en Shows the management entry overview and quick links
 * @description_zh 顯示後台管理入口總覽與常用功能捷徑
 */
import type { ReactNode } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Divider,
  Stack,
  Typography,
} from '@mui/material';
import ApartmentIcon from '@mui/icons-material/Apartment';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import BadgeIcon from '@mui/icons-material/Badge';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import ManageAccountsIcon from '@mui/icons-material/ManageAccounts';
import ManageSearchIcon from '@mui/icons-material/ManageSearch';
import PaymentsIcon from '@mui/icons-material/Payments';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import StorefrontIcon from '@mui/icons-material/Storefront';
import { Link } from 'react-router-dom';

interface DashboardLink {
  label: string;
  path: string;
  icon: ReactNode;
}

const summaryItems = [
  { label: '組織資料', value: '4', helper: '公司、部門、職位、員工' },
  { label: '權限流程', value: '4', helper: '角色、稽核、流程、待辦' },
  { label: '人事入口', value: '4', helper: '假別、餘額、申請、日曆' },
  { label: 'POS 管理', value: '6', helper: '商品、分類、訂單、退款、會員、促銷' },
  { label: '庫存管理', value: '4', helper: '庫存、進貨、盤點、調撥' },
  { label: '營運設定', value: '8', helper: '發票、稅別、班次、支付、對帳' },
];

const storefrontLinks: DashboardLink[] = [
  { label: '進入收銀台', path: '/pos/register', icon: <StorefrontIcon /> },
  { label: '門市訂單', path: '/pos/orders', icon: <ReceiptLongIcon /> },
  { label: '退款處理', path: '/pos/refunds', icon: <ReceiptLongIcon /> },
];

const backOfficeLinks: DashboardLink[] = [
  { label: '商品管理', path: '/admin/pos/products', icon: <RestaurantMenuIcon /> },
  { label: '庫存總覽', path: '/admin/inventory/overview', icon: <Inventory2Icon /> },
  { label: '盤點單', path: '/admin/inventory/stock-takes', icon: <Inventory2Icon /> },
  { label: '訂單列表', path: '/admin/pos/orders', icon: <ReceiptLongIcon /> },
  { label: '會員管理', path: '/admin/pos/members', icon: <ManageAccountsIcon /> },
  { label: '對帳', path: '/admin/operations/reconciliation', icon: <PaymentsIcon /> },
  { label: '員工管理', path: '/admin/organization/employees', icon: <BadgeIcon /> },
  { label: '部門管理', path: '/admin/organization/departments', icon: <ApartmentIcon /> },
  { label: '角色權限', path: '/admin/access/roles', icon: <ManageAccountsIcon /> },
  { label: '稽核紀錄', path: '/admin/access/audit-logs', icon: <ManageSearchIcon /> },
  { label: '流程定義', path: '/admin/workflow/definitions', icon: <AssignmentTurnedInIcon /> },
  { label: '請假日曆', path: '/admin/leave/calendar', icon: <CalendarMonthIcon /> },
];

const renderLinkGrid = (items: DashboardLink[]) => (
  <Box
    sx={{
      display: 'grid',
      gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, minmax(0, 1fr))', lg: 'repeat(3, minmax(0, 1fr))' },
      gap: 1.5,
    }}
  >
    {items.map((item) => (
      <Button
        key={item.path}
        component={Link}
        to={item.path}
        variant="outlined"
        color="secondary"
        startIcon={item.icon}
        sx={{
          justifyContent: 'flex-start',
          minHeight: 56,
          px: 2,
          bgcolor: item.path.startsWith('/pos/')
            ? 'rgba(178,198,255,0.06)'
            : 'rgba(255, 109, 0, 0.06)',
        }}
      >
        {item.label}
      </Button>
    ))}
  </Box>
);

/**
 * 後台首頁 / Admin dashboard
 */
export default function AdminDashboardPage() {
  return (
    <Stack spacing={3}>
      <Box>
        <Stack direction="row" alignItems="center" spacing={1.5} sx={{ mb: 1 }}>
          <Typography variant="h3" fontWeight={900}>
            後台首頁
          </Typography>
          <Chip label="Admin" color="secondary" />
        </Stack>
        <Typography color="text.secondary" fontWeight={600}>
          集中管理 POS 營運、商品、庫存、支付、發票、組織、人員與流程資料。
        </Typography>
      </Box>

      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: { xs: '1fr', md: 'repeat(3, minmax(0, 1fr))' },
          gap: 2,
        }}
      >
        {summaryItems.map((item) => (
          <Card key={item.label} sx={{ borderRadius: 1, border: '1px solid rgba(255,255,255,0.08)' }}>
            <CardContent>
              <Typography color="text.secondary" fontWeight={800}>
                {item.label}
              </Typography>
              <Typography variant="h3" fontWeight={900} sx={{ mt: 1 }}>
                {item.value}
              </Typography>
              <Typography color="text.secondary" fontWeight={600}>
                {item.helper}
              </Typography>
            </CardContent>
          </Card>
        ))}
      </Box>

      <Card sx={{ borderRadius: 1, border: '1px solid rgba(178,198,255,0.12)' }}>
        <CardContent>
          <Stack direction={{ xs: 'column', md: 'row' }} alignItems={{ xs: 'flex-start', md: 'center' }} spacing={1.5} sx={{ mb: 2 }}>
            <Box sx={{ flexGrow: 1 }}>
              <Typography variant="h5" fontWeight={900}>
                門市前台
              </Typography>
              <Typography color="text.secondary" fontWeight={600}>
                收銀、訂單查詢與退款處理保留在 POS 前台。
              </Typography>
            </Box>
            <Chip label="Foreground POS" color="primary" variant="outlined" />
          </Stack>
          <Divider sx={{ mb: 2 }} />
          {renderLinkGrid(storefrontLinks)}
        </CardContent>
      </Card>

      <Card sx={{ borderRadius: 1, border: '1px solid rgba(255,255,255,0.08)' }}>
        <CardContent>
          <Typography variant="h5" fontWeight={900} sx={{ mb: 2 }}>
            後台管理
          </Typography>
          <Divider sx={{ mb: 2 }} />
          {renderLinkGrid(backOfficeLinks)}
        </CardContent>
      </Card>
    </Stack>
  );
}
