/**
 * @file ZReportPage.tsx
 * @description Z Report 日結頁 / Z Report daily close page
 * @description_en View and generate Z Reports with hash integrity verification
 * @description_zh 查看與產生 Z Report，包含 Hash 完整性驗證
 */
import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Button, Table, TableHead, TableRow, TableCell, TableBody,
  Chip, Alert, TextField, Dialog, DialogTitle, DialogContent, DialogActions,
  CircularProgress, Tooltip,
} from '@mui/material';
import { reportApi } from '../api/staffApi';
import type { GenerateZReportPayload, ZReport } from '../types';
import { DEFAULT_EMPLOYEE_ID, DEFAULT_STORE_ID } from '../../pos-orders/config';
import { formatDateTime, toISODateString } from '@shared/utils';


// ========================================
// Z Report 日結頁 / Z Report daily close page
// ========================================
const ZReportPage: React.FC = () => {
  const [reports, setReports] = useState<ZReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm] = useState<GenerateZReportPayload>({
    reportDate: toISODateString(new Date()),
    cashInDrawer: 0,
    generatedBy: DEFAULT_EMPLOYEE_ID,
  });

  const loadReports = async () => {
    try {
      setLoading(true);
      const res = await reportApi.listZ(DEFAULT_STORE_ID);
      setReports(res.data ?? []);
    } catch {
      setError('載入失敗');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadReports(); }, []);

  const handleGenerate = async () => {
    try {
      await reportApi.generateZ(DEFAULT_STORE_ID, form);
      setDialogOpen(false);
      await loadReports();
      setSuccess('Z Report 已產生');
    } catch {
      setError('產生失敗（可能當日已有 Z Report）');
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>;

  return (
    <Box sx={{ p: 3 }}>
      {/* ======================================== */}
      {/* 頁面標題 / Page header */}
      {/* ======================================== */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h5" fontWeight="bold">Z Report 日結</Typography>
        <Button variant="contained" onClick={() => setDialogOpen(true)}>產生今日 Z Report</Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      {/* ======================================== */}
      {/* Z Report 列表 / Z Report list */}
      {/* ======================================== */}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>報表日期</TableCell>
            <TableCell>報表編號</TableCell>
            <TableCell align="right">總銷售</TableCell>
            <TableCell align="right">退款</TableCell>
            <TableCell align="right">淨銷售</TableCell>
            <TableCell align="right">現金差異</TableCell>
            <TableCell align="right">交易數</TableCell>
            <TableCell align="right">班次數</TableCell>
            <TableCell>完整性</TableCell>
            <TableCell>產生時間</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {reports.map(r => (
            <TableRow key={r.id} hover>
              <TableCell><strong>{r.reportDate}</strong></TableCell>
              <TableCell><code>{r.reportNo}</code></TableCell>
              <TableCell align="right">${r.totalSales.toLocaleString()}</TableCell>
              <TableCell align="right">${r.totalRefunds.toLocaleString()}</TableCell>
              <TableCell align="right"><strong>${r.netSales.toLocaleString()}</strong></TableCell>
              <TableCell align="right">
                <Chip
                  label={r.cashVariance >= 0 ? `+${r.cashVariance}` : `${r.cashVariance}`}
                  color={r.cashVariance === 0 ? 'default' : r.cashVariance > 0 ? 'success' : 'error'}
                  size="small"
                />
              </TableCell>
              <TableCell align="right">{r.transactionCount}</TableCell>
              <TableCell align="right">{r.shiftCount}</TableCell>
              <TableCell>
                <Tooltip title={r.contentHash}>
                  <Chip
                    label={r.hashValid ? '✓ 完整' : '✗ 異常'}
                    color={r.hashValid ? 'success' : 'error'}
                    size="small"
                  />
                </Tooltip>
              </TableCell>
              <TableCell>{formatDateTime(r.generatedAt)}</TableCell>
            </TableRow>
          ))}
          {reports.length === 0 && (
            <TableRow>
              <TableCell colSpan={10} align="center">尚無 Z Report 記錄</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      {/* ======================================== */}
      {/* 產生 Z Report 對話框 / Generate Z Report dialog */}
      {/* ======================================== */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>產生 Z Report</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField
            type="date"
            label="報表日期"
            value={form.reportDate}
            onChange={e => setForm({ ...form, reportDate: e.target.value })}
            InputLabelProps={{ shrink: true }}
            required
          />
          <TextField
            type="number"
            label="現金抽屜金額"
            value={form.cashInDrawer}
            onChange={e => setForm({ ...form, cashInDrawer: Number(e.target.value) })}
            inputProps={{ min: 0, step: 0.01 }}
            required
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleGenerate}>產生報表</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ZReportPage;
