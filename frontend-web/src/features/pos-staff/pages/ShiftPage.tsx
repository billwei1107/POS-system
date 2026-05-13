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
  CircularProgress, Card, CardContent,
} from '@mui/material';
import { formatDateTime } from '@shared/utils';
import { shiftApi } from '../api/staffApi';
import { cashDrawerApi } from '../../pos-payment/api/paymentApi';
import type { OpenShiftPayload, StaffShift } from '../types';
import { DEFAULT_EMPLOYEE_ID, DEFAULT_STORE_ID, DEFAULT_TERMINAL_ID } from '../../pos-orders/config';

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
  const [openForm, setOpenForm] = useState<OpenShiftPayload>({
    employeeId: DEFAULT_EMPLOYEE_ID,
    terminalId: DEFAULT_TERMINAL_ID,
    openingCash: 1000,
  });
  const [closingCash, setClosingCash] = useState('');

  const loadShifts = async () => {
    try {
      setLoading(true);
      const res = await shiftApi.listOpen(DEFAULT_STORE_ID);
      setShifts(res.data ?? []);
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
      await shiftApi.open(DEFAULT_STORE_ID, openForm);
      try {
        await cashDrawerApi.open({
          storeId: DEFAULT_STORE_ID,
          terminalId: openForm.terminalId || DEFAULT_TERMINAL_ID,
          openedBy: openForm.employeeId,
          openingAmount: openForm.openingCash,
        });
      } catch {
        // 現金抽屜可能已由同終端機開啟；開班成功時不阻斷班次流程。
      }
      setOpenDialog(false);
      setOpenForm({ employeeId: DEFAULT_EMPLOYEE_ID, terminalId: DEFAULT_TERMINAL_ID, openingCash: 1000 });
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
      try {
        const drawer = await cashDrawerApi.getOpen(DEFAULT_TERMINAL_ID);
        if (drawer.data) {
          await cashDrawerApi.close(drawer.data.id, DEFAULT_EMPLOYEE_ID, Number(closingCash), 'shift close');
        }
      } catch {
        // 若沒有開啟中的抽屜，仍保留班次關班結果。
      }
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

  const renderShiftActions = (shift: StaffShift) => shift.status === 'OPEN' && (
    <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, gap: 1, width: { xs: '100%', sm: 'auto' } }}>
      <Button
        size="small"
        variant="outlined"
        color="primary"
        onClick={() => setCloseDialog({ open: true, shiftId: shift.id })}
        sx={{ minWidth: { sm: 88 } }}
      >
        關班
      </Button>
      <Button
        size="small"
        variant="outlined"
        color="warning"
        onClick={() => handleBlindClose(shift.id)}
        sx={{ minWidth: { sm: 112 } }}
      >
        盲點結算
      </Button>
    </Box>
  );

  const renderShiftCards = () => (
    <Box sx={{ display: { xs: 'flex', md: 'none' }, flexDirection: 'column', gap: 1.25 }}>
      {shifts.length === 0 ? (
        <Card variant="outlined" sx={{ borderRadius: 2 }}>
          <CardContent>
            <Typography variant="body2" color="text.secondary">尚無開放班次</Typography>
          </CardContent>
        </Card>
      ) : shifts.map((shift) => (
        <Card key={shift.id} variant="outlined" sx={{ borderRadius: 2 }}>
          <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.25 }}>
            <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 1 }}>
              <Box sx={{ minWidth: 0 }}>
                <Typography variant="caption" color="text.secondary">班次編號</Typography>
                <Typography variant="body2" fontWeight={800} sx={{ overflowWrap: 'anywhere' }}>
                  {shift.shiftNo}
                </Typography>
              </Box>
              <Chip label={STATUS_LABEL[shift.status] ?? shift.status} color={STATUS_COLOR[shift.status] ?? 'default'} size="small" />
            </Box>

            <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 1 }}>
              <Box>
                <Typography variant="caption" color="text.secondary">員工 ID</Typography>
                <Typography variant="body2" sx={{ fontFamily: 'monospace', overflowWrap: 'anywhere' }}>{shift.employeeId}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">交易筆數</Typography>
                <Typography variant="body2" fontWeight={800}>{shift.transactionCount}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">開班現金</Typography>
                <Typography variant="body2">{shift.openingCash.toLocaleString()}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">淨銷售</Typography>
                <Typography variant="body2" fontWeight={800}>{shift.netSales.toLocaleString()}</Typography>
              </Box>
              <Box sx={{ gridColumn: '1 / -1' }}>
                <Typography variant="caption" color="text.secondary">開班時間</Typography>
                <Typography variant="body2">{formatDateTime(shift.openedAt)}</Typography>
              </Box>
            </Box>

            {renderShiftActions(shift)}
          </CardContent>
        </Card>
      ))}
    </Box>
  );

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>;

  return (
    <Box sx={{ p: 3 }}>
      {/* ======================================== */}
      {/* 頁面標題 / Page header */}
      {/* ======================================== */}
      <Box sx={{
        display: 'flex',
        flexDirection: { xs: 'column', sm: 'row' },
        justifyContent: 'space-between',
        alignItems: { xs: 'stretch', sm: 'center' },
        gap: 1.5,
        mb: 2,
      }}>
        <Typography variant="h5" fontWeight="bold">班次管理</Typography>
        <Button variant="contained" onClick={() => setOpenDialog(true)}>新增開班</Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      {/* ======================================== */}
      {/* 班次列表 / Shift list */}
      {/* ======================================== */}
      {renderShiftCards()}

      <Box sx={{ display: { xs: 'none', md: 'block' } }}>
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
                <TableCell>{formatDateTime(s.openedAt)}</TableCell>
                <TableCell align="right">{s.openingCash.toLocaleString()}</TableCell>
                <TableCell align="right">{s.netSales.toLocaleString()}</TableCell>
                <TableCell align="right">{s.transactionCount}</TableCell>
                <TableCell>{renderShiftActions(s)}</TableCell>
              </TableRow>
            ))}
            {shifts.length === 0 && (
              <TableRow>
                <TableCell colSpan={8} align="center">尚無開放班次</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Box>

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
            label="終端機 ID"
            value={openForm.terminalId ?? ''}
            onChange={e => setOpenForm({ ...openForm, terminalId: e.target.value })}
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
