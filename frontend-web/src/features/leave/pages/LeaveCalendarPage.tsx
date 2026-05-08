/**
 * @file LeaveCalendarPage.tsx
 * @description 部門請假日曆頁面 / Department leave calendar page
 * @description_en Monthly view of approved leave requests across the organization
 * @description_zh 月曆視圖，顯示部門內已核准的請假記錄
 */
import { useEffect, useState } from 'react';
import {
  Box, Button, Chip, Paper, Stack, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Typography,
} from '@mui/material';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { fetchLeaveCalendar, fetchLeaveTypes } from '../api/leaveApi';
import type { LeaveRequest, LeaveType } from '../types';

export default function LeaveCalendarPage() {
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [requests, setRequests] = useState<LeaveRequest[]>([]);
  const [types, setTypes] = useState<Record<string, LeaveType>>({});

  useEffect(() => {
    fetchLeaveTypes().then(list => {
      const map: Record<string, LeaveType> = {};
      list.forEach(t => { map[t.id] = t; });
      setTypes(map);
    }).catch(() => {});
  }, []);

  const load = () => {
    const start = `${year}-${String(month).padStart(2, '0')}-01`;
    const lastDay = new Date(year, month, 0).getDate();
    const end = `${year}-${String(month).padStart(2, '0')}-${lastDay}`;
    fetchLeaveCalendar(start, end).then(setRequests).catch(() => setRequests([]));
  };

  useEffect(() => { load(); }, [year, month]);

  const prevMonth = () => { if (month === 1) { setYear(y => y - 1); setMonth(12); } else setMonth(m => m - 1); };
  const nextMonth = () => { if (month === 12) { setYear(y => y + 1); setMonth(1); } else setMonth(m => m + 1); };

  return (
    <Box>
      {/* ========================================
          月份導航 / Month navigation
          ======================================== */}
      <Stack direction="row" alignItems="center" spacing={2} mb={2}>
        <Typography variant="h5">請假日曆</Typography>
        <Box flex={1} />
        <Button onClick={prevMonth} startIcon={<ChevronLeftIcon />} variant="outlined" size="small">上月</Button>
        <Typography variant="h6" minWidth={120} textAlign="center">{year} 年 {month} 月</Typography>
        <Button onClick={nextMonth} endIcon={<ChevronRightIcon />} variant="outlined" size="small">下月</Button>
      </Stack>

      {/* ========================================
          已核准請假列表 / Approved leave list
          ======================================== */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>員工 ID</TableCell>
              <TableCell>假別</TableCell>
              <TableCell>開始日期</TableCell>
              <TableCell>結束日期</TableCell>
              <TableCell>時數</TableCell>
              <TableCell>狀態</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {requests.map(r => (
              <TableRow key={r.id}>
                <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.75rem' }}>{r.employeeId.substring(0, 8)}...</TableCell>
                <TableCell>{types[r.leaveTypeId]?.name ?? '-'}</TableCell>
                <TableCell>{r.startDate}</TableCell>
                <TableCell>{r.endDate}</TableCell>
                <TableCell>{r.totalHours} 小時</TableCell>
                <TableCell><Chip label="已核准" color="success" size="small" /></TableCell>
              </TableRow>
            ))}
            {requests.length === 0 && (
              <TableRow><TableCell colSpan={6} align="center">本月無已核准的請假記錄</TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
