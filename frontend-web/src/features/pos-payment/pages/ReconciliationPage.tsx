/**
 * @file ReconciliationPage.tsx
 * @description 每日對帳頁 / Daily reconciliation page
 * @description_en Generate and confirm daily payment reconciliation per store
 * @description_zh 產生並確認每門店每日支付對帳記錄
 */
import React, { useState } from 'react';
import {
  Box, Typography, Button, Table, TableHead, TableRow, TableCell, TableBody,
  Chip, Alert, TextField, Paper, Dialog, DialogTitle, DialogContent,
  DialogActions, CircularProgress, Card, CardContent,
} from '@mui/material';
import { reconciliationApi } from '../api/paymentApi';
import type { Reconciliation, ReconStatus } from '../types';
import { DEFAULT_EMPLOYEE_ID, DEFAULT_STORE_ID } from '../../pos-orders/config';

const STATUS_COLOR: Record<ReconStatus, 'default' | 'warning' | 'success' | 'error'> = {
  PENDING: 'warning',
  MATCHED: 'success',
  DISCREPANCY: 'error',
};

const STATUS_LABEL: Record<ReconStatus, string> = {
  PENDING: '待確認',
  MATCHED: '吻合',
  DISCREPANCY: '差異',
};

// ========================================
// 每日對帳頁 / Daily reconciliation page
// ========================================
const ReconciliationPage: React.FC = () => {
  const today = new Date().toISOString().split('T')[0];
  const [date, setDate] = useState(today);
  const [records, setRecords] = useState<Reconciliation[]>([]);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // ========================================
  // 確認對帳對話框狀態 / Confirm reconciliation dialog state
  // ========================================
  const [confirmDialog, setConfirmDialog] = useState<{ open: boolean; reconId: string }>({
    open: false,
    reconId: '',
  });
  const [gatewayAmount, setGatewayAmount] = useState('');
  const [reconciledBy, setReconciledBy] = useState(DEFAULT_EMPLOYEE_ID);

  const loadRecords = async () => {
    if (!date) return;
    setLoading(true);
    setError('');
    try {
      const res = await reconciliationApi.list(DEFAULT_STORE_ID, date);
      setRecords(res.data ?? []);
    } catch {
      setError('查詢失敗');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerate = async () => {
    if (!date) return;
    setGenerating(true);
    setError('');
    try {
      const res = await reconciliationApi.generate(DEFAULT_STORE_ID, date);
      setRecords(res.data ?? []);
      setSuccess(`已產生 ${res.data?.length ?? 0} 筆對帳記錄`);
    } catch {
      setError('產生對帳失敗');
    } finally {
      setGenerating(false);
    }
  };

  const handleConfirm = async () => {
    if (!gatewayAmount || !reconciledBy) return;
    try {
      await reconciliationApi.confirm(confirmDialog.reconId, Number(gatewayAmount), reconciledBy);
      setConfirmDialog({ open: false, reconId: '' });
      setGatewayAmount('');
      setReconciledBy(DEFAULT_EMPLOYEE_ID);
      await loadRecords();
      setSuccess('對帳已確認');
    } catch {
      setError('確認失敗');
    }
  };

  const fmt = (v: number | null) =>
    v === null ? '-' : `NT$ ${Number(v).toLocaleString('zh-TW', { minimumFractionDigits: 2 })}`;

  const renderReconciliationCards = () => (
    <Box sx={{ display: { xs: 'flex', md: 'none' }, flexDirection: 'column', gap: 1.25 }}>
      {loading || generating ? (
        <Card variant="outlined" sx={{ borderRadius: 2 }}>
          <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <CircularProgress size={20} />
            <Typography variant="body2" color="text.secondary">
              {generating ? '產生對帳中...' : '查詢對帳中...'}
            </Typography>
          </CardContent>
        </Card>
      ) : records.length === 0 ? (
        <Card variant="outlined" sx={{ borderRadius: 2 }}>
          <CardContent>
            <Typography variant="body2" color="text.secondary">尚無對帳記錄，請先產生</Typography>
          </CardContent>
        </Card>
      ) : records.map((record) => (
        <Card key={record.id} variant="outlined" sx={{ borderRadius: 2 }}>
          <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.25 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 1 }}>
              <Box sx={{ minWidth: 0 }}>
                <Typography variant="caption" color="text.secondary">支付方式類型</Typography>
                <Typography variant="body2" fontWeight={800}>{record.methodType}</Typography>
              </Box>
              <Chip label={STATUS_LABEL[record.status]} color={STATUS_COLOR[record.status]} size="small" />
            </Box>

            <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 1 }}>
              <Box>
                <Typography variant="caption" color="text.secondary">交易筆數</Typography>
                <Typography variant="body2" fontWeight={800}>{record.transactionCount}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">淨額</Typography>
                <Typography variant="body2" fontWeight={800}>{fmt(record.netAmount)}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">交易金額</Typography>
                <Typography variant="body2">{fmt(record.totalAmount)}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">退款金額</Typography>
                <Typography variant="body2">{fmt(record.refundAmount)}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">閘道金額</Typography>
                <Typography variant="body2">{fmt(record.gatewayAmount)}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">差異</Typography>
                <Typography
                  variant="body2"
                  color={record.variance && record.variance !== 0 ? 'error.main' : 'text.primary'}
                >
                  {fmt(record.variance)}
                </Typography>
              </Box>
            </Box>

            {record.status === 'PENDING' && (
              <Button
                size="small"
                variant="outlined"
                onClick={() => setConfirmDialog({ open: true, reconId: record.id })}
                sx={{ alignSelf: 'flex-end' }}
              >
                確認
              </Button>
            )}
          </CardContent>
        </Card>
      ))}
    </Box>
  );

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" fontWeight="bold" sx={{ mb: 2 }}>每日對帳</Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      {/* ======================================== */}
      {/* 日期選擇與操作列 / Date selector and action bar */}
      {/* ======================================== */}
      <Paper sx={{
        p: 2,
        mb: 3,
        display: 'flex',
        flexDirection: { xs: 'column', sm: 'row' },
        gap: 2,
        alignItems: { xs: 'stretch', sm: 'center' },
      }}>
        <TextField
          type="date"
          label="對帳日期"
          value={date}
          onChange={(e) => setDate(e.target.value)}
          InputLabelProps={{ shrink: true }}
          size="small"
        />
        <Button variant="outlined" onClick={loadRecords} disabled={loading}>
          {loading ? <CircularProgress size={20} /> : '查詢'}
        </Button>
        <Button variant="contained" onClick={handleGenerate} disabled={generating}>
          {generating ? '產生中...' : '產生對帳'}
        </Button>
      </Paper>

      {/* ======================================== */}
      {/* 對帳記錄列表 / Reconciliation records table */}
      {/* ======================================== */}
      {renderReconciliationCards()}

      <Box sx={{ display: { xs: 'none', md: 'block' } }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>支付方式類型</TableCell>
              <TableCell align="right">交易筆數</TableCell>
              <TableCell align="right">交易金額</TableCell>
              <TableCell align="right">退款金額</TableCell>
              <TableCell align="right">淨額</TableCell>
              <TableCell align="right">閘道金額</TableCell>
              <TableCell align="right">差異</TableCell>
              <TableCell>狀態</TableCell>
              <TableCell>操作</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {records.map((r) => (
              <TableRow key={r.id}>
                <TableCell>{r.methodType}</TableCell>
                <TableCell align="right">{r.transactionCount}</TableCell>
                <TableCell align="right">{fmt(r.totalAmount)}</TableCell>
                <TableCell align="right">{fmt(r.refundAmount)}</TableCell>
                <TableCell align="right">{fmt(r.netAmount)}</TableCell>
                <TableCell align="right">{fmt(r.gatewayAmount)}</TableCell>
                <TableCell align="right" sx={{ color: r.variance && r.variance !== 0 ? 'error.main' : 'inherit' }}>
                  {fmt(r.variance)}
                </TableCell>
                <TableCell>
                  <Chip
                    label={STATUS_LABEL[r.status]}
                    color={STATUS_COLOR[r.status]}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  {r.status === 'PENDING' && (
                    <Button
                      size="small"
                      variant="outlined"
                      onClick={() => setConfirmDialog({ open: true, reconId: r.id })}
                    >
                      確認
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
            {records.length === 0 && (
              <TableRow>
                <TableCell colSpan={9} align="center">尚無對帳記錄，請先產生</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Box>

      {/* ======================================== */}
      {/* 確認對帳對話框 / Confirm reconciliation dialog */}
      {/* ======================================== */}
      <Dialog open={confirmDialog.open} onClose={() => setConfirmDialog({ open: false, reconId: '' })} maxWidth="xs" fullWidth>
        <DialogTitle>確認對帳</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField
            label="閘道結算金額"
            type="number"
            value={gatewayAmount}
            onChange={(e) => setGatewayAmount(e.target.value)}
            required
          />
          <TextField
            label="確認人員 ID"
            value={reconciledBy}
            onChange={(e) => setReconciledBy(e.target.value)}
            required
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDialog({ open: false, reconId: '' })}>取消</Button>
          <Button variant="contained" onClick={handleConfirm} disabled={!gatewayAmount || !reconciledBy}>
            確認
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ReconciliationPage;
