/**
 * @file TaxClassSettingsPage.tsx
 * @description 稅率類別設定頁 / Tax class settings page
 * @description_en Manage store-level tax classes: list, create, deactivate
 * @description_zh 管理門店稅率類別：列表、新增、停用
 */
import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Button, Table, TableHead, TableRow, TableCell, TableBody,
  Chip, Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  MenuItem, Switch, FormControlLabel, Alert, CircularProgress,
} from '@mui/material';
import { taxClassApi } from '../api/taxApi';
import type { TaxClass, CreateTaxClassRequest, TaxType } from '../types';

const STORE_ID = import.meta.env.VITE_DEFAULT_STORE_ID as string;

const TAX_TYPE_OPTIONS: { value: TaxType; label: string }[] = [
  { value: 'INCLUSIVE', label: '含稅' },
  { value: 'EXCLUSIVE', label: '外加稅' },
  { value: 'EXEMPT', label: '免稅' },
  { value: 'ZERO_RATED', label: '零稅率' },
];

const TAX_TYPE_COLOR: Record<TaxType, 'default' | 'success' | 'primary' | 'warning'> = {
  INCLUSIVE: 'primary',
  EXCLUSIVE: 'warning',
  EXEMPT: 'success',
  ZERO_RATED: 'default',
};

const TAX_TYPE_LABEL: Record<TaxType, string> = {
  INCLUSIVE: '含稅',
  EXCLUSIVE: '外加稅',
  EXEMPT: '免稅',
  ZERO_RATED: '零稅率',
};

// ========================================
// 稅率類別設定頁 / Tax class settings page
// ========================================
const TaxClassSettingsPage: React.FC = () => {
  const [classes, setClasses] = useState<TaxClass[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [form, setForm] = useState<CreateTaxClassRequest>({
    storeId: STORE_ID,
    name: '',
    taxType: 'INCLUSIVE',
    rate: 0.05,
    description: '',
    isDefault: false,
  });

  const loadClasses = async () => {
    try {
      setLoading(true);
      const res = await taxClassApi.list(STORE_ID);
      setClasses(res.data.data ?? []);
    } catch {
      setError('載入失敗');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadClasses(); }, []);

  const handleCreate = async () => {
    if (!form.name) return;
    setSubmitting(true);
    try {
      await taxClassApi.create(form);
      setDialogOpen(false);
      setForm({ storeId: STORE_ID, name: '', taxType: 'INCLUSIVE', rate: 0.05, description: '', isDefault: false });
      await loadClasses();
    } catch {
      setError('建立失敗');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeactivate = async (id: string) => {
    if (!window.confirm('確定停用此稅率類別？')) return;
    try {
      await taxClassApi.deactivate(id);
      await loadClasses();
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
        <Typography variant="h5" fontWeight="bold">稅率類別設定</Typography>
        <Button variant="contained" onClick={() => setDialogOpen(true)}>新增稅率類別</Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      {/* ======================================== */}
      {/* 稅率列表 / Tax class list */}
      {/* ======================================== */}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>名稱</TableCell>
            <TableCell>類型</TableCell>
            <TableCell align="right">稅率</TableCell>
            <TableCell>說明</TableCell>
            <TableCell>預設</TableCell>
            <TableCell>操作</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {classes.map((tc) => (
            <TableRow key={tc.id}>
              <TableCell>{tc.name}</TableCell>
              <TableCell>
                <Chip label={TAX_TYPE_LABEL[tc.taxType]} color={TAX_TYPE_COLOR[tc.taxType]} size="small" />
              </TableCell>
              <TableCell align="right">{(tc.rate * 100).toFixed(1)}%</TableCell>
              <TableCell>{tc.description ?? '-'}</TableCell>
              <TableCell>
                {tc.isDefault && <Chip label="預設" color="success" size="small" />}
              </TableCell>
              <TableCell>
                <Button size="small" color="error" onClick={() => handleDeactivate(tc.id)}>
                  停用
                </Button>
              </TableCell>
            </TableRow>
          ))}
          {classes.length === 0 && (
            <TableRow>
              <TableCell colSpan={6} align="center">尚無稅率類別</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      {/* ======================================== */}
      {/* 新增對話框 / Create dialog */}
      {/* ======================================== */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>新增稅率類別</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField
            label="名稱"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
          />
          <TextField
            select
            label="稅率類型"
            value={form.taxType}
            onChange={(e) => setForm({ ...form, taxType: e.target.value as TaxType })}
          >
            {TAX_TYPE_OPTIONS.map((opt) => (
              <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
            ))}
          </TextField>
          <TextField
            type="number"
            label="稅率（如 0.05 = 5%）"
            value={form.rate}
            onChange={(e) => setForm({ ...form, rate: Number(e.target.value) })}
            inputProps={{ step: 0.01, min: 0, max: 1 }}
          />
          <TextField
            label="說明（選填）"
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
          <FormControlLabel
            control={
              <Switch
                checked={form.isDefault}
                onChange={(e) => setForm({ ...form, isDefault: e.target.checked })}
              />
            }
            label="設為預設稅率"
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

export default TaxClassSettingsPage;
