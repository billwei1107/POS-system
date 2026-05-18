/**
 * @file StockOverviewPage.tsx
 * @description 庫存總覽頁 / Stock overview page
 * @description_en View store inventory levels, alerts, and entry points for receiving and stock takes
 * @description_zh 查看門店庫存水位、警示，並提供進貨驗收與盤點單入口
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
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { AssignmentTurnedIn, Inventory2, LocalShipping, Tune } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { getActivePosContext } from '../../pos-orders/posSession';
import { productApi } from '../../pos-products/api/productApi';
import type { ProductItem } from '../../pos-products/types';
import { alertApi, stockApi } from '../api/inventoryApi';
import type { AdjustStockPayload, StockAlert, StoreStock } from '../types';

const formatQty = (value: number) => Number(value || 0).toFixed(3).replace(/\.?0+$/, '');

const getStockChipProps = (stock: StoreStock) => {
  if (stock.quantity <= 0) return { label: '缺貨', color: 'error' as const };
  if (stock.reorderPoint > 0 && stock.quantity <= stock.reorderPoint) return { label: '低庫存', color: 'warning' as const };
  return { label: '充足', color: 'success' as const };
};

// ========================================
// 庫存總覽頁 / Stock overview page
// ========================================
const StockOverviewPage: React.FC = () => {
  const navigate = useNavigate();
  const [stocks, setStocks] = useState<StoreStock[]>([]);
  const [alerts, setAlerts] = useState<StockAlert[]>([]);
  const [products, setProducts] = useState<ProductItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [adjDialog, setAdjDialog] = useState(false);
  const posContext = useMemo(() => getActivePosContext(), []);
  const [adjForm, setAdjForm] = useState<AdjustStockPayload>({
    storeId: posContext.storeId,
    itemId: '',
    adjustQty: 0,
    operatedBy: posContext.employeeId,
    notes: '',
  });

  const productMap = useMemo(
    () => new Map(products.map(product => [product.id, product])),
    [products]
  );

  const sortedStocks = useMemo(
    () => [...stocks].sort((a, b) => {
      const aProduct = productMap.get(a.itemId);
      const bProduct = productMap.get(b.itemId);
      return (aProduct?.name ?? a.itemId).localeCompare(bProduct?.name ?? b.itemId, 'zh-Hant');
    }),
    [productMap, stocks]
  );

  const stats = useMemo(() => {
    const lowStock = stocks.filter(stock => stock.quantity > 0 && stock.reorderPoint > 0 && stock.quantity <= stock.reorderPoint).length;
    const outOfStock = stocks.filter(stock => stock.quantity <= 0).length;
    const totalQty = stocks.reduce((sum, stock) => sum + Number(stock.quantity || 0), 0);
    return { totalItems: stocks.length, lowStock, outOfStock, totalQty };
  }, [stocks]);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      const [stockRes, alertRes, productRes] = await Promise.all([
        stockApi.listByStore(posContext.storeId),
        alertApi.listUnacknowledged(posContext.storeId),
        productApi.getProducts({ page: 0, size: 500 }),
      ]);
      setStocks(stockRes.data ?? []);
      setAlerts(alertRes.data ?? []);
      setProducts(productRes.data?.content ?? []);
    } catch {
      setError('載入庫存資料失敗');
    } finally {
      setLoading(false);
    }
  }, [posContext.storeId]);

  useEffect(() => { loadData(); }, [loadData]);

  const getProductName = (itemId: string) => productMap.get(itemId)?.name ?? `商品 ${itemId.slice(0, 8)}`;
  const getProductSku = (itemId: string) => productMap.get(itemId)?.sku ?? itemId;

  const handleAdjust = async () => {
    if (!adjForm.itemId || adjForm.adjustQty === 0) {
      setError('請選擇商品並填寫非 0 的調整數量');
      return;
    }

    try {
      await stockApi.adjust(adjForm);
      setAdjDialog(false);
      setAdjForm({
        storeId: posContext.storeId,
        itemId: '',
        adjustQty: 0,
        operatedBy: posContext.employeeId,
        notes: '',
      });
      await loadData();
      setSuccess('庫存調整成功');
    } catch {
      setError('調整庫存失敗');
    }
  };

  const handleAcknowledge = async (alertId: string) => {
    try {
      await alertApi.acknowledge(alertId, posContext.employeeId);
      setAlerts(prev => prev.filter(alert => alert.id !== alertId));
    } catch {
      setError('確認警示失敗');
    }
  };

  const renderStockCard = (stock: StoreStock) => {
    const chip = getStockChipProps(stock);
    return (
      <Card key={stock.id} variant="outlined" sx={{ borderRadius: 3 }}>
        <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1 }}>
            <Box sx={{ minWidth: 0 }}>
              <Typography variant="body2" color="text.secondary" fontWeight={800}>{getProductSku(stock.itemId)}</Typography>
              <Typography variant="h6" fontWeight={900} sx={{ overflowWrap: 'anywhere' }}>{getProductName(stock.itemId)}</Typography>
            </Box>
            <Chip label={chip.label} color={chip.color} />
          </Box>
          <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 1 }}>
            <Box>
              <Typography variant="caption" color="text.secondary">庫存量</Typography>
              <Typography variant="h5" fontWeight={900}>{formatQty(stock.quantity)}</Typography>
            </Box>
            <Box>
              <Typography variant="caption" color="text.secondary">可用量</Typography>
              <Typography variant="h5" fontWeight={900}>{formatQty(stock.availableQuantity)}</Typography>
            </Box>
            <Box>
              <Typography variant="caption" color="text.secondary">預留量</Typography>
              <Typography fontWeight={800}>{formatQty(stock.reservedQuantity)}</Typography>
            </Box>
            <Box>
              <Typography variant="caption" color="text.secondary">補貨點</Typography>
              <Typography fontWeight={800}>{formatQty(stock.reorderPoint)}</Typography>
            </Box>
          </Box>
        </CardContent>
      </Card>
    );
  };

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>;
  }

  return (
    <Box sx={{ pb: { xs: 10, sm: 0 } }}>
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, justifyContent: 'space-between', gap: 2, mb: 3 }}>
        <Box>
          <Typography variant="h3" fontWeight={900} sx={{ fontSize: { xs: '2.1rem', md: '3rem' } }}>
            庫存總覽
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 0.75 }}>
            檢視庫存水位，進貨先驗收，定期用盤點單校正庫存。
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, alignSelf: { xs: 'stretch', md: 'center' } }}>
          <Button
            variant="contained"
            color="secondary"
            startIcon={<LocalShipping />}
            onClick={() => navigate('/admin/inventory/receiving')}
            sx={{ minHeight: 56, flex: { xs: 1, sm: 'initial' } }}
          >
            進貨驗收
          </Button>
          <Button
            variant="outlined"
            startIcon={<AssignmentTurnedIn />}
            onClick={() => navigate('/admin/inventory/stock-takes')}
            sx={{ minHeight: 56, flex: { xs: 1, sm: 'initial' } }}
          >
            盤點單
          </Button>
          <Button
            variant="outlined"
            startIcon={<Tune />}
            onClick={() => setAdjDialog(true)}
            sx={{ minHeight: 56, flex: { xs: '1 0 100%', sm: 'initial' } }}
          >
            手動調整
          </Button>
        </Box>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: 'repeat(2, minmax(0, 1fr))', md: 'repeat(4, minmax(0, 1fr))' }, gap: 1.5, mb: 2.5 }}>
        <Card variant="outlined" sx={{ borderRadius: 3 }}>
          <CardContent>
            <Typography color="text.secondary" variant="body2">庫存品項</Typography>
            <Typography variant="h4" fontWeight={900}>{stats.totalItems}</Typography>
          </CardContent>
        </Card>
        <Card variant="outlined" sx={{ borderRadius: 3 }}>
          <CardContent>
            <Typography color="text.secondary" variant="body2">總庫存量</Typography>
            <Typography variant="h4" fontWeight={900}>{formatQty(stats.totalQty)}</Typography>
          </CardContent>
        </Card>
        <Card variant="outlined" sx={{ borderRadius: 3 }}>
          <CardContent>
            <Typography color="text.secondary" variant="body2">低庫存</Typography>
            <Typography variant="h4" fontWeight={900} color={stats.lowStock > 0 ? 'warning.main' : 'text.primary'}>{stats.lowStock}</Typography>
          </CardContent>
        </Card>
        <Card variant="outlined" sx={{ borderRadius: 3 }}>
          <CardContent>
            <Typography color="text.secondary" variant="body2">缺貨</Typography>
            <Typography variant="h4" fontWeight={900} color={stats.outOfStock > 0 ? 'error.main' : 'text.primary'}>{stats.outOfStock}</Typography>
          </CardContent>
        </Card>
      </Box>

      {alerts.length > 0 && (
        <Paper sx={{ p: { xs: 2, md: 2.5 }, mb: 2.5, borderRadius: 4, border: '1px solid rgba(255, 193, 7, 0.28)' }}>
          <Typography variant="h6" fontWeight={900} sx={{ mb: 1.5 }}>待處理庫存警示</Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            {alerts.map(alert => (
              <Box
                key={alert.id}
                sx={{
                  display: 'flex',
                  flexDirection: { xs: 'column', sm: 'row' },
                  alignItems: { xs: 'stretch', sm: 'center' },
                  justifyContent: 'space-between',
                  gap: 1,
                  p: 1.5,
                  borderRadius: 3,
                  bgcolor: 'rgba(255,255,255,0.04)',
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                  <Chip
                    label={alert.alertType === 'OUT_OF_STOCK' ? '缺貨' : alert.alertType === 'LOW_STOCK' ? '低庫存' : '即將到期'}
                    color={alert.alertType === 'OUT_OF_STOCK' ? 'error' : 'warning'}
                  />
                  <Typography fontWeight={800}>{getProductName(alert.itemId)}</Typography>
                  <Typography color="text.secondary">目前 {formatQty(Number(alert.currentQty))}</Typography>
                </Box>
                <Button variant="outlined" onClick={() => handleAcknowledge(alert.id)}>確認處理</Button>
              </Box>
            ))}
          </Box>
        </Paper>
      )}

      <Box sx={{ display: { xs: 'grid', md: 'none' }, gridTemplateColumns: '1fr', gap: 1.5 }}>
        {sortedStocks.length === 0 ? (
          <Card variant="outlined" sx={{ borderRadius: 3 }}>
            <CardContent>
              <Typography color="text.secondary">尚無庫存資料，請先從進貨驗收建立庫存。</Typography>
            </CardContent>
          </Card>
        ) : sortedStocks.map(renderStockCard)}
      </Box>

      <Paper sx={{ display: { xs: 'none', md: 'block' }, borderRadius: 4, overflow: 'hidden' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>商品</TableCell>
              <TableCell>SKU</TableCell>
              <TableCell align="right">庫存量</TableCell>
              <TableCell align="right">預留量</TableCell>
              <TableCell align="right">可用量</TableCell>
              <TableCell align="right">補貨點</TableCell>
              <TableCell>狀態</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {sortedStocks.map(stock => {
              const chip = getStockChipProps(stock);
              return (
                <TableRow key={stock.id} hover>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Inventory2 color="secondary" fontSize="small" />
                      <Typography fontWeight={900}>{getProductName(stock.itemId)}</Typography>
                    </Box>
                  </TableCell>
                  <TableCell>{getProductSku(stock.itemId)}</TableCell>
                  <TableCell align="right">{formatQty(stock.quantity)}</TableCell>
                  <TableCell align="right">{formatQty(stock.reservedQuantity)}</TableCell>
                  <TableCell align="right">{formatQty(stock.availableQuantity)}</TableCell>
                  <TableCell align="right">{formatQty(stock.reorderPoint)}</TableCell>
                  <TableCell><Chip label={chip.label} color={chip.color} size="small" /></TableCell>
                </TableRow>
              );
            })}
            {sortedStocks.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} align="center">尚無庫存資料，請先從進貨驗收建立庫存。</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>

      <Dialog open={adjDialog} onClose={() => setAdjDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>手動調整庫存</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField
            select
            label="商品"
            value={adjForm.itemId}
            onChange={event => setAdjForm({ ...adjForm, itemId: event.target.value })}
            required
          >
            {products.filter(product => product.active && product.trackInventory).map(product => (
              <MenuItem key={product.id} value={product.id}>{product.name}（{product.sku}）</MenuItem>
            ))}
          </TextField>
          <TextField
            type="number"
            label="調整數量（正數增加、負數減少）"
            value={adjForm.adjustQty}
            onChange={event => setAdjForm({ ...adjForm, adjustQty: Number(event.target.value) })}
            inputProps={{ step: 0.001 }}
          />
          <TextField
            label="備註"
            value={adjForm.notes}
            onChange={event => setAdjForm({ ...adjForm, notes: event.target.value })}
            multiline
            minRows={2}
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
