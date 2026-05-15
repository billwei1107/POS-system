/**
 * @file TransferPage.tsx
 * @description 調撥管理頁 / Transfer management page
 * @description_en Manage inter-store stock transfers: create, approve, ship, receive, cancel
 * @description_zh 管理門店間調撥：建立、核准、出貨、收貨、取消
 */
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Box, Typography, Button, Table, TableHead, TableRow, TableCell, TableBody,
  Chip, Alert, TextField, Dialog, DialogTitle, DialogContent, DialogActions,
  CircularProgress, Collapse,
} from '@mui/material';
import { formatDateTime } from '@shared/utils';
import { getActivePosContext } from '../../pos-orders/posSession';
import { transferApi } from '../api/inventoryApi';
import type { CreateTransferRequestPayload, TransferRequest, TransferStatus } from '../types';

const STATUS_LABEL: Record<TransferStatus, string> = {
  REQUESTED: '待核准',
  APPROVED: '已核准',
  IN_TRANSIT: '運送中',
  RECEIVED: '已收貨',
  CANCELLED: '已取消',
};

const STATUS_COLOR: Record<TransferStatus, 'default' | 'warning' | 'primary' | 'success' | 'error'> = {
  REQUESTED: 'warning',
  APPROVED: 'primary',
  IN_TRANSIT: 'primary',
  RECEIVED: 'success',
  CANCELLED: 'error',
};

