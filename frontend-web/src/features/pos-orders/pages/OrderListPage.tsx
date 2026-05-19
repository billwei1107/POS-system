/**
 * @file OrderListPage.tsx
 * @description 訂單列表頁面 / Order list page
 * @description_en Management view for listing, filtering and voiding POS orders
 * @description_zh POS 訂單管理列表頁，支援狀態篩選、分頁與作廢操作
 */
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, Select, MenuItem, FormControl,
  InputLabel, Pagination, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, CircularProgress, Alert, Card, CardContent,
} from '@mui/material';
import { orderApi } from '../api/orderApi';
import type { Order, OrderStatus, OrderListParams } from '../types';
import { useLocation, useNavigate } from 'react-router-dom';
import { paymentApi } from '../../pos-payment/api/paymentApi';
import type { PaymentTransaction } from '../../pos-payment/types';
import { invoiceApi } from '../../pos-tax/api/taxApi';
import type { Invoice } from '../../pos-tax/types';
import { formatDateTime } from '@shared/utils';
import { getActivePosContext } from '../posSession';

// ========================================
// 狀態顏色映射 / Status color mapping
// ========================================
const STATUS_COLOR: Record<OrderStatus, 'default' | 'primary' | 'secondary' | 'error' | 'warning' | 'success' | 'info'> = {
  DRAFT: 'default',
  CONFIRMED: 'info',
  PREPARING: 'warning',
  READY: 'primary',
  COMPLETED: 'success',
  CLOSED: 'default',
  VOIDED: 'error',
};

const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
  DRAFT: '草稿',
  CONFIRMED: '已確認',
  PREPARING: '製作中',
  READY: '待取餐',
  COMPLETED: '已完成',
  CLOSED: '已關帳',
  VOIDED: '已作廢',
};

type PaymentStatusSummary = 'PAID' | 'UNPAID' | 'REFUNDED' | 'FAILED';
type InvoiceStatusSummary = 'ISSUED' | 'VOIDED' | 'ALLOWANCE' | 'MISSING';

const PAYMENT_STATUS_LABEL: Record<PaymentStatusSummary, string> = {
  PAID: '已付款',
  UNPAID: '未付款',
  REFUNDED: '已退款',
  FAILED: '付款異常',
};

const PAYMENT_STATUS_COLOR: Record<PaymentStatusSummary, 'default' | 'success' | 'warning' | 'error'> = {
  PAID: 'success',
  UNPAID: 'default',
  REFUNDED: 'warning',
  FAILED: 'error',
};

const INVOICE_STATUS_LABEL: Record<InvoiceStatusSummary, string> = {
  ISSUED: '已開立',
  VOIDED: '已作廢',
  ALLOWANCE: '折讓',
  MISSING: '未開立',
};

const INVOICE_STATUS_COLOR: Record<InvoiceStatusSummary, 'default' | 'success' | 'warning' | 'error'> = {
  ISSUED: 'success',
  VOIDED: 'error',
  ALLOWANCE: 'warning',
  MISSING: 'default',
};

const ORDER_TABLE_MIN_WIDTH = 1180;
const ORDER_NO_COLUMN_WIDTH = 190;
const ORDER_TABLE_COLUMN_WIDTHS = {
  status: 124,
  orderType: 94,
  itemCount: 60,
  payment: 100,
  invoice: 100,
  discount: 142,
  total: 84,
  createdAt: 142,
  actions: 144,
} as const;

const tableHeaderSx = {
  whiteSpace: 'nowrap',
  fontWeight: 900,
  bgcolor: 'background.paper',
} as const;

const tableBodyCellSx = {
  whiteSpace: 'nowrap',
  verticalAlign: 'middle',
} as const;

const stickyOrderCellSx = {
  ...tableBodyCellSx,
  position: 'sticky',
  left: 0,
  zIndex: 2,
  width: ORDER_NO_COLUMN_WIDTH,
  minWidth: ORDER_NO_COLUMN_WIDTH,
  maxWidth: ORDER_NO_COLUMN_WIDTH,
  overflow: 'hidden',
  bgcolor: 'background.paper',
  borderRight: '1px solid rgba(255,255,255,0.08)',
} as const;

