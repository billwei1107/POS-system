/**
 * @file LeaveRequestPage.tsx
 * @description 請假申請與記錄頁面 / Leave request and history page
 * @description_en Submit leave requests and view personal leave history with cancellation support
 * @description_zh 提交請假申請並查看個人請假記錄，支援銷假操作
 */
import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  FormControl, InputLabel, MenuItem, Paper, Select, Stack, Table,
  TableBody, TableCell, TableContainer, TableHead, TableRow, TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { fetchLeaveRequests, submitLeaveRequest, cancelLeaveRequest, fetchLeaveTypes } from '../api/leaveApi';
import type { LeaveRequest, LeaveType, HalfDay, SubmitLeaveRequestPayload } from '../types';
import { organizationApi } from '../../organization/api/organizationApi';

const STATUS_LABELS: Record<string, string> = {
  PENDING: '待審核', APPROVED: '已核准', REJECTED: '已拒絕', CANCELLED: '已取消',
};
const STATUS_COLORS: Record<string, 'warning' | 'success' | 'error' | 'default'> = {
  PENDING: 'warning', APPROVED: 'success', REJECTED: 'error', CANCELLED: 'default',
};
const HALF_DAY_LABELS: Record<HalfDay, string> = { FULL: '全天', MORNING: '上午', AFTERNOON: '下午' };

const emptyForm: SubmitLeaveRequestPayload = {
  employeeId: '', leaveTypeId: '', startDate: '', endDate: '',
  startHalf: 'FULL', endHalf: 'FULL', reason: '',
};

