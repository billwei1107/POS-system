/**
 * @file StockTakePage.tsx
 * @description 盤點管理頁 / Stock take management page
 * @description_en Manage inventory stock take sessions: start, submit counts, complete, cancel
 * @description_zh 管理盤點工作階段：開始、登記盤點數量、完成、取消
 */
import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Button, Table, TableHead, TableRow, TableCell, TableBody,
  Chip, Alert, TextField, Paper, CircularProgress, Collapse, LinearProgress,
} from '@mui/material';
import { stockTakeApi } from '../api/inventoryApi';
import type { StockTake, StockTakeStatus } from '../types';

const STORE_ID = import.meta.env.VITE_DEFAULT_STORE_ID as string;

const STATUS_LABEL: Record<StockTakeStatus, string> = {
  IN_PROGRESS: '進行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};

const STATUS_COLOR: Record<StockTakeStatus, 'warning' | 'success' | 'error'> = {
  IN_PROGRESS: 'warning',
  COMPLETED: 'success',
  CANCELLED: 'error',
};

// ========================================
// 盤點管理頁 / Stock take management page
// ========================================
const StockTakePage: React.FC = () => {
  const [takes, setTakes] = useState<StockTake[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [countInputs, setCountInputs] = useState<Record<string, string>>({});

  const loadTakes = async () => {
    try {
      setLoading(true);
      const res = await stockTakeApi.listByStore(STORE_ID);
      setTakes(res.data.data ?? []);
    } catch {
      setError('載入失敗');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadTakes(); }, []);

  const handleStart = async () => {
    try {
      await stockTakeApi.start(STORE_ID);
      await loadTakes();
      setSuccess('盤點已開始');
    } catch {
      setError('開始失敗（可能已有進行中的盤點）');
    }
  };

  const handleSubmitCount = async (takeId: string, itemId: string) => {
    const val = countInputs[`${takeId}_${itemId}`];
    if (!val) return;
    try {
      await stockTakeApi.submitCount(takeId, itemId, Number(val));
      await loadTakes();
    } catch {
      setError('登記失敗');
    }
  };

  const handleComplete = async (takeId: string) => {
    if (!window.confirm('確認完成盤點？完成後將依盤點數量調整庫存。')) return;
    try {
      await stockTakeApi.complete(takeId);
      await loadTakes();
      setSuccess('盤點已完成，庫存已調整');
    } catch {
      setError('完成失敗');
    }
  };

  const handleCancel = async (takeId: string) => {
    try {
      await stockTakeApi.cancel(takeId);
      await loadTakes();
    } catch {
      setError('取消失敗');
    }
  };

  // ========================================
  // 計算盤點完成進度 / Calculate counting progress
  // ========================================
  const calcProgress = (take: StockTake) => {
    if (take.items.length === 0) return 0;
    const counted = take.items.filter(i => i.countedQty !== null && i.countedQty !== undefined).length;
    return Math.round((counted / take.items.length) * 100);
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>;

  const inProgress = takes.find(t => t.status === 'IN_PROGRESS');

  return (
    <Box sx={{ p: 3 }}>
      {/* ======================================== */}
      {/* 頁面標題 / Page header */}
      {/* ======================================== */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h5" fontWeight="bold">盤點管理</Typography>
        <Button
          variant="contained"
          onClick={handleStart}
          disabled={!!inProgress}
        >
          {inProgress ? '盤點進行中' : '開始新盤點'}
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      {/* ======================================== */}
      {/* 盤點列表 / Stock take list */}
      {/* ======================================== */}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>開始時間</TableCell>
            <TableCell>狀態</TableCell>
            <TableCell>品項數</TableCell>
            <TableCell>盤點進度</TableCell>
            <TableCell>完成時間</TableCell>
            <TableCell>操作</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {takes.map(take => {
            const pct = calcProgress(take);
            return (
              <React.Fragment key={take.id}>
                <TableRow
                  hover
                  sx={{ cursor: 'pointer' }}
                  onClick={() => setExpandedId(expandedId === take.id ? null : take.id)}
                >
                  <TableCell>{new Date(take.startedAt).toLocaleString('zh-TW')}</TableCell>
                  <TableCell>
                    <Chip label={STATUS_LABEL[take.status]} color={STATUS_COLOR[take.status]} size="small" />
                  </TableCell>
                  <TableCell>{take.items.length}</TableCell>
                  <TableCell sx={{ minWidth: 120 }}>
                    <LinearProgress
                      variant="determinate"
                      value={pct}
                      color={pct >= 100 ? 'success' : 'primary'}
                      sx={{ height: 8, borderRadius: 4 }}
                    />
                    <Typography variant="caption">{pct}%</Typography>
                  </TableCell>
                  <TableCell>
                    {take.completedAt ? new Date(take.completedAt).toLocaleString('zh-TW') : '-'}
                  </TableCell>
                  <TableCell onClick={e => e.stopPropagation()}>
                    {take.status === 'IN_PROGRESS' && (
                      <Box sx={{ display: 'flex', gap: 1 }}>
                        <Button size="small" variant="contained" color="success"
                          onClick={() => handleComplete(take.id)}>完成盤點</Button>
                        <Button size="small" variant="outlined" color="error"
                          onClick={() => handleCancel(take.id)}>取消</Button>
                      </Box>
                    )}
                  </TableCell>
                </TableRow>

                {/* ======================================== */}
                {/* 展開品項盤點輸入 / Expandable item count input */}
                {/* ======================================== */}
                <TableRow>
                  <TableCell colSpan={6} sx={{ py: 0 }}>
                    <Collapse in={expandedId === take.id} timeout="auto">
                      <Paper sx={{ p: 2, m: 1, bgcolor: 'grey.50' }}>
                        <Typography variant="subtitle2" sx={{ mb: 1 }}>品項盤點明細</Typography>
                        <Table size="small">
                          <TableHead>
                            <TableRow>
                              <TableCell>商品 ID</TableCell>
                              <TableCell align="right">系統庫存</TableCell>
                              <TableCell align="right">盤點數量</TableCell>
                              <TableCell align="right">差異</TableCell>
                              {take.status === 'IN_PROGRESS' && <TableCell>登記</TableCell>}
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            {take.items.map(item => {
                              const key = `${take.id}_${item.itemId}`;
                              return (
                                <TableRow key={item.id}>
                                  <TableCell><code>{item.itemId.slice(0, 8)}...</code></TableCell>
                                  <TableCell align="right">{item.systemQty}</TableCell>
                                  <TableCell align="right">
                                    {item.countedQty !== null && item.countedQty !== undefined
                                      ? item.countedQty
                                      : <Chip label="未盤" size="small" />}
                                  </TableCell>
                                  <TableCell align="right">
                                    {item.difference !== null && item.difference !== undefined && (
                                      <Chip
                                        label={item.difference > 0 ? `+${item.difference}` : `${item.difference}`}
                                        color={item.difference === 0 ? 'default' : item.difference > 0 ? 'success' : 'error'}
                                        size="small"
                                      />
                                    )}
                                  </TableCell>
                                  {take.status === 'IN_PROGRESS' && (
                                    <TableCell>
                                      <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                                        <TextField
                                          size="small"
                                          type="number"
                                          placeholder="盤點數"
                                          value={countInputs[key] ?? ''}
                                          onChange={e => setCountInputs(prev => ({ ...prev, [key]: e.target.value }))}
                                          inputProps={{ min: 0, step: 0.001 }}
                                          sx={{ width: 100 }}
                                        />
                                        <Button
                                          size="small"
                                          variant="outlined"
                                          onClick={() => handleSubmitCount(take.id, item.itemId)}
                                        >
                                          登記
                                        </Button>
                                      </Box>
                                    </TableCell>
                                  )}
                                </TableRow>
                              );
                            })}
                          </TableBody>
                        </Table>
                      </Paper>
                    </Collapse>
                  </TableCell>
                </TableRow>
              </React.Fragment>
            );
          })}
          {takes.length === 0 && (
            <TableRow>
              <TableCell colSpan={6} align="center">尚無盤點記錄，點擊「開始新盤點」</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </Box>
  );
};

export default StockTakePage;
