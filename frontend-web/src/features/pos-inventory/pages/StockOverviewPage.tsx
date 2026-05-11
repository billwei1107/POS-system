/**
 * @file StockOverviewPage.tsx
 * @description 庫存總覽頁 / Stock overview page
 * @description_en View and manage store inventory levels with alert badges and manual adjustment
 * @description_zh 查看門店庫存水位、未確認警示，並支援手動調整庫存
 */
import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Button, Table, TableHead, TableRow, TableCell, TableBody,
  Chip, Alert, TextField, Paper, Dialog, DialogTitle, DialogContent,
  DialogActions, CircularProgress, Badge, Card, CardContent,
} from '@mui/material';
import { DEFAULT_STORE_ID } from '../../pos-orders/config';
import { alertApi, stockApi } from '../api/inventoryApi';
import type { AdjustStockPayload, StockAlert, StoreStock } from '../types';

// ========================================
// 庫存狀態 Chip 配色 / Stock status chip color
// ========================================
const getStockChipProps = (stock: StoreStock) => {
  if (stock.quantity <= 0) return { label: '缺貨', color: 'error' as const };
  if (stock.reorderPoint > 0 && stock.quantity <= stock.reorderPoint) return { label: '低庫存', color: 'warning' as const };
  return { label: '充足', color: 'success' as const };
};

