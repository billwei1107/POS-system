/**
 * @file AuditLogPage.tsx
 * @description 稽核紀錄頁 / Audit log page
 * @description_en Admin page for browsing sensitive operation audit records
 * @description_zh 後台查詢敏感操作稽核紀錄的管理頁
 */
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  MenuItem,
  Pagination,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import ManageSearchIcon from '@mui/icons-material/ManageSearch';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import { formatDateTime } from '@shared/utils';
import { auditApi } from '../api/auditApi';
import type { AuditLog, AuditStatus } from '../types';

const PAGE_SIZE = 10;

const STATUS_LABEL: Record<AuditStatus, string> = {
  SUCCESS: '成功',
  FAILED: '失敗',
};

const STATUS_COLOR: Record<AuditStatus, 'success' | 'error'> = {
  SUCCESS: 'success',
  FAILED: 'error',
};

const MODULE_OPTIONS = [
  { value: '', label: '全部模組' },
  { value: 'pos-refund', label: '退款' },
  { value: 'inventory-stock', label: '庫存' },
  { value: 'inventory-stock-take', label: '盤點' },
  { value: 'inventory-transfer', label: '調撥' },
  { value: 'inventory-alert', label: '庫存警示' },
];

const STATUS_OPTIONS: { value: '' | AuditStatus; label: string }[] = [
  { value: '', label: '全部狀態' },
  { value: 'SUCCESS', label: '成功' },
  { value: 'FAILED', label: '失敗' },
];

