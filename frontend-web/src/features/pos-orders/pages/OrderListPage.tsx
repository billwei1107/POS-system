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
  DialogActions, TextField, CircularProgress, Alert,
} from '@mui/material';
import { orderApi } from '../api/orderApi';
import type { Order, OrderStatus, OrderListParams } from '../types';

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

const STORE_ID = import.meta.env.VITE_DEFAULT_STORE_ID || '';

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
    setLoading(true);
    setError('');
    try {
      const params: OrderListParams = {
        storeId: STORE_ID,
        page: page - 1,
        size: 20,
        ...(status ? { status } : {}),
      };
      const res = await orderApi.list(params);
      if (res.success && res.data) {
        setOrders(res.data.content);
        setTotal(res.data.totalPages);
      }
    } catch {
      setError('載入訂單失敗，請重試');
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
      setError('作廢訂單失敗');
    }
  };

  const formatMoney = (amount: number) =>
    new Intl.NumberFormat('zh-TW', { style: 'currency', currency: 'TWD', maximumFractionDigits: 0 }).format(amount);

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleString('zh-TW', { dateStyle: 'short', timeStyle: 'short' });

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" fontWeight="bold" mb={3}>訂單管理</Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* 篩選列 / Filter row */}
      <Box sx={{ display: 'flex', gap: 2, mb: 2, alignItems: 'center' }}>
        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>訂單狀態</InputLabel>
          <Select value={status} label="訂單狀態" onChange={e => { setStatus(e.target.value as OrderStatus | ''); setPage(1); }}>
            <MenuItem value="">全部</MenuItem>
            {(['DRAFT','CONFIRMED','PREPARING','READY','COMPLETED','CLOSED','VOIDED'] as OrderStatus[]).map(s => (
              <MenuItem key={s} value={s}>{s}</MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      {/* 訂單表格 / Order table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>訂單編號</TableCell>
              <TableCell>狀態</TableCell>
              <TableCell>類型</TableCell>
              <TableCell>商品數</TableCell>
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
                <TableCell colSpan={7} align="center">尚無訂單資料</TableCell>
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

      {/* 作廢對話框 / Void dialog */}
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
