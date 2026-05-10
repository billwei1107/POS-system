/**
 * @file OrderListPage.tsx
 * @description 訂單列表頁面 / Order list page
 * @description_en Management view for listing, filtering and voiding POS orders
 * @description_zh POS 訂單管理列表頁，支援狀態篩選、分頁與作廢操作
 */
import React, { useState, useEffect, useCallback } from 'react';
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, Select, MenuItem, FormControl,
  InputLabel, Pagination, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, CircularProgress, Alert, Card, CardContent,
} from '@mui/material';
import { orderApi } from '../api/orderApi';
import type { Order, OrderStatus, OrderListParams } from '../types';
import { DEFAULT_STORE_ID } from '../config';

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

const ORDER_METRICS = [
  { label: '進行中訂單', value: '0', helper: '等待同步資料' },
  { label: '今日營收', value: '$0.00', helper: '尚未選擇門店' },
  { label: '平均客單', value: '$0.00', helper: '首筆訂單後開始計算' },
];

const OrderListPage: React.FC = () => {
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
        setOrders(res.data.content);
        setTotal(res.data.totalPages);
      }
    } catch {
      setError('訂單載入失敗，請稍後再試。');
    } finally {
      setLoading(false);
    }
  }, [page, status]);

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

  const formatMoney = (amount: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleString('en-US', { dateStyle: 'medium', timeStyle: 'short' });

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
        {ORDER_METRICS.map(metric => (
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

      <TableContainer component={Paper} sx={{ flexGrow: 1, bgcolor: 'background.paper', borderRadius: 3, border: '1px solid rgba(255,255,255,0.06)', boxShadow: 'none' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>訂單編號</TableCell>
              <TableCell>狀態</TableCell>
              <TableCell>類型</TableCell>
              <TableCell>品項</TableCell>
              <TableCell align="right">合計</TableCell>
              <TableCell>建立時間</TableCell>
              <TableCell>操作</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} align="center"><CircularProgress size={24} /></TableCell>
              </TableRow>
            ) : orders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 8 }}>
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
                <TableCell align="right">{formatMoney(order.grandTotal)}</TableCell>
                <TableCell>{formatDate(order.createdAt)}</TableCell>
                <TableCell>
                  {['DRAFT','CONFIRMED','PREPARING','READY'].includes(order.status) && (
                    <Button size="small" color="error" onClick={() => { setVoidTarget(order); setVoidDialog(true); }}>
                      作廢
                    </Button>
                  )}
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
    </Box>
  );
};

export default OrderListPage;
