/**
 * @file LeaveTypePage.tsx
 * @description 假別類型管理頁面 / Leave type management page
 * @description_en CRUD management for leave types (annual, sick, personal, etc.)
 * @description_zh 假別類型的新增、編輯、刪除管理頁面
 */
import { useCallback, useEffect, useState } from 'react';
import {
  Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  FormControlLabel, IconButton, Paper, Stack, Switch, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
  Select, MenuItem, FormControl, InputLabel,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { fetchLeaveTypes, createLeaveType, updateLeaveType, deleteLeaveType } from '../api/leaveApi';
import type { LeaveType, PaidType } from '../types';

const PAID_TYPE_LABELS: Record<PaidType, string> = {
  PAID: '有薪',
  UNPAID: '無薪',
  HALF_PAY: '半薪',
};

const PAID_TYPE_COLORS: Record<PaidType, 'success' | 'error' | 'warning'> = {
  PAID: 'success',
  UNPAID: 'error',
  HALF_PAY: 'warning',
};

const emptyForm = { name: '', code: '', paidType: 'PAID' as PaidType, requireAttachment: false, maxDaysPerYear: '' };

export default function LeaveTypePage() {
  const [types, setTypes] = useState<LeaveType[]>([]);
  const [open, setOpen] = useState(false);
  const [editId, setEditId] = useState<string | null>(null);
  const [form, setForm] = useState(emptyForm);

  const load = useCallback(() => {
    fetchLeaveTypes().then(setTypes).catch(() => { /* handled by interceptor */ });
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleOpen = (type?: LeaveType) => {
    if (type) {
      setEditId(type.id);
      setForm({ name: type.name, code: type.code, paidType: type.paidType, requireAttachment: type.requireAttachment, maxDaysPerYear: type.maxDaysPerYear?.toString() ?? '' });
    } else {
      setEditId(null);
      setForm(emptyForm);
    }
    setOpen(true);
  };

  const handleSave = async () => {
    const payload = { ...form, maxDaysPerYear: form.maxDaysPerYear ? Number(form.maxDaysPerYear) : null };
    try {
      if (editId) await updateLeaveType(editId, payload);
      else await createLeaveType(payload);
      setOpen(false);
      load();
    } catch { /* handled */ }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('確定刪除此假別？')) return;
    try { await deleteLeaveType(id); load(); } catch { /* handled */ }
  };

  return (
    <Box>
      {/* ========================================
          標題列 / Header
          ======================================== */}
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h5">假別類型管理</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpen()}>新增假別</Button>
      </Stack>

      {/* ========================================
          假別列表 / Leave type table
          ======================================== */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>假別代碼</TableCell>
              <TableCell>假別名稱</TableCell>
              <TableCell>薪資類型</TableCell>
              <TableCell>需附件</TableCell>
              <TableCell>年度上限（天）</TableCell>
              <TableCell align="right">操作</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {types.map(t => (
              <TableRow key={t.id}>
                <TableCell><code>{t.code}</code></TableCell>
                <TableCell>{t.name}</TableCell>
                <TableCell>
                  <Chip label={PAID_TYPE_LABELS[t.paidType]} color={PAID_TYPE_COLORS[t.paidType]} size="small" />
                </TableCell>
                <TableCell>{t.requireAttachment ? '是' : '否'}</TableCell>
                <TableCell>{t.maxDaysPerYear ?? '無限制'}</TableCell>
                <TableCell align="right">
                  <IconButton size="small" onClick={() => handleOpen(t)}><EditIcon fontSize="small" /></IconButton>
                  <IconButton size="small" color="error" onClick={() => handleDelete(t.id)}><DeleteIcon fontSize="small" /></IconButton>
                </TableCell>
              </TableRow>
            ))}
            {types.length === 0 && (
              <TableRow><TableCell colSpan={6} align="center">尚無假別，請點擊新增</TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* ========================================
          新增/編輯對話框 / Create/Edit dialog
          ======================================== */}
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editId ? '編輯假別' : '新增假別'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} mt={1}>
            <TextField label="假別名稱" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} fullWidth required />
            <TextField label="假別代碼" value={form.code} onChange={e => setForm(f => ({ ...f, code: e.target.value.toUpperCase() }))} fullWidth required disabled={!!editId} helperText="建立後不可修改（如 ANNUAL、SICK）" />
            <FormControl fullWidth>
              <InputLabel>薪資類型</InputLabel>
              <Select value={form.paidType} label="薪資類型" onChange={e => setForm(f => ({ ...f, paidType: e.target.value as PaidType }))}>
                <MenuItem value="PAID">有薪（PAID）</MenuItem>
                <MenuItem value="UNPAID">無薪（UNPAID）</MenuItem>
                <MenuItem value="HALF_PAY">半薪（HALF_PAY）</MenuItem>
              </Select>
            </FormControl>
            <TextField label="年度上限（天）" type="number" value={form.maxDaysPerYear} onChange={e => setForm(f => ({ ...f, maxDaysPerYear: e.target.value }))} fullWidth helperText="留空表示無限制" />
            <FormControlLabel control={<Switch checked={form.requireAttachment} onChange={e => setForm(f => ({ ...f, requireAttachment: e.target.checked }))} />} label="需要附件" />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleSave} disabled={!form.name || !form.code}>儲存</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
