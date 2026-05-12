/**
 * @file ReceivingPage.tsx
 * @description 進貨驗收頁 / Receiving page
 * @description_en Count inbound goods and post verified receiving quantities into inventory
 * @description_zh 讓門店在進貨時先點收商品，再依實際驗收數量入庫
 */
import React, { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  IconButton,
  MenuItem,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import { Add, DeleteOutline, Inventory2, Save } from '@mui/icons-material';
import { DEFAULT_EMPLOYEE_ID, DEFAULT_STORE_ID } from '../../pos-orders/config';
import { productApi } from '../../pos-products/api/productApi';
import type { ProductItem } from '../../pos-products/types';
import { stockApi } from '../api/inventoryApi';
import type { StoreStock } from '../types';

interface ReceivingRow {
  rowId: string;
  itemId: string;
  expectedQty: string;
  receivedQty: string;
}

const createRow = (): ReceivingRow => ({
  rowId: crypto.randomUUID(),
  itemId: '',
  expectedQty: '',
  receivedQty: '',
});

const toNumber = (value: string) => Number(value || 0);

const formatQty = (value: number) => Number(value || 0).toFixed(3).replace(/\.?0+$/, '');

// ========================================
// 進貨驗收頁 / Receiving page
// ========================================
const ReceivingPage: React.FC = () => {
  const [products, setProducts] = useState<ProductItem[]>([]);
  const [stocks, setStocks] = useState<StoreStock[]>([]);
  const [rows, setRows] = useState<ReceivingRow[]>([createRow()]);
  const [documentNo, setDocumentNo] = useState('');
  const [supplier, setSupplier] = useState('');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const productMap = useMemo(
    () => new Map(products.map(product => [product.id, product])),
    [products]
  );

  const stockMap = useMemo(
    () => new Map(stocks.map(stock => [stock.itemId, stock])),
    [stocks]
  );

  const receivingProducts = useMemo(
    () => products.filter(product => product.active && product.trackInventory),
    [products]
  );

  const summary = useMemo(() => {
    const validRows = rows.filter(row => row.itemId && toNumber(row.receivedQty) > 0);
    const expectedTotal = rows.reduce((sum, row) => sum + toNumber(row.expectedQty), 0);
    const receivedTotal = rows.reduce((sum, row) => sum + toNumber(row.receivedQty), 0);
    const diffRows = rows.filter(row => row.itemId && toNumber(row.expectedQty) !== toNumber(row.receivedQty));

    return {
      itemCount: validRows.length,
      expectedTotal,
      receivedTotal,
      diffCount: diffRows.length,
    };
  }, [rows]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [productRes, stockRes] = await Promise.all([
        productApi.getProducts({ page: 0, size: 500 }),
        stockApi.listByStore(DEFAULT_STORE_ID),
      ]);
      setProducts(productRes.data?.content ?? []);
      setStocks(stockRes.data ?? []);
    } catch {
      setError('載入商品或庫存失敗');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const updateRow = (rowId: string, patch: Partial<ReceivingRow>) => {
    setRows(prev => prev.map(row => (row.rowId === rowId ? { ...row, ...patch } : row)));
  };

  const removeRow = (rowId: string) => {
    setRows(prev => (prev.length <= 1 ? prev : prev.filter(row => row.rowId !== rowId)));
  };

  const resetForm = () => {
    setRows([createRow()]);
    setDocumentNo('');
    setSupplier('');
    setNotes('');
  };

  const buildNotes = () => {
    const lines = [
      documentNo ? `單號：${documentNo}` : '',
      supplier ? `供應商：${supplier}` : '',
      notes ? `備註：${notes}` : '',
      ...rows
        .filter(row => row.itemId)
        .map(row => {
          const product = productMap.get(row.itemId);
          const expected = toNumber(row.expectedQty);
          const received = toNumber(row.receivedQty);
          const diff = received - expected;
          return `${product?.sku ?? row.itemId} ${product?.name ?? ''} 預計 ${formatQty(expected)} / 實收 ${formatQty(received)} / 差異 ${formatQty(diff)}`;
        }),
    ];
    return lines.filter(Boolean).join('\n');
  };

  const handleSubmit = async () => {
    const validRows = rows.filter(row => row.itemId && toNumber(row.receivedQty) > 0);
    if (validRows.length === 0) {
      setError('請至少選擇一個商品並填寫實收數量');
      return;
    }

    try {
      setSubmitting(true);
      setError('');
      await stockApi.receive({
        storeId: DEFAULT_STORE_ID,
        operatedBy: DEFAULT_EMPLOYEE_ID,
        notes: buildNotes(),
        items: validRows.map(row => ({ itemId: row.itemId, receivedQty: toNumber(row.receivedQty) })),
      });
      setSuccess('進貨驗收完成，庫存已依實收數量入庫');
      resetForm();
      await loadData();
    } catch {
      setError('進貨驗收入庫失敗，請確認商品與數量後再試一次');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>;
  }

  return (
    <Box sx={{ pb: { xs: 10, sm: 0 } }}>
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, justifyContent: 'space-between', gap: 2, mb: 3 }}>
        <Box>
          <Typography variant="h3" fontWeight={900} sx={{ fontSize: { xs: '2.1rem', md: '3rem' } }}>
            進貨驗收
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 0.75 }}>
            先核對進貨單，再以實際點收數量入庫。
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="secondary"
          startIcon={<Save />}
          onClick={handleSubmit}
          disabled={submitting}
          sx={{ minHeight: 56, px: 3, alignSelf: { xs: 'stretch', md: 'center' } }}
        >
          完成驗收入庫
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', lg: '1.15fr 0.85fr' }, gap: 2.5 }}>
        <Paper sx={{ p: { xs: 2, md: 3 }, borderRadius: 4 }}>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(2, minmax(0, 1fr))' }, gap: 2, mb: 2.5 }}>
            <TextField
              label="進貨單號"
              value={documentNo}
              onChange={event => setDocumentNo(event.target.value)}
              placeholder="例如 PO-20260512-001"
            />
            <TextField
              label="供應商"
              value={supplier}
              onChange={event => setSupplier(event.target.value)}
              placeholder="例如 信義烘豆廠"
            />
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1, mb: 1.5 }}>
            <Typography variant="h6" fontWeight={900}>點收品項</Typography>
            <Button startIcon={<Add />} onClick={() => setRows(prev => [...prev, createRow()])}>
              新增品項
            </Button>
          </Box>

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            {rows.map((row, index) => {
              const product = productMap.get(row.itemId);
              const stock = stockMap.get(row.itemId);
              const expectedQty = toNumber(row.expectedQty);
              const receivedQty = toNumber(row.receivedQty);
              const diff = row.itemId ? receivedQty - expectedQty : 0;

              return (
                <Card key={row.rowId} variant="outlined" sx={{ borderRadius: 3 }}>
                  <CardContent sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'minmax(260px, 1.5fr) repeat(2, minmax(120px, 0.7fr)) auto' }, gap: 1.25, alignItems: 'center' }}>
                    <TextField
                      select
                      label={`商品 #${index + 1}`}
                      value={row.itemId}
                      onChange={event => updateRow(row.rowId, { itemId: event.target.value })}
                      fullWidth
                    >
                      {receivingProducts.map(item => (
                        <MenuItem key={item.id} value={item.id}>
                          {item.name}（{item.sku}）
                        </MenuItem>
                      ))}
                    </TextField>
                    <TextField
                      type="number"
                      label="進貨單數量"
                      value={row.expectedQty}
                      onChange={event => updateRow(row.rowId, { expectedQty: event.target.value })}
                      inputProps={{ min: 0, step: 0.001 }}
                    />
                    <TextField
                      type="number"
                      label="實收數量"
                      value={row.receivedQty}
                      onChange={event => updateRow(row.rowId, { receivedQty: event.target.value })}
                      inputProps={{ min: 0.001, step: 0.001 }}
                      required
                    />
                    <IconButton aria-label="移除品項" onClick={() => removeRow(row.rowId)} disabled={rows.length <= 1}>
                      <DeleteOutline />
                    </IconButton>

                    <Box sx={{ gridColumn: { xs: '1', md: '1 / -1' }, display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                      <Chip label={`目前庫存 ${formatQty(Number(stock?.quantity ?? 0))}`} size="small" />
                      {product && <Chip label={product.unit} size="small" variant="outlined" />}
                      {row.itemId && (
                        <Chip
                          label={diff === 0 ? '數量相符' : `差異 ${diff > 0 ? '+' : ''}${formatQty(diff)}`}
                          color={diff === 0 ? 'success' : 'warning'}
                          size="small"
                        />
                      )}
                    </Box>
                  </CardContent>
                </Card>
              );
            })}
          </Box>

          <TextField
            label="驗收備註"
            value={notes}
            onChange={event => setNotes(event.target.value)}
            multiline
            minRows={3}
            fullWidth
            sx={{ mt: 2 }}
          />
        </Paper>

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Paper sx={{ p: 3, borderRadius: 4 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
              <Inventory2 color="secondary" />
              <Typography variant="h6" fontWeight={900}>驗收摘要</Typography>
            </Box>
            <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 1.5 }}>
              <Card variant="outlined" sx={{ borderRadius: 3 }}>
                <CardContent>
                  <Typography color="text.secondary" variant="body2">品項數</Typography>
                  <Typography variant="h4" fontWeight={900}>{summary.itemCount}</Typography>
                </CardContent>
              </Card>
              <Card variant="outlined" sx={{ borderRadius: 3 }}>
                <CardContent>
                  <Typography color="text.secondary" variant="body2">差異品項</Typography>
                  <Typography variant="h4" fontWeight={900}>{summary.diffCount}</Typography>
                </CardContent>
              </Card>
              <Card variant="outlined" sx={{ borderRadius: 3 }}>
                <CardContent>
                  <Typography color="text.secondary" variant="body2">進貨單總數</Typography>
                  <Typography variant="h4" fontWeight={900}>{formatQty(summary.expectedTotal)}</Typography>
                </CardContent>
              </Card>
              <Card variant="outlined" sx={{ borderRadius: 3 }}>
                <CardContent>
                  <Typography color="text.secondary" variant="body2">實收入庫</Typography>
                  <Typography variant="h4" fontWeight={900} color="secondary">{formatQty(summary.receivedTotal)}</Typography>
                </CardContent>
              </Card>
            </Box>
          </Paper>

          <Alert severity="info" sx={{ borderRadius: 3 }}>
            入庫數量以「實收數量」為準；進貨單數量只用來核對差異，方便之後追供應商短溢交。
          </Alert>
        </Box>
      </Box>
    </Box>
  );
};

export default ReceivingPage;
