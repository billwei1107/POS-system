/**
 * @file InvoiceTrackPage.tsx
 * @description 發票字軌管理頁 / Invoice track management page
 * @description_en Register and view bi-monthly MoF-allocated invoice character tracks
 * @description_zh 登記並查看財政部配發的發票字軌號碼範圍
 */
import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Button, Table, TableHead, TableRow, TableCell, TableBody,
  Chip, Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  Alert, CircularProgress, LinearProgress, Tooltip,
} from '@mui/material';
import { invoiceTrackApi } from '../api/taxApi';
import type { InvoiceTrack, AddInvoiceTrackRequest } from '../types';
import { DEFAULT_STORE_ID } from '../../pos-orders/config';

// ========================================
// 字軌使用進度計算 / Track usage progress calculation
// ========================================
const calcUsagePercent = (track: InvoiceTrack): number => {
  const start = parseInt(track.startNo);
  const end = parseInt(track.endNo);
  const current = parseInt(track.currentNo);
  const total = end - start + 1;
  const used = current === 0 ? 0 : current - start + 1;
  return Math.round((used / total) * 100);
};

// ========================================
// 發票字軌管理頁 / Invoice track management page
// ========================================
const InvoiceTrackPage: React.FC = () => {
  const [tracks, setTracks] = useState<InvoiceTrack[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [form, setForm] = useState<AddInvoiceTrackRequest>({
    storeId: DEFAULT_STORE_ID,
    sellerId: '',
    trackPrefix: '',
    yearMonth: '',
    period: '',
    startNo: '00000001',
    endNo: '00000050',
  });

  const loadTracks = async () => {
    try {
      setLoading(true);
      const res = await invoiceTrackApi.list(DEFAULT_STORE_ID);
      setTracks(res.data ?? []);
    } catch {
      setError('載入失敗');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadTracks(); }, []);

  const handleAdd = async () => {
    if (!form.sellerId || !form.trackPrefix || !form.yearMonth) return;
    setSubmitting(true);
    try {
      await invoiceTrackApi.add(form);
      setDialogOpen(false);
      setForm({ storeId: DEFAULT_STORE_ID, sellerId: '', trackPrefix: '', yearMonth: '', period: '', startNo: '00000001', endNo: '00000050' });
      await loadTracks();
      setSuccess('字軌已新增');
    } catch {
      setError('新增失敗');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>;

  return (
    <Box sx={{ p: 3 }}>
      {/* ======================================== */}
      {/* 頁面標題 / Page header */}
      {/* ======================================== */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h5" fontWeight="bold">發票字軌管理</Typography>
        <Button variant="contained" onClick={() => setDialogOpen(true)}>新增字軌</Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      {/* ======================================== */}
      {/* 字軌列表 / Track list */}
      {/* ======================================== */}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>字軌</TableCell>
            <TableCell>統一編號</TableCell>
            <TableCell>期別</TableCell>
            <TableCell>起始號</TableCell>
            <TableCell>結束號</TableCell>
            <TableCell>已用至</TableCell>
            <TableCell>使用進度</TableCell>
            <TableCell>狀態</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {tracks.map((t) => {
            const pct = calcUsagePercent(t);
            const isNearExhaust = pct >= 80;
            return (
              <TableRow key={t.id}>
                <TableCell><strong>{t.trackPrefix}</strong></TableCell>
                <TableCell>{t.sellerId}</TableCell>
                <TableCell>{t.period}</TableCell>
                <TableCell>{t.startNo}</TableCell>
                <TableCell>{t.endNo}</TableCell>
                <TableCell>{t.currentNo === '00000000' ? '未使用' : t.currentNo}</TableCell>
                <TableCell sx={{ minWidth: 120 }}>
                  <Tooltip title={`${pct}%`}>
                    <LinearProgress
                      variant="determinate"
                      value={pct}
                      color={pct >= 100 ? 'error' : isNearExhaust ? 'warning' : 'primary'}
                      sx={{ height: 8, borderRadius: 4 }}
                    />
                  </Tooltip>
                </TableCell>
                <TableCell>
                  <Chip
                    label={pct >= 100 ? '已用盡' : t.isActive ? '使用中' : '停用'}
                    color={pct >= 100 ? 'error' : t.isActive ? 'success' : 'default'}
                    size="small"
                  />
                </TableCell>
              </TableRow>
            );
          })}
          {tracks.length === 0 && (
            <TableRow>
              <TableCell colSpan={8} align="center">尚無字軌資料，請向財政部申請後登記</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      {/* ======================================== */}
      {/* 新增字軌對話框 / Add track dialog */}
      {/* ======================================== */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>新增發票字軌</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField
            label="統一編號（8碼）"
            value={form.sellerId}
            onChange={(e) => setForm({ ...form, sellerId: e.target.value })}
            inputProps={{ maxLength: 8 }}
            required
          />
          <TextField
            label="字軌前綴（2碼英文，如 AB）"
            value={form.trackPrefix}
            onChange={(e) => setForm({ ...form, trackPrefix: e.target.value.toUpperCase() })}
            inputProps={{ maxLength: 2 }}
            required
          />
          <TextField
            label="民國年月（如 11401）"
            value={form.yearMonth}
            onChange={(e) => setForm({ ...form, yearMonth: e.target.value })}
            helperText="114年01月 = 11401"
            required
          />
          <TextField
            label="發票期別（如 11401-11402）"
            value={form.period}
            onChange={(e) => setForm({ ...form, period: e.target.value })}
          />
          <TextField
            label="起始號（8碼，如 00000001）"
            value={form.startNo}
            onChange={(e) => setForm({ ...form, startNo: e.target.value })}
            inputProps={{ maxLength: 8 }}
          />
          <TextField
            label="結束號（8碼，如 00000050）"
            value={form.endNo}
            onChange={(e) => setForm({ ...form, endNo: e.target.value })}
            inputProps={{ maxLength: 8 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleAdd} disabled={submitting}>
            {submitting ? '新增中...' : '新增'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default InvoiceTrackPage;
