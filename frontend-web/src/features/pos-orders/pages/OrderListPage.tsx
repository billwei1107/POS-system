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
import { DEFAULT_STORE_ID } from '../config';
import { useNavigate } from 'react-router-dom';
import { paymentApi } from '../../pos-payment/api/paymentApi';
import type { PaymentTransaction } from '../../pos-payment/types';
import { invoiceApi } from '../../pos-tax/api/taxApi';
import type { Invoice } from '../../pos-tax/types';

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
  const [orders, setOrders] = useState<Order[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [status, setStatus] = useState<OrderStatus | ''>('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 作廢對話框 / Void dialog
  const [voidDialog, setVoidDialog] = useState(false);
  const [voidTarget, setVoidTarget] = useState<Order | null>(null);
  const [voidReason, setVoidReason] = useState('');
  const [voidedBy] = useState('00000000-0000-0000-0000-000000000001');
  const [paymentDialog, setPaymentDialog] = useState(false);
  const [paymentTarget, setPaymentTarget] = useState<Order | null>(null);
  const [paymentTransactions, setPaymentTransactions] = useState<PaymentTransaction[]>([]);
  const [paymentLoading, setPaymentLoading] = useState(false);
  const [paymentStatusByOrder, setPaymentStatusByOrder] = useState<Record<string, PaymentStatusSummary>>({});
  const [invoiceStatusByOrder, setInvoiceStatusByOrder] = useState<Record<string, InvoiceStatusSummary>>({});

  function formatMoney(amount: number) {
    return new Intl.NumberFormat('zh-TW', { style: 'currency', currency: 'TWD', maximumFractionDigits: 0 }).format(amount);
  }

  const orderMetrics = useMemo(() => {
    const activeCount = orders.filter(order => !['COMPLETED', 'CLOSED', 'VOIDED'].includes(order.status)).length;
    const completedOrders = orders.filter(order => ['COMPLETED', 'CLOSED'].includes(order.status));
    const revenue = completedOrders.reduce((sum, order) => sum + order.grandTotal, 0);
    const averageTicket = completedOrders.length > 0 ? revenue / completedOrders.length : 0;

    return [
      { label: '進行中訂單', value: String(activeCount), helper: total > 1 ? `目前第 ${page} 頁資料` : '目前列表資料' },
      { label: '本頁營收', value: formatMoney(revenue), helper: `${completedOrders.length} 筆已完成訂單` },
      { label: '平均客單', value: formatMoney(averageTicket), helper: completedOrders.length > 0 ? '依本頁已完成訂單計算' : '首筆訂單後開始計算' },
    ];
  }, [orders, page, total]);

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
    if (!DEFAULT_STORE_ID) {
      setOrders([]);
      setTotal(0);
      setError('尚未設定預設門店。請設定 VITE_DEFAULT_STORE_ID 後載入即時訂單。');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const params: OrderListParams = {
        storeId: DEFAULT_STORE_ID,
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
  }, [loadOrderClosureStatuses, page, status]);

  useEffect(() => { fetchOrders(); }, [fetchOrders]);

  // ========================================
  // 作廢訂單 / Handle void order
  // ========================================
  const handleVoid = async () => {
    if (!voidTarget) return;
    try {
      await orderApi.void(voidTarget.id, voidedBy, voidReason);
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

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleString('en-US', { dateStyle: 'medium', timeStyle: 'short' });

  const getPaymentSummary = (order: Order) =>
    paymentStatusByOrder[order.id] ?? (order.paidTotal > 0 ? 'PAID' : 'UNPAID');

  const getInvoiceSummary = (order: Order) =>
    invoiceStatusByOrder[order.id] ?? 'MISSING';

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

  const renderOrderActions = (order: Order) => (
    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', justifyContent: { xs: 'stretch', md: 'flex-start' } }}>
      {['COMPLETED', 'CLOSED'].includes(order.status) && (
        <>
          <Button
            size="small"
            variant="outlined"
            onClick={() => handleOpenPayments(order)}
            sx={{ flex: { xs: '1 1 120px', md: '0 0 auto' } }}
          >
            付款記錄
          </Button>
          <Button
            size="small"
            color="secondary"
            onClick={() => navigate(`/pos/refunds?orderId=${order.id}&amount=${order.grandTotal}&orderNo=${encodeURIComponent(order.orderNo)}`)}
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
          onClick={() => { setVoidTarget(order); setVoidDialog(true); }}
          sx={{ flex: { xs: '1 1 120px', md: '0 0 auto' } }}
        >
          作廢
        </Button>
      )}
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', gap: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'stretch', md: 'flex-start' }, flexDirection: { xs: 'column', md: 'row' }, gap: 2 }}>
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
          sx={{ minHeight: 44, px: 3, fontWeight: 900 }}
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

      <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap', bgcolor: 'background.paper', p: 2, borderRadius: 3, border: '1px solid rgba(255,255,255,0.06)' }}>
        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>訂單狀態</InputLabel>
          <Select value={status} label="訂單狀態" onChange={e => { setStatus(e.target.value as OrderStatus | ''); setPage(1); }}>
            <MenuItem value="">全部</MenuItem>
            {(['DRAFT','CONFIRMED','PREPARING','READY','COMPLETED','CLOSED','VOIDED'] as OrderStatus[]).map(s => (
              <MenuItem key={s} value={s}>{s}</MenuItem>
            ))}
          </Select>
        </FormControl>
        <TextField
          size="small"
          placeholder="依訂單編號搜尋"
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
        {!loading && orders.length === 0 && (
          <Card sx={{ bgcolor: 'background.paper', borderRadius: 3, border: '1px solid rgba(255,255,255,0.06)', boxShadow: 'none' }}>
            <CardContent sx={{ py: 5, textAlign: 'center' }}>
              <Typography variant="h6" fontWeight={800}>目前沒有訂單</Typography>
              <Typography variant="body2" color="text.secondary">建立銷售後，訂單會顯示在這裡。</Typography>
            </CardContent>
          </Card>
        )}
        {!loading && orders.map(order => (
          <Card key={order.id} sx={{ bgcolor: 'background.paper', borderRadius: 3, border: '1px solid rgba(255,255,255,0.06)', boxShadow: 'none' }}>
            <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, '&:last-child': { pb: 2 } }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1.5, alignItems: 'flex-start' }}>
                <Box sx={{ minWidth: 0 }}>
                  <Typography variant="caption" color="text.secondary" fontWeight={800}>訂單編號</Typography>
                  <Typography variant="body2" fontFamily="monospace" fontWeight={800} sx={{ overflowWrap: 'anywhere' }}>
                    {order.orderNo}
                  </Typography>
                </Box>
                <Chip label={order.status} color={STATUS_COLOR[order.status]} size="small" sx={{ flexShrink: 0 }} />
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
                  <Typography variant="caption" color="text.secondary">建立時間</Typography>
                  <Typography variant="body2">{formatDate(order.createdAt)}</Typography>
                </Box>
              </Box>
              {renderClosureChips(order)}
              {renderOrderActions(order)}
            </CardContent>
          </Card>
        ))}
      </Box>

      <TableContainer component={Paper} sx={{ display: { xs: 'none', md: 'block' }, flexGrow: 1, bgcolor: 'background.paper', borderRadius: 3, border: '1px solid rgba(255,255,255,0.06)', boxShadow: 'none' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>訂單編號</TableCell>
              <TableCell>狀態</TableCell>
              <TableCell>類型</TableCell>
              <TableCell>品項</TableCell>
              <TableCell>付款狀態</TableCell>
              <TableCell>發票狀態</TableCell>
              <TableCell align="right">合計</TableCell>
              <TableCell>建立時間</TableCell>
              <TableCell>操作</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={9} align="center"><CircularProgress size={24} /></TableCell>
              </TableRow>
            ) : orders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} align="center" sx={{ py: 8 }}>
                  <Typography variant="h6" fontWeight={800}>目前沒有訂單</Typography>
                  <Typography variant="body2" color="text.secondary">
                    選擇門店並建立銷售後，訂單會顯示在這裡。
                  </Typography>
                </TableCell>
              </TableRow>
            ) : orders.map(order => (
              <TableRow key={order.id} hover>
                <TableCell><Typography variant="body2" fontFamily="monospace">{order.orderNo}</Typography></TableCell>
                <TableCell>
                  <Chip label={order.status} color={STATUS_COLOR[order.status]} size="small" />
                </TableCell>
                <TableCell>{order.orderType}</TableCell>
                <TableCell>{order.items?.length ?? 0}</TableCell>
                <TableCell>
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
                <TableCell>
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
                <TableCell align="right">{formatMoney(order.grandTotal)}</TableCell>
                <TableCell>{formatDate(order.createdAt)}</TableCell>
                <TableCell>
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
                    <TableCell>{formatDate(txn.processedAt)}</TableCell>
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