export default function LeaveRequestPage() {
  const [employeeId, setEmployeeId] = useState('');
  const [employeeName, setEmployeeName] = useState('');
  const [requests, setRequests] = useState<LeaveRequest[]>([]);
  const [types, setTypes] = useState<LeaveType[]>([]);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<SubmitLeaveRequestPayload>(emptyForm);
  const [loadingEmployee, setLoadingEmployee] = useState(true);
  const [employeeError, setEmployeeError] = useState('');

  useEffect(() => {
    fetchLeaveTypes().then(setTypes).catch(() => {});
  }, []);

  useEffect(() => {
    const loadCurrentEmployee = async () => {
      try {
        setLoadingEmployee(true);
        const employee = await organizationApi.getCurrentEmployee();
        setEmployeeId(employee.id);
        setEmployeeName(employee.name);
        setRequests(await fetchLeaveRequests(employee.id));
      } catch {
        setEmployeeError('無法取得目前登入者的員工資料，請手動輸入員工 ID。');
      } finally {
        setLoadingEmployee(false);
      }
    };

    loadCurrentEmployee();
  }, []);

  const load = async (targetEmployeeId = employeeId) => {
    if (!targetEmployeeId.trim()) return;
    try { setRequests(await fetchLeaveRequests(targetEmployeeId.trim())); } catch { setRequests([]); }
  };

  const handleSubmit = async () => {
    try {
      await submitLeaveRequest({ ...form, employeeId: employeeId.trim() });
      setOpen(false);
      setForm(emptyForm);
      load();
    } catch { /* handled */ }
  };

  const handleCancel = async (id: string) => {
    if (!window.confirm('確定銷假？若已核准將退回餘假。')) return;
    try { await cancelLeaveRequest(id); load(); } catch { /* handled */ }
  };

  return (
    <Box>
      {/* ========================================
          標題與員工查詢 / Header and employee search
          ======================================== */}
      <Typography variant="h5" mb={2}>請假申請</Typography>
      {employeeError && <Alert severity="warning" sx={{ mb: 2 }}>{employeeError}</Alert>}
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} mb={2} alignItems={{ xs: 'stretch', sm: 'center' }}>
        <TextField
          label="員工 ID"
          value={employeeId}
          onChange={e => {
            setEmployeeId(e.target.value);
            setEmployeeName('');
          }}
          size="small"
          sx={{ width: { xs: '100%', sm: 320 } }}
          placeholder={loadingEmployee ? '讀取目前員工資料...' : '輸入員工 UUID'}
        />
        {employeeName && <Chip label={employeeName} color="primary" variant="outlined" />}
        <Button variant="outlined" onClick={() => load()} disabled={loadingEmployee || !employeeId.trim()}>查詢記錄</Button>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpen(true)} disabled={loadingEmployee || !employeeId.trim()}>申請請假</Button>
      </Stack>

      {/* ========================================
          請假記錄列表 / Request history table
          ======================================== */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>假別</TableCell>
              <TableCell>開始日期</TableCell>
              <TableCell>結束日期</TableCell>
              <TableCell>時數</TableCell>
              <TableCell>原因</TableCell>
              <TableCell>狀態</TableCell>
              <TableCell align="right">操作</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {requests.map(r => {
              const type = types.find(t => t.id === r.leaveTypeId);
              return (
                <TableRow key={r.id}>
                  <TableCell>{type?.name ?? '-'}</TableCell>
                  <TableCell>{r.startDate} ({HALF_DAY_LABELS[r.startHalf]})</TableCell>
                  <TableCell>{r.endDate} ({HALF_DAY_LABELS[r.endHalf]})</TableCell>
                  <TableCell>{r.totalHours} 小時</TableCell>
                  <TableCell>{r.reason ?? '-'}</TableCell>
                  <TableCell><Chip label={STATUS_LABELS[r.status]} color={STATUS_COLORS[r.status]} size="small" /></TableCell>
                  <TableCell align="right">
                    {(r.status === 'PENDING' || r.status === 'APPROVED') && (
                      <Button size="small" color="error" onClick={() => handleCancel(r.id)}>銷假</Button>
                    )}
                  </TableCell>
                </TableRow>
              );
            })}
            {requests.length === 0 && (
              <TableRow><TableCell colSpan={7} align="center">{loadingEmployee ? '正在載入目前員工請假記錄' : '目前沒有請假記錄'}</TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* ========================================
          請假申請對話框 / Submit leave dialog
          ======================================== */}
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>申請請假</DialogTitle>
        <DialogContent>
          <Stack spacing={2} mt={1}>
            <FormControl fullWidth required>
              <InputLabel>假別</InputLabel>
              <Select value={form.leaveTypeId} label="假別" onChange={e => setForm(f => ({ ...f, leaveTypeId: e.target.value }))}>
                {types.map(t => <MenuItem key={t.id} value={t.id}>{t.name}（{t.code}）</MenuItem>)}
              </Select>
            </FormControl>
            <Stack direction="row" spacing={2}>
              <TextField label="開始日期" type="date" value={form.startDate} onChange={e => setForm(f => ({ ...f, startDate: e.target.value }))} InputLabelProps={{ shrink: true }} fullWidth required />
              <FormControl sx={{ minWidth: 120 }}>
                <InputLabel>首日</InputLabel>
                <Select value={form.startHalf} label="首日" onChange={e => setForm(f => ({ ...f, startHalf: e.target.value as HalfDay }))}>
                  <MenuItem value="FULL">全天</MenuItem>
                  <MenuItem value="AFTERNOON">下午</MenuItem>
                </Select>
              </FormControl>
            </Stack>
            <Stack direction="row" spacing={2}>
              <TextField label="結束日期" type="date" value={form.endDate} onChange={e => setForm(f => ({ ...f, endDate: e.target.value }))} InputLabelProps={{ shrink: true }} fullWidth required />
              <FormControl sx={{ minWidth: 120 }}>
                <InputLabel>末日</InputLabel>
                <Select value={form.endHalf} label="末日" onChange={e => setForm(f => ({ ...f, endHalf: e.target.value as HalfDay }))}>
                  <MenuItem value="FULL">全天</MenuItem>
                  <MenuItem value="MORNING">上午</MenuItem>
                </Select>
              </FormControl>
            </Stack>
            <TextField label="請假原因" multiline rows={3} value={form.reason} onChange={e => setForm(f => ({ ...f, reason: e.target.value }))} fullWidth />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleSubmit} disabled={!form.leaveTypeId || !form.startDate || !form.endDate}>送出申請</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
