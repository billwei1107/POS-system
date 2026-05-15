/**
 * @file PayMethodSettingsPage.tsx
 * @description 支付方式設定頁 / Payment method settings page
 * @description_en Manage store-level payment methods: list, create, deactivate
 * @description_zh 管理門店支付方式：列表、新增、停用
 */
import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Button, Table, TableHead, TableRow, TableCell, TableBody,
  Chip, Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  MenuItem, Switch, FormControlLabel, Alert, CircularProgress,
} from '@mui/material';
import { DEFAULT_STORE_ID } from '../../pos-orders/config';
import { payMethodApi } from '../api/paymentApi';
import type { PayMethod, CreatePayMethodRequest, MethodType } from '../types';

const STORE_ID = DEFAULT_STORE_ID;

const METHOD_TYPE_OPTIONS: { value: MethodType; label: string }[] = [
  { value: 'CASH', label: '現金' },
  { value: 'CARD', label: '刷卡' },
  { value: 'QR_CODE', label: 'QR Code' },
  { value: 'GIFT_CARD', label: '禮品卡' },
  { value: 'MIXED', label: '混合' },
];

const METHOD_COLOR: Record<MethodType, 'default' | 'success' | 'primary' | 'warning' | 'secondary'> = {
  CASH: 'success',
  CARD: 'primary',
  QR_CODE: 'warning',
  GIFT_CARD: 'secondary',
  MIXED: 'default',
};

// ========================================
// 支付方式設定頁 / Pay method settings page
// ========================================
const PayMethodSettingsPage: React.FC = () => {
  const [methods, setMethods] = useState<PayMethod[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // ========================================
  // 新增表單狀態 / New pay method form state
  // ========================================
  const [form, setForm] = useState<CreatePayMethodRequest>({
    storeId: STORE_ID,
    code: '',
    name: '',
    methodType: 'CASH',
    isChangeBack: true,
    sortOrder: 0,
  });

  const loadMethods = async () => {
    try {
      setLoading(true);
      const res = await payMethodApi.list(STORE_ID);
      setMethods(res.data ?? []);
    } catch {
      setError('載入失敗');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadMethods(); }, []);

  const handleCreate = async () => {
    if (!form.code || !form.name) return;
    setSubmitting(true);
    try {
      await payMethodApi.create(form);
      setDialogOpen(false);
      setForm({ storeId: STORE_ID, code: '', name: '', methodType: 'CASH', isChangeBack: true, sortOrder: 0 });
      await loadMethods();
    } catch {
      setError('建立失敗');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeactivate = async (id: string) => {
    if (!window.confirm('確定停用此支付方式？')) return;
    try {
      await payMethodApi.deactivate(id);
      await loadMethods();
    } catch {
      setError('停用失敗');
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>;

  return (
    <Box sx={{ p: 3 }}>
      {/* ======================================== */}
      {/* 頁面標題列 / Page header */}
      {/* ======================================== */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h5" fontWeight="bold">支付方式設定</Typography>
        <Button variant="contained" onClick={() => setDialogOpen(true)}>新增支付方式</Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      {/* ======================================== */}
      {/* 支付方式列表 / Pay method list table */}
      {/* ======================================== */}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>代碼</TableCell>
            <TableCell>名稱</TableCell>
            <TableCell>類型</TableCell>
            <TableCell>找零</TableCell>
            <TableCell>排序</TableCell>
            <TableCell>操作</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {methods.map((m) => (
            <TableRow key={m.id}>
              <TableCell>{m.code}</TableCell>
              <TableCell>{m.name}</TableCell>
              <TableCell>
                <Chip label={m.methodType} color={METHOD_COLOR[m.methodType]} size="small" />
              </TableCell>
              <TableCell>{m.isChangeBack ? '是' : '否'}</TableCell>
              <TableCell>{m.sortOrder}</TableCell>
              <TableCell>
                <Button size="small" color="error" onClick={() => handleDeactivate(m.id)}>
                  停用
                </Button>
              </TableCell>
            </TableRow>
          ))}
          {methods.length === 0 && (
            <TableRow>
              <TableCell colSpan={6} align="center">尚無支付方式</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      {/* ======================================== */}
      {/* 新增支付方式對話框 / Create pay method dialog */}
      {/* ======================================== */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>新增支付方式</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField
            label="代碼"
            value={form.code}
            onChange={(e) => setForm({ ...form, code: e.target.value })}
            required
            helperText="唯一識別碼，如 CASH / VISA"
          />
          <TextField
            label="名稱"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
          />
          <TextField
            select
            label="類型"
            value={form.methodType}
            onChange={(e) => setForm({ ...form, methodType: e.target.value as MethodType })}
          >
            {METHOD_TYPE_OPTIONS.map((opt) => (
              <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
            ))}
          </TextField>
          <TextField
            type="number"
            label="排序"
            value={form.sortOrder}
            onChange={(e) => setForm({ ...form, sortOrder: Number(e.target.value) })}
          />
          <FormControlLabel
            control={
              <Switch
                checked={form.isChangeBack}
                onChange={(e) => setForm({ ...form, isChangeBack: e.target.checked })}
              />
            }
            label="允許找零"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleCreate} disabled={submitting}>
            {submitting ? '建立中...' : '建立'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default PayMethodSettingsPage;