// ========================================
// 調撥管理頁 / Transfer management page
// ========================================
const TransferPage: React.FC = () => {
  const [transfers, setTransfers] = useState<TransferRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const posContext = useMemo(() => getActivePosContext(), []);

  // ========================================
  // 新增調撥表單 / New transfer form
  // ========================================
  const [form, setForm] = useState<CreateTransferRequestPayload>({
    fromStoreId: posContext.storeId,
    toStoreId: '',
    notes: '',
    items: [{ itemId: '', requestedQty: 1 }],
  });

  const loadTransfers = useCallback(async () => {
    try {
      setLoading(true);
      const res = await transferApi.listByStore(posContext.storeId);
      setTransfers(res.data ?? []);
    } catch {
      setError('載入失敗');
    } finally {
      setLoading(false);
    }
  }, [posContext.storeId]);

  useEffect(() => { loadTransfers(); }, [loadTransfers]);

  const handleCreate = async () => {
    if (!form.toStoreId || form.items.some(i => !i.itemId)) return;
    try {
      await transferApi.create(form);
      setDialogOpen(false);
      setForm({ fromStoreId: posContext.storeId, toStoreId: '', notes: '', items: [{ itemId: '', requestedQty: 1 }] });
      await loadTransfers();
      setSuccess('調撥申請已建立');
    } catch {
      setError('建立失敗');
    }
  };

  const handleAction = async (action: 'approve' | 'ship' | 'receive' | 'cancel', id: string) => {
    try {
      await transferApi[action](id);
      await loadTransfers();
      setSuccess(`操作成功`);
    } catch {
      setError('操作失敗');
    }
  };

  const addItem = () => setForm(f => ({ ...f, items: [...f.items, { itemId: '', requestedQty: 1 }] }));

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>;

  return (
    <Box sx={{ p: 3 }}>
      {/* ======================================== */}
      {/* 頁面標題 / Page header */}
      {/* ======================================== */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h5" fontWeight="bold">調撥管理</Typography>
        <Button variant="contained" onClick={() => setDialogOpen(true)}>新增調撥申請</Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      {/* ======================================== */}
      {/* 調撥列表 / Transfer list */}
      {/* ======================================== */}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>調撥單號</TableCell>
            <TableCell>來源門店</TableCell>
            <TableCell>目的門店</TableCell>
            <TableCell>狀態</TableCell>
            <TableCell>申請時間</TableCell>
            <TableCell>操作</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {transfers.map(t => (
            <React.Fragment key={t.id}>
              <TableRow
                hover
                sx={{ cursor: 'pointer' }}
                onClick={() => setExpandedId(expandedId === t.id ? null : t.id)}
              >
                <TableCell><strong>{t.transferNo}</strong></TableCell>
                <TableCell><code>{t.fromStoreId.slice(0, 8)}...</code></TableCell>
                <TableCell><code>{t.toStoreId.slice(0, 8)}...</code></TableCell>
                <TableCell>
                  <Chip label={STATUS_LABEL[t.status]} color={STATUS_COLOR[t.status]} size="small" />
                </TableCell>
                <TableCell>{formatDateTime(t.createdAt)}</TableCell>
                <TableCell onClick={e => e.stopPropagation()}>
                  <Box sx={{ display: 'flex', gap: 1 }}>
                    {t.status === 'REQUESTED' && (
                      <>
                        <Button size="small" variant="outlined" color="primary"
                          onClick={() => handleAction('approve', t.id)}>核准</Button>
                        <Button size="small" variant="outlined" color="error"
                          onClick={() => handleAction('cancel', t.id)}>取消</Button>
                      </>
                    )}
                    {t.status === 'APPROVED' && (
                      <Button size="small" variant="outlined" color="primary"
                        onClick={() => handleAction('ship', t.id)}>確認出貨</Button>
                    )}
                    {t.status === 'IN_TRANSIT' && (
                      <Button size="small" variant="contained" color="success"
                        onClick={() => handleAction('receive', t.id)}>確認收貨</Button>
                    )}
                  </Box>
                </TableCell>
              </TableRow>

              {/* ======================================== */}
              {/* 展開品項明細 / Expandable item details */}
              {/* ======================================== */}
              <TableRow>
                <TableCell colSpan={6} sx={{ py: 0 }}>
                  <Collapse in={expandedId === t.id} timeout="auto">
                    <Box sx={{ p: 2, bgcolor: 'grey.50' }}>
                      <Typography variant="subtitle2" sx={{ mb: 1 }}>品項明細</Typography>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>商品 ID</TableCell>
                            <TableCell align="right">申請量</TableCell>
                            <TableCell align="right">出貨量</TableCell>
                            <TableCell align="right">收貨量</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {t.items.map(item => (
                            <TableRow key={item.id}>
                              <TableCell><code>{item.itemId.slice(0, 8)}...</code></TableCell>
                              <TableCell align="right">{item.requestedQty}</TableCell>
                              <TableCell align="right">{item.shippedQty}</TableCell>
                              <TableCell align="right">{item.receivedQty}</TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </Box>
                  </Collapse>
                </TableCell>
              </TableRow>
            </React.Fragment>
          ))}
          {transfers.length === 0 && (
            <TableRow>
              <TableCell colSpan={6} align="center">尚無調撥記錄</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      {/* ======================================== */}
      {/* 新增調撥對話框 / Create transfer dialog */}
      {/* ======================================== */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>新增調撥申請</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField
            label="目的門店 ID"
            value={form.toStoreId}
            onChange={e => setForm({ ...form, toStoreId: e.target.value })}
            required
          />
          <TextField
            label="備註"
            value={form.notes}
            onChange={e => setForm({ ...form, notes: e.target.value })}
          />
          <Typography variant="subtitle2">品項清單</Typography>
          {form.items.map((item, idx) => (
            <Box key={idx} sx={{ display: 'flex', gap: 1 }}>
              <TextField
                label={`商品 ID #${idx + 1}`}
                value={item.itemId}
                onChange={e => {
                  const items = [...form.items];
                  items[idx] = { ...items[idx], itemId: e.target.value };
                  setForm({ ...form, items });
                }}
                size="small"
                sx={{ flex: 2 }}
              />
              <TextField
                type="number"
                label="數量"
                value={item.requestedQty}
                onChange={e => {
                  const items = [...form.items];
                  items[idx] = { ...items[idx], requestedQty: Number(e.target.value) };
                  setForm({ ...form, items });
                }}
                size="small"
                sx={{ flex: 1 }}
                inputProps={{ min: 0.001, step: 0.001 }}
              />
            </Box>
          ))}
          <Button variant="text" size="small" onClick={addItem}>+ 新增品項</Button>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleCreate}>建立</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default TransferPage;