const stickyOrderHeadSx = {
  ...tableHeaderSx,
  position: 'sticky',
  left: 0,
  zIndex: 3,
  width: ORDER_NO_COLUMN_WIDTH,
  minWidth: ORDER_NO_COLUMN_WIDTH,
  maxWidth: ORDER_NO_COLUMN_WIDTH,
  overflow: 'hidden',
  borderRight: '1px solid rgba(255,255,255,0.08)',
} as const;

const scrollableOrderNoSx = {
  display: 'block',
  maxWidth: ORDER_NO_COLUMN_WIDTH - 32,
  overflowX: 'auto',
  overflowY: 'hidden',
  whiteSpace: 'nowrap',
  WebkitOverflowScrolling: 'touch',
  '&::-webkit-scrollbar': { height: 4 },
  '&::-webkit-scrollbar-thumb': {
    bgcolor: 'rgba(112,72,232,0.35)',
    borderRadius: 999,
  },
} as const;

const statusChipSx = {
  minWidth: 78,
  justifyContent: 'center',
  '& .MuiChip-label': {
    overflow: 'visible',
    textOverflow: 'clip',
    whiteSpace: 'nowrap',
  },
} as const;

const DISCOUNT_SOURCE_LABEL: Record<NonNullable<Order['discountSource']>, string> = {
  MANUAL: '手動折扣',
  MEMBER: '會員折扣',
  PROMOTION: '促銷折扣',
};

const summarizePaymentStatus = (transactions: PaymentTransaction[]): PaymentStatusSummary => {
  if (transactions.some(txn => txn.status === 'REFUNDED')) return 'REFUNDED';
  if (transactions.some(txn => txn.status === 'SUCCESS')) return 'PAID';
  if (transactions.some(txn => txn.status === 'FAILED' || txn.status === 'VOIDED')) return 'FAILED';
  return 'UNPAID';
};

const summarizeInvoiceStatus = (invoice: Invoice | null): InvoiceStatusSummary => {
  if (!invoice) return 'MISSING';
  return invoice.status;
};

const OrderListPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [orders, setOrders] = useState<Order[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [status, setStatus] = useState<OrderStatus | ''>('');
  const [orderNoKeyword, setOrderNoKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 作廢對話框 / Void dialog
  const [voidDialog, setVoidDialog] = useState(false);
  const [voidTarget, setVoidTarget] = useState<Order | null>(null);
  const [voidReason, setVoidReason] = useState('');
  const posContext = useMemo(() => getActivePosContext(), []);
  const [paymentDialog, setPaymentDialog] = useState(false);
  const [paymentTarget, setPaymentTarget] = useState<Order | null>(null);
  const [paymentTransactions, setPaymentTransactions] = useState<PaymentTransaction[]>([]);
  const [paymentLoading, setPaymentLoading] = useState(false);
  const [paymentStatusByOrder, setPaymentStatusByOrder] = useState<Record<string, PaymentStatusSummary>>({});
  const [invoiceStatusByOrder, setInvoiceStatusByOrder] = useState<Record<string, InvoiceStatusSummary>>({});
  const [detailTarget, setDetailTarget] = useState<Order | null>(null);
  const refundBasePath = location.pathname.startsWith('/admin') ? '/admin/pos/refunds' : '/pos/refunds';

  function formatMoney(amount: number) {
    return new Intl.NumberFormat('zh-TW', { style: 'currency', currency: 'TWD', maximumFractionDigits: 0 }).format(amount);
  }

  const displayedOrders = useMemo(() => {
    const keyword = orderNoKeyword.trim().toLowerCase();
    if (!keyword) return orders;
    return orders.filter(order => order.orderNo.toLowerCase().includes(keyword));
  }, [orderNoKeyword, orders]);

  const orderMetrics = useMemo(() => {
    const activeCount = displayedOrders.filter(order => !['COMPLETED', 'CLOSED', 'VOIDED'].includes(order.status)).length;
    const completedOrders = displayedOrders.filter(order => ['COMPLETED', 'CLOSED'].includes(order.status));
    const revenue = completedOrders.reduce((sum, order) => sum + order.grandTotal, 0);
    const averageTicket = completedOrders.length > 0 ? revenue / completedOrders.length : 0;

    return [
      { label: '進行中訂單', value: String(activeCount), helper: total > 1 ? `目前第 ${page} 頁資料` : '目前列表資料' },
      { label: '本頁營收', value: formatMoney(revenue), helper: `${completedOrders.length} 筆已完成訂單` },
      { label: '平均客單', value: formatMoney(averageTicket), helper: completedOrders.length > 0 ? '依本頁已完成訂單計算' : '首筆訂單後開始計算' },
    ];
  }, [displayedOrders, page, total]);

  const loadOrderClosureStatuses = useCallback(async (orderList: Order[]) => {
    const closedOrders = orderList.filter(order => ['COMPLETED', 'CLOSED'].includes(order.status));
    if (closedOrders.length === 0) {
      setPaymentStatusByOrder({});
      setInvoiceStatusByOrder({});
      return;
    }

    const statusEntries = await Promise.all(closedOrders.map(async (order) => {
      let paymentStatus: PaymentStatusSummary = order.paidTotal > 0 ? 'PAID' : 'UNPAID';
      let invoiceStatus: InvoiceStatusSummary = 'MISSING';

      try {
        const paymentRes = await paymentApi.getByOrder(order.id);
        paymentStatus = summarizePaymentStatus(paymentRes.data ?? []);
      } catch {
        paymentStatus = order.paidTotal > 0 ? 'PAID' : 'FAILED';
      }

      try {
        const invoiceRes = await invoiceApi.getByOrder(order.id);
        invoiceStatus = summarizeInvoiceStatus(invoiceRes.data ?? null);
      } catch {
        invoiceStatus = 'MISSING';
      }

      return [order.id, paymentStatus, invoiceStatus] as const;
    }));

    setPaymentStatusByOrder(Object.fromEntries(statusEntries.map(([orderId, paymentStatus]) => [orderId, paymentStatus])));
    setInvoiceStatusByOrder(Object.fromEntries(statusEntries.map(([orderId, , invoiceStatus]) => [orderId, invoiceStatus])));
  }, []);

  const fetchOrders = useCallback(async () => {
    if (!posContext.storeId) {
      setOrders([]);
      setTotal(0);
      setError('尚未取得 POS 門店資訊。請重新登入收銀台後載入即時訂單。');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const params: OrderListParams = {
        storeId: posContext.storeId,
        page: page - 1,
        size: 20,
        ...(status ? { status } : {}),
      };
      const res = await orderApi.list(params);
      if (res.code === 200 && res.data) {
        const content = res.data.content;
        setOrders(content);
        setTotal(res.data.totalPages);
        await loadOrderClosureStatuses(content);
      }
    } catch {
      setError('訂單載入失敗，請稍後再試。');
    } finally {
      setLoading(false);
    }
  }, [loadOrderClosureStatuses, page, posContext.storeId, status]);

  useEffect(() => { fetchOrders(); }, [fetchOrders]);

  // ========================================
  // 作廢訂單 / Handle void order
  // ========================================
  const handleVoid = async () => {
    if (!voidTarget) return;
    try {
      await orderApi.void(voidTarget.id, posContext.employeeId, voidReason);
      setVoidDialog(false);
      setVoidReason('');
      fetchOrders();
    } catch {
      setError('訂單作廢失敗。');
    }
  };

  // ========================================
  // 查詢付款記錄 / Load payment transactions
  // ========================================
  const handleOpenPayments = async (order: Order) => {
    setPaymentTarget(order);
    setPaymentDialog(true);
    setPaymentLoading(true);
    setError('');
    try {
      const res = await paymentApi.getByOrder(order.id);
      setPaymentTransactions(res.data ?? []);
    } catch {
      setPaymentTransactions([]);
      setError('付款記錄載入失敗。');
    } finally {
      setPaymentLoading(false);
    }
  };

  const getPaymentSummary = (order: Order) =>
    paymentStatusByOrder[order.id] ?? (order.paidTotal > 0 ? 'PAID' : 'UNPAID');

  const getInvoiceSummary = (order: Order) =>
    invoiceStatusByOrder[order.id] ?? 'MISSING';

  // ========================================
  // 訂單詳情稽核顯示 / Order detail audit display
  // ========================================
  const formatOptional = (value?: string | number | null) => {
    if (value === undefined || value === null || value === '') return '-';
    return String(value);
  };

  const formatDiscountSource = (order: Order) => (
    order.discountSource ? DISCOUNT_SOURCE_LABEL[order.discountSource] : '-'
  );

  const renderAuditField = (label: string, value?: string | number | null) => (
    <Box sx={{ minWidth: 0 }}>
      <Typography variant="caption" color="text.secondary" fontWeight={800}>{label}</Typography>
      <Typography variant="body2" fontWeight={800} sx={{ overflowWrap: 'anywhere' }}>
        {formatOptional(value)}
      </Typography>
    </Box>
  );

  const renderClosureChips = (order: Order) => {
    if (!['COMPLETED', 'CLOSED'].includes(order.status)) {
      return <Typography variant="caption" color="text.secondary">尚未完成</Typography>;
    }

    const paymentSummary = getPaymentSummary(order);
    const invoiceSummary = getInvoiceSummary(order);

    return (
      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
        <Chip label={PAYMENT_STATUS_LABEL[paymentSummary]} color={PAYMENT_STATUS_COLOR[paymentSummary]} size="small" />
        <Chip label={INVOICE_STATUS_LABEL[invoiceSummary]} color={INVOICE_STATUS_COLOR[invoiceSummary]} size="small" />
      </Box>
    );
  };

  const renderDiscountChip = (order: Order) => {
    if (order.discountTotal <= 0 || !order.discountSource) {
      return <Typography variant="caption" color="text.secondary">-</Typography>;
    }

    const label = order.discountLabel || DISCOUNT_SOURCE_LABEL[order.discountSource];

    return (
      <Chip
        label={`${label} -${formatMoney(order.discountTotal)}`}
        color={order.discountSource === 'PROMOTION' ? 'success' : 'warning'}
        size="small"
        sx={{
          maxWidth: '100%',
          '& .MuiChip-label': {
            overflow: 'hidden',
            textOverflow: 'ellipsis',
          },
        }}
      />
    );
  };

  const stopRowClick = (event: React.MouseEvent) => {
    event.stopPropagation();
  };

  const renderOrderActions = (order: Order) => (
    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', justifyContent: { xs: 'stretch', md: 'flex-start' } }}>
      <Button
        size="small"
        variant="outlined"
        onClick={(event) => {
          stopRowClick(event);
          setDetailTarget(order);
        }}
        sx={{ flex: { xs: '1 1 120px', md: '0 0 auto' } }}
      >
        詳情
      </Button>
      {['COMPLETED', 'CLOSED'].includes(order.status) && (
        <>
          <Button
            size="small"
            variant="outlined"
            onClick={(event) => {
              stopRowClick(event);
              handleOpenPayments(order);
            }}
            sx={{ flex: { xs: '1 1 120px', md: '0 0 auto' } }}
          >
            付款記錄
          </Button>
          <Button
            size="small"
            color="secondary"
            onClick={(event) => {
              stopRowClick(event);
              navigate(`${refundBasePath}?orderId=${order.id}&amount=${order.grandTotal}&orderNo=${encodeURIComponent(order.orderNo)}`);
            }}
            sx={{ flex: { xs: '1 1 120px', md: '0 0 auto' } }}
          >
            退款
          </Button>
        </>
      )}
      {['DRAFT','CONFIRMED','PREPARING','READY'].includes(order.status) && (
        <Button
          size="small"
          color="error"
          onClick={(event) => {
            stopRowClick(event);
            setVoidTarget(order);
            setVoidDialog(true);
          }}
          sx={{ flex: { xs: '1 1 120px', md: '0 0 auto' } }}
        >
          作廢
        </Button>
      )}
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', gap: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'stretch', md: 'center' }, flexDirection: { xs: 'column', md: 'row' }, gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={900} sx={{ mb: 0.5 }}>
            訂單
          </Typography>
          <Typography variant="body2" color="text.secondary">
            檢視進行中訂單、已完成付款與可作廢票據。
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="secondary"
          onClick={() => navigate('/pos/register')}
          sx={{ minHeight: 52, px: 3, fontWeight: 900 }}
        >
          新增訂單
        </Button>
      </Box>

      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' }, gap: 2 }}>
        {orderMetrics.map(metric => (
          <Card key={metric.label} sx={{ bgcolor: 'background.paper', borderRadius: 3, boxShadow: 'none', border: '1px solid rgba(255,255,255,0.06)' }}>
            <CardContent sx={{ '&:last-child': { pb: 2 } }}>
              <Typography variant="caption" color="text.secondary" fontWeight={800} sx={{ textTransform: 'uppercase', letterSpacing: 0.8 }}>
                {metric.label}
              </Typography>
              <Typography variant="h4" fontWeight={900} sx={{ my: 0.75 }}>
                {metric.value}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {metric.helper}
              </Typography>
            </CardContent>
          </Card>
        ))}
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Box sx={{
        display: 'flex',
        flexDirection: { xs: 'column', sm: 'row' },
        gap: 2,
        alignItems: { xs: 'stretch', sm: 'center' },
        flexWrap: 'wrap',
        bgcolor: 'background.paper',
        p: 2,
        borderRadius: 3,
        border: '1px solid rgba(255,255,255,0.06)',
      }}>
        <FormControl size="small" sx={{ minWidth: { xs: '100%', sm: 160 } }}>
          <InputLabel>訂單狀態</InputLabel>
          <Select value={status} label="訂單狀態" onChange={e => { setStatus(e.target.value as OrderStatus | ''); setPage(1); }}>
            <MenuItem value="">全部</MenuItem>
            {(['DRAFT','CONFIRMED','PREPARING','READY','COMPLETED','CLOSED','VOIDED'] as OrderStatus[]).map(s => (
              <MenuItem key={s} value={s}>{ORDER_STATUS_LABEL[s]}</MenuItem>
            ))}
          </Select>
        </FormControl>
        <TextField
          size="small"
          placeholder="依訂單編號搜尋"
          value={orderNoKeyword}
          onChange={(event) => setOrderNoKeyword(event.target.value)}
          sx={{ flex: '1 1 260px', '& fieldset': { borderColor: 'rgba(255,255,255,0.08)' } }}
        />
      </Box>

      <Box sx={{ display: { xs: 'flex', md: 'none' }, flexDirection: 'column', gap: 1.5 }}>
        {loading && (
          <Card sx={{ bgcolor: 'background.paper', borderRadius: 3, border: '1px solid rgba(255,255,255,0.06)', boxShadow: 'none' }}>
            <CardContent sx={{ display: 'flex', justifyContent: 'center', py: 5 }}>
              <CircularProgress size={24} />
            </CardContent>
          </Card>
        )}
        {!loading && displayedOrders.length === 0 && (
          <Card sx={{ bgcolor: 'background.paper', borderRadius: 3, border: '1px solid rgba(255,255,255,0.06)', boxShadow: 'none' }}>
            <CardContent sx={{ py: 5, textAlign: 'center' }}>
              <Typography variant="h6" fontWeight={800}>
                {orders.length === 0 ? '目前沒有訂單' : '找不到符合條件的訂單'}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {orders.length === 0 ? '建立銷售後，訂單會顯示在這裡。' : '請調整訂單編號關鍵字或清除搜尋。'}
              </Typography>
            </CardContent>
          </Card>
        )}
        {!loading && displayedOrders.map(order => (
          <Card key={order.id} sx={{ bgcolor: 'background.paper', borderRadius: 3, border: '1px solid rgba(255,255,255,0.06)', boxShadow: 'none' }}>
            <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, '&:last-child': { pb: 2 } }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1.5, alignItems: 'flex-start' }}>
                <Box sx={{ minWidth: 0 }}>
                  <Typography variant="caption" color="text.secondary" fontWeight={800}>訂單編號</Typography>
                  <Typography variant="body2" fontFamily="monospace" fontWeight={800} sx={{ overflowWrap: 'anywhere' }}>
                    {order.orderNo}
                  </Typography>
                </Box>
                <Chip label={ORDER_STATUS_LABEL[order.status]} color={STATUS_COLOR[order.status]} size="small" sx={{ flexShrink: 0 }} />
              </Box>
              <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1.5 }}>
                <Box>
                  <Typography variant="caption" color="text.secondary">類型</Typography>
                  <Typography fontWeight={800}>{order.orderType}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">品項</Typography>
                  <Typography fontWeight={800}>{order.items?.length ?? 0}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">合計</Typography>
                  <Typography fontWeight={900} color="secondary.main">{formatMoney(order.grandTotal)}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">折扣</Typography>
                  <Box sx={{ mt: 0.5 }}>{renderDiscountChip(order)}</Box>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">建立時間</Typography>
                  <Typography variant="body2">{formatDateTime(order.createdAt)}</Typography>
                </Box>
              </Box>
              {renderClosureChips(order)}
              {renderOrderActions(order)}
            </CardContent>
          </Card>
        ))}
      </Box>

      <TableContainer
        component={Paper}
        data-testid="orders-desktop-table-container"
        sx={{ display: { xs: 'none', md: 'block' }, flexGrow: 1, bgcolor: 'background.paper', borderRadius: 3, border: '1px solid rgba(255,255,255,0.06)', boxShadow: 'none', overflowX: 'auto' }}
      >
        <Table stickyHeader data-testid="orders-desktop-table" sx={{ minWidth: ORDER_TABLE_MIN_WIDTH, tableLayout: 'fixed' }}>
          <colgroup>
            <col style={{ width: ORDER_NO_COLUMN_WIDTH }} />
            <col style={{ width: ORDER_TABLE_COLUMN_WIDTHS.status }} />
            <col style={{ width: ORDER_TABLE_COLUMN_WIDTHS.orderType }} />
            <col style={{ width: ORDER_TABLE_COLUMN_WIDTHS.itemCount }} />
            <col style={{ width: ORDER_TABLE_COLUMN_WIDTHS.payment }} />
            <col style={{ width: ORDER_TABLE_COLUMN_WIDTHS.invoice }} />
            <col style={{ width: ORDER_TABLE_COLUMN_WIDTHS.discount }} />
            <col style={{ width: ORDER_TABLE_COLUMN_WIDTHS.total }} />
            <col style={{ width: ORDER_TABLE_COLUMN_WIDTHS.createdAt }} />
            <col style={{ width: ORDER_TABLE_COLUMN_WIDTHS.actions }} />
          </colgroup>
          <TableHead>
            <TableRow>
              <TableCell sx={stickyOrderHeadSx}>訂單編號</TableCell>
              <TableCell sx={tableHeaderSx}>狀態</TableCell>
              <TableCell sx={tableHeaderSx}>類型</TableCell>
              <TableCell sx={tableHeaderSx}>品項</TableCell>
              <TableCell sx={tableHeaderSx}>付款狀態</TableCell>
              <TableCell sx={tableHeaderSx}>發票狀態</TableCell>
              <TableCell sx={tableHeaderSx}>折扣</TableCell>
              <TableCell align="right" sx={tableHeaderSx}>合計</TableCell>
              <TableCell sx={tableHeaderSx}>建立時間</TableCell>
              <TableCell sx={tableHeaderSx}>操作</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={10} align="center"><CircularProgress size={24} /></TableCell>
              </TableRow>
            ) : displayedOrders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={10} align="center" sx={{ py: 8 }}>
                  <Typography variant="h6" fontWeight={800}>
                    {orders.length === 0 ? '目前沒有訂單' : '找不到符合條件的訂單'}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {orders.length === 0 ? '選擇門店並建立銷售後，訂單會顯示在這裡。' : '請調整訂單編號關鍵字或清除搜尋。'}
                  </Typography>
                </TableCell>
              </TableRow>
            ) : displayedOrders.map(order => (
              <TableRow
                key={order.id}
                hover
                tabIndex={0}
                aria-label={`查看訂單 ${order.orderNo}`}
                onClick={() => setDetailTarget(order)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    setDetailTarget(order);
                  }
                }}
                sx={{
                  cursor: 'pointer',
                  '&:hover td': { bgcolor: 'rgba(255,255,255,0.04)' },
                  '&:focus-visible td': {
                    outline: '2px solid rgba(255,109,0,0.72)',
                    outlineOffset: -2,
                  },
                }}
              >
                <TableCell sx={stickyOrderCellSx}>
                  <Box sx={scrollableOrderNoSx} tabIndex={0} onClick={stopRowClick}>
                    <Typography
                      data-testid="desktop-order-number"
                      variant="body2"
                      fontFamily="monospace"
                      sx={{ display: 'inline-block', whiteSpace: 'nowrap', pr: 1 }}
                    >
                      {order.orderNo}
                    </Typography>
                  </Box>
                </TableCell>
                <TableCell sx={tableBodyCellSx}>
                  <Chip label={ORDER_STATUS_LABEL[order.status]} color={STATUS_COLOR[order.status]} size="small" sx={statusChipSx} />
                </TableCell>
                <TableCell sx={tableBodyCellSx}>{order.orderType}</TableCell>
                <TableCell sx={tableBodyCellSx}>{order.items?.length ?? 0}</TableCell>
                <TableCell sx={tableBodyCellSx}>
                  {['COMPLETED', 'CLOSED'].includes(order.status) ? (
                    <Chip
                      label={PAYMENT_STATUS_LABEL[getPaymentSummary(order)]}
                      color={PAYMENT_STATUS_COLOR[getPaymentSummary(order)]}
                      size="small"
                    />
                  ) : (
                    <Typography variant="caption" color="text.secondary">-</Typography>
                  )}
                </TableCell>
                <TableCell sx={tableBodyCellSx}>
                  {['COMPLETED', 'CLOSED'].includes(order.status) ? (
                    <Chip
                      label={INVOICE_STATUS_LABEL[getInvoiceSummary(order)]}
                      color={INVOICE_STATUS_COLOR[getInvoiceSummary(order)]}
                      size="small"
                    />
                  ) : (
                    <Typography variant="caption" color="text.secondary">-</Typography>
                  )}
                </TableCell>
                <TableCell sx={tableBodyCellSx}>{renderDiscountChip(order)}</TableCell>
                <TableCell align="right" sx={tableBodyCellSx}>{formatMoney(order.grandTotal)}</TableCell>
                <TableCell sx={tableBodyCellSx}>{formatDateTime(order.createdAt)}</TableCell>
                <TableCell sx={tableBodyCellSx}>
                  {renderOrderActions(order)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* 分頁 / Pagination */}
      {total > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <Pagination count={total} page={page} onChange={(_, v) => setPage(v)} />
        </Box>
      )}

      <Dialog open={voidDialog} onClose={() => setVoidDialog(false)}>
        <DialogTitle>作廢訂單 {voidTarget?.orderNo}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth autoFocus margin="dense"
            label="作廢原因" value={voidReason}
            onChange={e => setVoidReason(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setVoidDialog(false)}>取消</Button>
          <Button color="error" onClick={handleVoid}>確認作廢</Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={Boolean(detailTarget)}
        onClose={() => setDetailTarget(null)}
        aria-labelledby="order-detail-dialog-title"
        maxWidth="lg"
        fullWidth
        slotProps={{
          paper: {
            sx: {
              bgcolor: 'background.paper',
              backgroundImage: 'none',
              color: 'text.primary',
            },
          },
        }}
      >
        <DialogTitle
          id="order-detail-dialog-title"
          sx={{
            position: 'relative',
            px: 3,
            pt: 2.75,
            pb: 1.25,
            color: 'text.primary',
          }}
        >
          <Box aria-hidden="true" sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, minWidth: 0 }}>
            <Typography component="span" variant="h6" fontWeight={900} color="text.primary">
              訂單詳情
            </Typography>
            <Typography
              component="span"
              variant="body2"
              fontFamily="monospace"
              fontWeight={800}
              color="text.secondary"
              sx={{ overflowWrap: 'anywhere' }}
            >
              {detailTarget?.orderNo}
            </Typography>
          </Box>
          <Box
            component="span"
            sx={{
              position: 'absolute',
              width: 1,
              height: 1,
              p: 0,
              m: -1,
              overflow: 'hidden',
              clip: 'rect(0 0 0 0)',
              whiteSpace: 'nowrap',
              border: 0,
            }}
          >
            訂單詳情 {detailTarget?.orderNo}
          </Box>
        </DialogTitle>
        <DialogContent>
          {detailTarget && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(4, 1fr)' }, gap: 2 }}>
                {renderAuditField('訂單狀態', ORDER_STATUS_LABEL[detailTarget.status])}
                {renderAuditField('訂單類型', detailTarget.orderType)}
                {renderAuditField('建立時間', formatDateTime(detailTarget.createdAt))}
                {renderAuditField('完成時間', detailTarget.completedAt ? formatDateTime(detailTarget.completedAt) : null)}
                {renderAuditField('門店 ID', detailTarget.storeId)}
                {renderAuditField('終端 ID', detailTarget.terminalId)}
                {renderAuditField('操作員 ID', detailTarget.employeeId)}
                {renderAuditField('會員 ID', detailTarget.memberId)}
              </Box>

              <Box sx={{ p: 2, borderRadius: 2, border: '1px solid rgba(255,255,255,0.08)', bgcolor: 'rgba(255,255,255,0.03)' }}>
                <Typography variant="subtitle2" fontWeight={900} sx={{ mb: 2 }}>折扣稽核</Typography>
                <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(4, 1fr)' }, gap: 2 }}>
                  {renderAuditField('折扣來源', formatDiscountSource(detailTarget))}
                  {renderAuditField('折扣標籤', detailTarget.discountLabel)}
                  {renderAuditField('促銷規則 ID', detailTarget.promotionRuleId)}
                  {renderAuditField('優惠碼', detailTarget.promotionCode)}
                </Box>
              </Box>

              <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(5, 1fr)' }, gap: 2 }}>
                {renderAuditField('小計', formatMoney(detailTarget.subtotal))}
                {renderAuditField('折扣', `-${formatMoney(detailTarget.discountTotal)}`)}
                {renderAuditField('稅額', formatMoney(detailTarget.taxTotal))}
                {renderAuditField('已收', formatMoney(detailTarget.paidTotal))}
                {renderAuditField('總計', formatMoney(detailTarget.grandTotal))}
              </Box>

              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>品項</TableCell>
                    <TableCell>SKU</TableCell>
                    <TableCell align="right">單價</TableCell>
                    <TableCell align="right">數量</TableCell>
                    <TableCell align="right">明細折扣</TableCell>
                    <TableCell align="right">小計</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {detailTarget.items.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>{item.itemNameSnapshot}</TableCell>
                      <TableCell>
                        <Typography variant="caption" fontFamily="monospace">{item.skuSnapshot ?? '-'}</Typography>
                      </TableCell>
                      <TableCell align="right">{formatMoney(item.unitPrice)}</TableCell>
                      <TableCell align="right">{item.quantity}</TableCell>
                      <TableCell align="right">{formatMoney(item.discountAmount)}</TableCell>
                      <TableCell align="right">{formatMoney(item.lineTotal)}</TableCell>
                    </TableRow>
                  ))}
                  {detailTarget.items.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={6} align="center" sx={{ py: 4 }}>尚無品項明細</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailTarget(null)}>關閉</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={paymentDialog} onClose={() => setPaymentDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>付款記錄 {paymentTarget?.orderNo}</DialogTitle>
        <DialogContent>
          {paymentLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress size={24} />
            </Box>
          ) : (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>支付方式</TableCell>
                  <TableCell>狀態</TableCell>
                  <TableCell align="right">付款金額</TableCell>
                  <TableCell align="right">實收</TableCell>
                  <TableCell align="right">找零</TableCell>
                  <TableCell>閘道編號</TableCell>
                  <TableCell>處理時間</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {paymentTransactions.map((txn) => (
                  <TableRow key={txn.id}>
                    <TableCell>{txn.methodType}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={txn.status}
                        color={txn.status === 'SUCCESS' ? 'success' : 'error'}
                      />
                    </TableCell>
                    <TableCell align="right">{formatMoney(txn.amount)}</TableCell>
                    <TableCell align="right">{txn.tendered === null ? '-' : formatMoney(txn.tendered)}</TableCell>
                    <TableCell align="right">{formatMoney(txn.changeGiven)}</TableCell>
                    <TableCell>
                      <Typography variant="caption" fontFamily="monospace">
                        {txn.gatewayRef ?? '-'}
                      </Typography>
                    </TableCell>
                    <TableCell>{formatDateTime(txn.processedAt)}</TableCell>
                  </TableRow>
                ))}
                {paymentTransactions.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                      尚無付款記錄
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPaymentDialog(false)}>關閉</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default OrderListPage;