// ========================================
// 庫存總覽頁 / Stock overview page
// ========================================
const StockOverviewPage: React.FC = () => {
  const [stocks, setStocks] = useState<StoreStock[]>([]);
  const [alerts, setAlerts] = useState<StockAlert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // ========================================
  // 調整對話框狀態 / Adjustment dialog state
  // ========================================
  const [adjDialog, setAdjDialog] = useState(false);
  const [adjForm, setAdjForm] = useState<AdjustStockPayload>({
    storeId: DEFAULT_STORE_ID, itemId: '', adjustQty: 0, notes: '',
  });

  const loadData = async () => {
    try {
      setLoading(true);
      const [stockRes, alertRes] = await Promise.all([
        stockApi.listByStore(DEFAULT_STORE_ID),
        alertApi.listUnacknowledged(DEFAULT_STORE_ID),
      ]);
      setStocks(stockRes.data ?? []);
      setAlerts(alertRes.data ?? []);
    } catch {
      setError('載入失敗');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const handleAdjust = async () => {
    try {
      await stockApi.adjust(adjForm);
      setAdjDialog(false);
      setAdjForm({ storeId: DEFAULT_STORE_ID, itemId: '', adjustQty: 0, notes: '' });
      await loadData();
      setSuccess('庫存調整成功');
    } catch {
      setError('調整失敗');
    }
  };

  const handleAcknowledge = async (alertId: string) => {
    try {
      await alertApi.acknowledge(alertId);
      setAlerts(prev => prev.filter(a => a.id !== alertId));
    } catch {
      setError('確認失敗');
    }
  };

  const renderStockCards = () => (
    <Box sx={{ display: { xs: 'flex', md: 'none' }, flexDirection: 'column', gap: 1.25 }}>
      {stocks.length === 0 ? (
        <Card variant="outlined" sx={{ borderRadius: 2 }}>
          <CardContent>
            <Typography variant="body2" color="text.secondary">尚無庫存資料</Typography>
          </CardContent>
        </Card>
      ) : stocks.map((stock) => {
        const chip = getStockChipProps(stock);

        return (
          <Card key={stock.id} variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.25 }}>
              <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 1 }}>
                <Box sx={{ minWidth: 0 }}>
                  <Typography variant="caption" color="text.secondary">商品 ID</Typography>
                  <Typography variant="body2" sx={{ fontFamily: 'monospace', fontWeight: 800, overflowWrap: 'anywhere' }}>
                    {stock.itemId}
                  </Typography>
                </Box>
                <Chip label={chip.label} color={chip.color} size="small" />
              </Box>

              <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 1 }}>
                <Box>
                  <Typography variant="caption" color="text.secondary">庫存量</Typography>
                  <Typography variant="body2" fontWeight={800}>{Number(stock.quantity).toFixed(2)}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">可用量</Typography>
                  <Typography variant="body2" fontWeight={800}>{Number(stock.availableQuantity).toFixed(2)}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">預留量</Typography>
                  <Typography variant="body2">{Number(stock.reservedQuantity).toFixed(2)}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">補貨點</Typography>
                  <Typography variant="body2">{Number(stock.reorderPoint).toFixed(2)}</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        );
      })}
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
        <Typography variant="h5" fontWeight="bold">庫存總覽</Typography>
        <Badge badgeContent={alerts.length} color="error">
          <Button variant="contained" onClick={() => setAdjDialog(true)}>手動調整庫存</Button>
        </Badge>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      {/* ======================================== */}
      {/* 未確認警示區 / Unacknowledged alerts */}
      {/* ======================================== */}
      {alerts.length > 0 && (
        <Paper sx={{ p: 2, mb: 3, bgcolor: 'warning.light' }}>
          <Typography variant="subtitle1" fontWeight="bold" sx={{ mb: 1 }}>
            庫存警示（{alerts.length} 筆待確認）
          </Typography>
          {alerts.map(alert => (
            <Box
              key={alert.id}
              sx={{
                display: 'flex',
                flexDirection: { xs: 'column', sm: 'row' },
                alignItems: { xs: 'stretch', sm: 'center' },
                gap: 1,
                mb: 1,
              }}
            >
              <Chip
                label={alert.alertType === 'OUT_OF_STOCK' ? '缺貨' : alert.alertType === 'LOW_STOCK' ? '低庫存' : '即將到期'}
                color={alert.alertType === 'OUT_OF_STOCK' ? 'error' : 'warning'}
                size="small"
              />
              <Typography variant="body2">
                商品 {alert.itemId.slice(0, 8)}... 目前庫存 {alert.currentQty}
              </Typography>
              <Button size="small" variant="outlined" onClick={() => handleAcknowledge(alert.id)}>
                確認
              </Button>
            </Box>
          ))}
        </Paper>
      )}

      {/* ======================================== */}
      {/* 庫存列表 / Stock list */}
      {/* ======================================== */}
      {renderStockCards()}

      <Box sx={{ display: { xs: 'none', md: 'block' } }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>商品 ID</TableCell>
              <TableCell align="right">庫存量</TableCell>
              <TableCell align="right">預留量</TableCell>
              <TableCell align="right">可用量</TableCell>
              <TableCell align="right">補貨點</TableCell>
              <TableCell>狀態</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {stocks.map(s => {
              const chip = getStockChipProps(s);
              return (
                <TableRow key={s.id}>
                  <TableCell><code>{s.itemId.slice(0, 8)}...</code></TableCell>
                  <TableCell align="right">{Number(s.quantity).toFixed(2)}</TableCell>
                  <TableCell align="right">{Number(s.reservedQuantity).toFixed(2)}</TableCell>
                  <TableCell align="right">{Number(s.availableQuantity).toFixed(2)}</TableCell>
                  <TableCell align="right">{Number(s.reorderPoint).toFixed(2)}</TableCell>
                  <TableCell><Chip label={chip.label} color={chip.color} size="small" /></TableCell>
                </TableRow>
              );
            })}
            {stocks.length === 0 && (
              <TableRow>
                <TableCell colSpan={6} align="center">尚無庫存資料</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Box>

      {/* ======================================== */}
      {/* 手動調整對話框 / Adjustment dialog */}
      {/* ======================================== */}
      <Dialog open={adjDialog} onClose={() => setAdjDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>手動調整庫存</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField
            label="商品 ID (UUID)"
            value={adjForm.itemId}
            onChange={e => setAdjForm({ ...adjForm, itemId: e.target.value })}
            required
          />
          <TextField
            type="number"
            label="調整數量（正數增加、負數減少）"
            value={adjForm.adjustQty}
            onChange={e => setAdjForm({ ...adjForm, adjustQty: Number(e.target.value) })}
            inputProps={{ step: 0.001 }}
          />
          <TextField
            label="備註"
            value={adjForm.notes}
            onChange={e => setAdjForm({ ...adjForm, notes: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAdjDialog(false)}>取消</Button>
          <Button variant="contained" onClick={handleAdjust}>確認調整</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default StockOverviewPage;