// ========================================
// 稽核紀錄頁 / Audit log page
// ========================================
const AuditLogPage: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [moduleFilter, setModuleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState<'' | AuditStatus>('');
  const [actionFilter, setActionFilter] = useState('');
  const [appliedAction, setAppliedAction] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const stats = useMemo(() => {
    const success = logs.filter(log => log.status === 'SUCCESS').length;
    const failed = logs.filter(log => log.status === 'FAILED').length;
    const avgDuration = logs.length > 0
      ? Math.round(logs.reduce((sum, log) => sum + Number(log.durationMs || 0), 0) / logs.length)
      : 0;
    return { success, failed, avgDuration };
  }, [logs]);

  const loadLogs = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const res = await auditApi.search({
        module: moduleFilter,
        status: statusFilter,
        action: appliedAction.trim(),
        page: page - 1,
        size: PAGE_SIZE,
      });
      const data = res.data;
      setLogs(data?.content ?? []);
      setTotalPages(Math.max(data?.totalPages ?? 1, 1));
      setTotalElements(data?.totalElements ?? 0);
    } catch {
      setError('稽核紀錄載入失敗，請確認帳號具備稽核查詢權限。');
      setLogs([]);
      setTotalPages(1);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  }, [appliedAction, moduleFilter, page, statusFilter]);

  useEffect(() => { loadLogs(); }, [loadLogs]);

  const handleApplyFilters = () => {
    setPage(1);
    setAppliedAction(actionFilter);
  };

  const handleResetFilters = () => {
    setModuleFilter('');
    setStatusFilter('');
    setActionFilter('');
    setAppliedAction('');
    setPage(1);
  };

  const renderUser = (log: AuditLog) => {
    if (!log.userId) return '-';
    return log.userId.slice(0, 8);
  };

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h3" fontWeight={900} sx={{ fontSize: { xs: '2rem', md: '3rem' } }}>
          稽核紀錄
        </Typography>
        <Typography color="text.secondary" fontWeight={600} sx={{ mt: 0.75 }}>
          追蹤退款、庫存、盤點與調撥等敏感操作的執行結果。
        </Typography>
      </Box>

      {error && <Alert severity="error" onClose={() => setError('')}>{error}</Alert>}

      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(4, minmax(0, 1fr))' }, gap: 1.5 }}>
        <Card variant="outlined" sx={{ borderRadius: 1 }}>
          <CardContent>
            <Typography color="text.secondary" fontWeight={800}>符合筆數</Typography>
            <Typography variant="h4" fontWeight={900}>{totalElements}</Typography>
          </CardContent>
        </Card>
        <Card variant="outlined" sx={{ borderRadius: 1 }}>
          <CardContent>
            <Typography color="text.secondary" fontWeight={800}>本頁成功</Typography>
            <Typography variant="h4" fontWeight={900} color="success.main">{stats.success}</Typography>
          </CardContent>
        </Card>
        <Card variant="outlined" sx={{ borderRadius: 1 }}>
          <CardContent>
            <Typography color="text.secondary" fontWeight={800}>本頁失敗</Typography>
            <Typography variant="h4" fontWeight={900} color="error.main">{stats.failed}</Typography>
          </CardContent>
        </Card>
        <Card variant="outlined" sx={{ borderRadius: 1 }}>
          <CardContent>
            <Typography color="text.secondary" fontWeight={800}>平均耗時</Typography>
            <Typography variant="h4" fontWeight={900}>{stats.avgDuration} ms</Typography>
          </CardContent>
        </Card>
      </Box>

      <Paper sx={{ p: 2, borderRadius: 1, border: '1px solid rgba(255,255,255,0.08)' }}>
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '220px 180px 1fr auto auto' }, gap: 1.5 }}>
          <TextField
            select
            label="模組"
            value={moduleFilter}
            onChange={(event) => { setModuleFilter(event.target.value); setPage(1); }}
          >
            {MODULE_OPTIONS.map(option => (
              <MenuItem key={option.value || 'all'} value={option.value}>{option.label}</MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="狀態"
            value={statusFilter}
            onChange={(event) => { setStatusFilter(event.target.value as '' | AuditStatus); setPage(1); }}
          >
            {STATUS_OPTIONS.map(option => (
              <MenuItem key={option.value || 'all'} value={option.value}>{option.label}</MenuItem>
            ))}
          </TextField>
          <TextField
            label="動作代碼"
            placeholder="例如 start、cancel、complete"
            value={actionFilter}
            onChange={(event) => setActionFilter(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') handleApplyFilters();
            }}
          />
          <Button variant="contained" color="secondary" startIcon={<ManageSearchIcon />} onClick={handleApplyFilters}>
            查詢
          </Button>
          <Button variant="outlined" startIcon={<RestartAltIcon />} onClick={handleResetFilters}>
            重設
          </Button>
        </Box>
      </Paper>

      <TableContainer component={Paper} sx={{ borderRadius: 1, border: '1px solid rgba(255,255,255,0.08)' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>時間</TableCell>
              <TableCell>模組</TableCell>
              <TableCell>動作</TableCell>
              <TableCell>狀態</TableCell>
              <TableCell>角色</TableCell>
              <TableCell>使用者</TableCell>
              <TableCell align="right">耗時</TableCell>
              <TableCell>方法</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading && (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 6 }}>
                  <CircularProgress size={28} />
                </TableCell>
              </TableRow>
            )}
            {!loading && logs.map(log => (
              <TableRow key={log.id} hover>
                <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDateTime(log.occurredAt)}</TableCell>
                <TableCell>{log.module}</TableCell>
                <TableCell>{log.action}</TableCell>
                <TableCell>
                  <Chip label={STATUS_LABEL[log.status]} color={STATUS_COLOR[log.status]} size="small" />
                </TableCell>
                <TableCell>{log.roleCode ?? '-'}</TableCell>
                <TableCell>
                  <Typography component="code" fontSize={13}>{renderUser(log)}</Typography>
                </TableCell>
                <TableCell align="right">{log.durationMs} ms</TableCell>
                <TableCell sx={{ maxWidth: 260 }}>
                  <Typography variant="body2" sx={{ overflowWrap: 'anywhere' }}>
                    {log.methodName}
                  </Typography>
                  {log.errorMessage && (
                    <Typography variant="caption" color="error.main" sx={{ overflowWrap: 'anywhere' }}>
                      {log.errorMessage}
                    </Typography>
                  )}
                </TableCell>
              </TableRow>
            ))}
            {!loading && logs.length === 0 && (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 6 }}>
                  尚無稽核紀錄
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Box sx={{ display: 'flex', justifyContent: 'center' }}>
        <Pagination
          count={totalPages}
          page={page}
          onChange={(_, value) => setPage(value)}
          color="secondary"
          size="large"
        />
      </Box>
    </Stack>
  );
};

export default AuditLogPage;
