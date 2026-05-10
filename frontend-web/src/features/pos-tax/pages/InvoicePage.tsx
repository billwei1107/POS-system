/**
 * @file InvoicePage.tsx
 * @description 電子發票查詢頁 / E-invoice query page
 * @description_en Query and manage issued e-invoices by date range; void individual invoices
 * @description_zh 依日期範圍查詢與管理已開立的電子發票，支援手動作廢
 */
import React, { useState } from 'react';
import {
  Box, Typography, Button, Table, TableHead, TableRow, TableCell, TableBody,
  Chip, Alert, TextField, Paper, Dialog, DialogTitle, DialogContent,
  DialogActions, CircularProgress,
} from '@mui/material';
import { invoiceApi } from '../api/taxApi';
import type { Invoice, InvoiceStatus, UploadStatus } from '../types';
import { DEFAULT_STORE_ID } from '../../pos-orders/config';

const STATUS_COLOR: Record<InvoiceStatus, 'default' | 'success' | 'error' | 'warning'> = {
  ISSUED: 'success',
  VOIDED: 'error',
  ALLOWANCE: 'warning',
};

const STATUS_LABEL: Record<InvoiceStatus, string> = {
  ISSUED: '有效',
  VOIDED: '已作廢',
  ALLOWANCE: '折讓',
};

const UPLOAD_COLOR: Record<UploadStatus, 'default' | 'success' | 'error' | 'warning'> = {
  PENDING: 'warning',
  SUCCESS: 'success',
  FAILED: 'error',
};

const UPLOAD_LABEL: Record<UploadStatus, string> = {
  PENDING: '待上傳',
  SUCCESS: '已上傳',
  FAILED: '上傳失敗',
};

// ========================================
// 電子發票查詢頁 / E-invoice query page
// ========================================
const InvoicePage: React.FC = () => {
  const today = new Date().toISOString().split('T')[0];
  const [fromDate, setFromDate] = useState(today);
  const [toDate, setToDate] = useState(today);
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // ========================================
  // 作廢對話框狀態 / Void dialog state
  // ========================================
  const [voidDialog, setVoidDialog] = useState<{ open: boolean; invoiceId: string; fullNo: string }>({
    open: false, invoiceId: '', fullNo: '',
  });
  const [voidReason, setVoidReason] = useState('');

  const loadInvoices = async () => {
    if (!fromDate || !toDate) return;
    setLoading(true);
    setError('');
    try {
      const from = `${fromDate}T00:00:00`;
      const to = `${toDate}T23:59:59`;
      const res = await invoiceApi.list(DEFAULT_STORE_ID, from, to);
      setInvoices(res.data ?? []);
    } catch {
      setError('查詢失敗');
    } finally {
      setLoading(false);
    }
  };

  const handleVoid = async () => {
    try {
      await invoiceApi.void(voidDialog.invoiceId, voidReason || '手動作廢');
      setVoidDialog({ open: false, invoiceId: '', fullNo: '' });
      setVoidReason('');
      await loadInvoices();
      setSuccess('發票已作廢');
    } catch {
      setError('作廢失敗');
    }
  };

  const fmt = (v: number) =>
    `NT$ ${Number(v).toLocaleString('zh-TW', { minimumFractionDigits: 2 })}`;

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" fontWeight="bold" sx={{ mb: 2 }}>電子發票查詢</Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      {/* ======================================== */}
      {/* 日期範圍查詢列 / Date range query bar */}
      {/* ======================================== */}
      <Paper sx={{ p: 2, mb: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
        <TextField
          type="date"
          label="開始日期"
          value={fromDate}
          onChange={(e) => setFromDate(e.target.value)}
          InputLabelProps={{ shrink: true }}
          size="small"
        />
        <TextField
          type="date"
          label="結束日期"
          value={toDate}
          onChange={(e) => setToDate(e.target.value)}
          InputLabelProps={{ shrink: true }}
          size="small"
        />
        <Button variant="outlined" onClick={loadInvoices} disabled={loading}>
          {loading ? <CircularProgress size={20} /> : '查詢'}
        </Button>
      </Paper>

      {/* ======================================== */}
      {/* 發票列表 / Invoice list */}
      {/* ======================================== */}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>發票號碼</TableCell>
            <TableCell>類型</TableCell>
            <TableCell>買方</TableCell>
            <TableCell align="right">稅前金額</TableCell>
            <TableCell align="right">稅額</TableCell>
            <TableCell align="right">含稅總額</TableCell>
            <TableCell>狀態</TableCell>
            <TableCell>上傳</TableCell>
            <TableCell>開立時間</TableCell>
            <TableCell>操作</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {invoices.map((inv) => (
            <TableRow key={inv.id}>
              <TableCell><strong>{inv.fullInvoiceNo ?? '（未分配字軌）'}</strong></TableCell>
              <TableCell>
                <Chip label={inv.invoiceType} size="small" color={inv.invoiceType === 'B2B' ? 'primary' : 'default'} />
              </TableCell>
              <TableCell>{inv.buyerName ?? inv.buyerId ?? '-'}</TableCell>
              <TableCell align="right">{fmt(inv.salesAmount)}</TableCell>
              <TableCell align="right">{fmt(inv.taxAmount)}</TableCell>
              <TableCell align="right">{fmt(inv.totalAmount)}</TableCell>
              <TableCell>
                <Chip label={STATUS_LABEL[inv.status]} color={STATUS_COLOR[inv.status]} size="small" />
              </TableCell>
              <TableCell>
                <Chip label={UPLOAD_LABEL[inv.uploadStatus]} color={UPLOAD_COLOR[inv.uploadStatus]} size="small" variant="outlined" />
              </TableCell>
              <TableCell>{new Date(inv.issueAt).toLocaleString('zh-TW')}</TableCell>
              <TableCell>
                {inv.status === 'ISSUED' && (
                  <Button
                    size="small"
                    color="error"
                    variant="outlined"
                    onClick={() => setVoidDialog({ open: true, invoiceId: inv.id, fullNo: inv.fullInvoiceNo ?? inv.id })}
                  >
                    作廢
                  </Button>
                )}
              </TableCell>
            </TableRow>
          ))}
          {invoices.length === 0 && (
            <TableRow>
              <TableCell colSpan={10} align="center">尚無發票記錄，請先查詢</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      {/* ======================================== */}
      {/* 作廢確認對話框 / Void confirmation dialog */}
      {/* ======================================== */}
      <Dialog open={voidDialog.open} onClose={() => setVoidDialog({ open: false, invoiceId: '', fullNo: '' })} maxWidth="xs" fullWidth>
        <DialogTitle>確認作廢發票</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <Typography variant="body2" color="text.secondary">
            發票號碼：<strong>{voidDialog.fullNo}</strong>
          </Typography>
          <TextField
            label="作廢原因"
            value={voidReason}
            onChange={(e) => setVoidReason(e.target.value)}
            placeholder="手動作廢"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setVoidDialog({ open: false, invoiceId: '', fullNo: '' })}>取消</Button>
          <Button variant="contained" color="error" onClick={handleVoid}>確認作廢</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default InvoicePage;
