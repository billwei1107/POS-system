/**
 * @file ShiftPage.tsx
 * @description 班次管理頁 / Shift management page
 * @description_en Manage staff shifts: open, close, blind close, and view open shifts
 * @description_zh 管理班次：開班、關班、盲點結算、查看當前開放班次
 */
import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Button, Table, TableHead, TableRow, TableCell, TableBody,
  Chip, Alert, TextField, Dialog, DialogTitle, DialogContent, DialogActions,
  CircularProgress,
} from '@mui/material';
import { shiftApi } from '../api/staffApi';
import type { OpenShiftPayload, StaffShift } from '../types';

const STORE_ID = import.meta.env.VITE_DEFAULT_STORE_ID as string;

const STATUS_LABEL: Record<string, string> = {
  OPEN: '開班中',
  CLOSED: '已關班',
  BLIND_CLOSED: '盲點結算',
};

const STATUS_COLOR: Record<string, 'success' | 'default' | 'warning'> = {
  OPEN: 'success',
  CLOSED: 'default',
  BLIND_CLOSED: 'warning',
};

// ========================================
// 班次管理頁 / Shift management page
// ========================================
const ShiftPage: React.FC = () => {
  const [shifts, setShifts] = useState<StaffShift[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [openDialog, setOpenDialog] = useState(false);
  const [closeDialog, setCloseDialog] = useState<{ open: boolean; shiftId: string }>({ open: false, shiftId: '' });

  // ========================================
  // 開班表單 / Open shift form
  // ========================================
  const [openForm, setOpenForm] = useState<OpenShiftPayload>({ employeeId: '', openingCash: 0 });
  const [closingCash, setClosingCash] = useState('');

  const loadShifts = async () => {
    try {
      setLoading(true);
      const res = await shiftApi.listOpen(STORE_ID);
      setShifts(res.data.data ?? []);
    } catch {
      setError('載入失敗');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadShifts(); }, []);

  const handleOpen = async () => {
    if (!openForm.employeeId) return;
    try {
      await shiftApi.open(STORE_ID, openForm);
      setOpenDialog(false);
      setOpenForm({ employeeId: '', openingCash: 0 });
      await loadShifts();
      setSuccess('班次已開啟');
    } catch {
      setError('開班失敗');
    }
  };

  const handleClose = async () => {
    if (!closingCash) return;
    try {
      await shiftApi.close(closeDialog.shiftId, { closingCash: Number(closingCash) });
      setCloseDialog({ open: false, shiftId: '' });
      setClosingCash('');
      await loadShifts();
      setSuccess('班次已關閉');
    } catch {
      setError('關班失敗');
    }
  };

  const handleBlindClose = async (shiftId: string) => {
    if (!window.confirm('確認盲點結算？此操作不需要現金清點。')) return;
    try {
      await shiftApi.blindClose(shiftId);
      await loadShifts();
      setSuccess('盲點結算完成');
    } catch {
      setError('盲點結算失敗');
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>;

  return (
    <Box sx={{ p: 3 }}>
      {/* ======================================== */}
      {/* 頁面標題 / Page header */}
      {/* ======================================== */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h5" fontWeight="bold">班次管理</Typography>
        <Button variant="contained" onClick={() => setOpenDialog(true)}>新增開班</Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      {/* ======================================== */}
      {/* 班次列表 / Shift list */}
      {/* ======================================== */}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>班次編號</TableCell>
            <TableCell>員工 ID</TableCell>
            <TableCell>狀態</TableCell>
            <TableCell>開班時間</TableCell>
            <TableCell align="right">開班現金</TableCell>
            <TableCell align="right">淨銷售</TableCell>
            <TableCell align="right">交易筆數</TableCell>
            <TableCell>操作</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {shifts.map(s => (
            <TableRow key={s.id} hover>
              <TableCell><strong>{s.shiftNo}</strong></TableCell>
              <TableCell><code>{s.employeeId.slice(0, 8)}...</code></TableCell>
              <TableCell>
                <Chip label={STATUS_LABEL[s.status] ?? s.status} color={STATUS_COLOR[s.status] ?? 'default'} size="small" />
              </TableCell>
              <TableCell>{new Date(s.openedAt).toLocaleString('zh-TW')}</TableCell>
              <TableCell align="right">{s.openingCash.toLocaleString()}</TableCell>
              <TableCell align="right">{s.netSales.toLocaleString()}</TableCell>
              <TableCell align="right">{s.transactionCount}</TableCell>
              <TableCell>
                {s.status === 'OPEN' && (
                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <Button size="small" variant="outlined" color="primary"
                      onClick={() => setCloseDialog({ open: true, shiftId: s.id })}>關班</Button>
                    <Button size="small" variant="outlined" color="warning"
                      onClick={() => handleBlindClose(s.id)}>盲點結算</Button>
                  </Box>
                )}
              </TableCell>
            </TableRow>
          ))}
          {shifts.length === 0 && (
            <TableRow>
              <TableCell colSpan={8} align="center">尚無開放班次</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      {/* ======================================== */}
      {/* 開班對話框 / Open shift dialog */}
      {/* ======================================== */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>新增開班</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField
            label="員工 ID"
            value={openForm.employeeId}
            onChange={e => setOpenForm({ ...openForm, employeeId: e.target.value })}
            required
          />
          <TextField
            type="number"
            label="開班現金"
            value={openForm.openingCash}
            onChange={e => setOpenForm({ ...openForm, openingCash: Number(e.target.value) })}
            inputProps={{ min: 0, step: 0.01 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>取消</Button>
          <Button variant="contained" onClick={handleOpen}>開班</Button>
        </DialogActions>
      </Dialog>

      {/* ======================================== */}
      {/* 關班對話框 / Close shift dialog */}
      {/* ======================================== */}
      <Dialog open={closeDialog.open} onClose={() => setCloseDialog({ open: false, shiftId: '' })} maxWidth="xs" fullWidth>
        <DialogTitle>確認關班</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <TextField
            type="number"
            label="現金清點金額"
            value={closingCash}
            onChange={e => setClosingCash(e.target.value)}
            fullWidth
            required
            inputProps={{ min: 0, step: 0.01 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCloseDialog({ open: false, shiftId: '' })}>取消</Button>
          <Button variant="contained" color="primary" onClick={handleClose}>確認關班</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ShiftPage;
