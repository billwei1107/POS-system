/**
 * @file LeaveBalancePage.tsx
 * @description 員工餘假查詢頁面 / Employee leave balance page
 * @description_en Displays remaining leave days per type for an employee
 * @description_zh 顯示員工各假別的年度剩餘天數
 */
import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Card, CardContent, Chip, Grid, LinearProgress,
  Stack, TextField, Typography,
} from '@mui/material';
import { fetchLeaveBalances, fetchLeaveTypes } from '../api/leaveApi';
import type { LeaveBalance, LeaveType } from '../types';
import { organizationApi } from '../../organization/api/organizationApi';

export default function LeaveBalancePage() {
  const [employeeId, setEmployeeId] = useState('');
  const [employeeName, setEmployeeName] = useState('');
  const [balances, setBalances] = useState<LeaveBalance[]>([]);
  const [types, setTypes] = useState<Record<string, LeaveType>>({});
  const [loading, setLoading] = useState(false);
  const [employeeError, setEmployeeError] = useState('');

  useEffect(() => {
    fetchLeaveTypes().then(list => {
      const map: Record<string, LeaveType> = {};
      list.forEach(t => { map[t.id] = t; });
      setTypes(map);
    }).catch(() => {});
  }, []);

  useEffect(() => {
    const loadCurrentEmployee = async () => {
      try {
        setLoading(true);
        const employee = await organizationApi.getCurrentEmployee();
        setEmployeeId(employee.id);
        setEmployeeName(employee.name);
        setBalances(await fetchLeaveBalances(employee.id));
      } catch {
        setEmployeeError('無法取得目前登入者的員工資料，請手動輸入員工 ID。');
      } finally {
        setLoading(false);
      }
    };

    loadCurrentEmployee();
  }, []);

  const handleSearch = async (targetEmployeeId = employeeId) => {
    if (!targetEmployeeId.trim()) return;
    setLoading(true);
    try {
      setBalances(await fetchLeaveBalances(targetEmployeeId.trim()));
    } catch {
      setBalances([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      {/* ========================================
          標題與查詢列 / Header and search
          ======================================== */}
      <Typography variant="h5" mb={2}>員工餘假查詢</Typography>
      {employeeError && <Alert severity="warning" sx={{ mb: 2 }}>{employeeError}</Alert>}
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} mb={3} alignItems={{ xs: 'stretch', sm: 'center' }}>
        <TextField
          label="員工 ID"
          value={employeeId}
          onChange={e => {
            setEmployeeId(e.target.value);
            setEmployeeName('');
          }}
          size="small"
          sx={{ width: { xs: '100%', sm: 320 } }}
          placeholder={loading ? '讀取目前員工資料...' : '輸入員工 UUID'}
        />
        {employeeName && <Chip label={employeeName} color="primary" variant="outlined" />}
        <Button variant="contained" onClick={() => handleSearch()} disabled={loading || !employeeId.trim()}>查詢</Button>
      </Stack>

      {/* ========================================
          餘假卡片 / Balance cards
          ======================================== */}
      {loading && <LinearProgress sx={{ mb: 2 }} />}
      <Grid container spacing={2}>
        {balances.map(b => {
          const type = types[b.leaveTypeId];
          const pct = b.totalDays > 0 ? (b.usedDays / b.totalDays) * 100 : 0;
          return (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={b.id}>
              <Card variant="outlined">
                <CardContent>
                  <Stack direction="row" justifyContent="space-between" alignItems="center" mb={1}>
                    <Typography variant="subtitle1" fontWeight="bold">{type?.name ?? b.leaveTypeId}</Typography>
                    <Chip label={`${b.year} 年`} size="small" />
                  </Stack>
                  <Typography variant="h4" color="primary.main" fontWeight="bold">
                    {b.remainingDays} <Typography component="span" variant="body2" color="text.secondary">天剩餘</Typography>
                  </Typography>
                  <LinearProgress variant="determinate" value={pct} sx={{ mt: 1, mb: 0.5, height: 6, borderRadius: 3 }} color={pct > 80 ? 'error' : 'primary'} />
                  <Stack direction="row" justifyContent="space-between">
                    <Typography variant="caption" color="text.secondary">已用 {b.usedDays} 天</Typography>
                    <Typography variant="caption" color="text.secondary">總計 {b.totalDays} 天</Typography>
                  </Stack>
                </CardContent>
              </Card>
            </Grid>
          );
        })}
        {!loading && balances.length === 0 && employeeId && (
          <Grid size={{ xs: 12 }}>
            <Typography color="text.secondary">查無餘假資料</Typography>
          </Grid>
        )}
      </Grid>
    </Box>
  );
}
