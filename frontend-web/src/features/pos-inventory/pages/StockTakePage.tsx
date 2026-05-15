/**
 * @file StockTakePage.tsx
 * @description 盤點單頁 / Stock take page
 * @description_en Manage stock take documents and count inventory items with variance tracking
 * @description_zh 管理盤點單、登記實盤數量，並追蹤帳面與實盤差異
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
  IconButton,
  LinearProgress,
  MenuItem,
  Pagination,
  Paper,
  Select,
  TextField,
  Typography,
} from '@mui/material';
import type { SelectChangeEvent } from '@mui/material/Select';
import { ArrowBack, AssignmentTurnedIn, CalendarMonth, CheckCircle, ChevronLeft, ChevronRight, Close, PlaylistAddCheck, RestartAlt } from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { formatDateTime } from '@shared/utils';
import { getActivePosContext } from '../../pos-orders/posSession';
import { productApi } from '../../pos-products/api/productApi';
import type { ProductItem } from '../../pos-products/types';
import { stockTakeApi } from '../api/inventoryApi';
import type { StockTake, StockTakeItem, StockTakeStatus } from '../types';

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

const STOCK_TAKE_PAGE_SIZE = 5;
const WEEKDAY_LABELS = ['日', '一', '二', '三', '四', '五', '六'];
const HOUR_OPTIONS = Array.from({ length: 24 }, (_, index) => String(index).padStart(2, '0'));
const MINUTE_OPTIONS = Array.from({ length: 60 }, (_, index) => String(index).padStart(2, '0'));
const MONTH_OPTIONS = Array.from({ length: 12 }, (_, index) => index + 1);
const STOCK_TAKE_SELECTED_BG = '#ff7a1a';

const pickerChoiceSx = (selected: boolean) => ({
  minHeight: 38,
  borderRadius: 1.5,
  fontWeight: 900,
  borderColor: selected ? STOCK_TAKE_SELECTED_BG : 'rgba(255,255,255,0.16)',
  bgcolor: selected ? STOCK_TAKE_SELECTED_BG : 'rgba(255,255,255,0.035)',
  color: selected ? '#ffffff' : 'rgba(255,255,255,0.74)',
  '&:hover': {
    borderColor: selected ? STOCK_TAKE_SELECTED_BG : 'rgba(255,255,255,0.34)',
    bgcolor: selected ? '#ff8a2a' : 'rgba(255,255,255,0.08)',
  },
});

const formatQty = (value: number) => Number(value || 0).toFixed(3).replace(/\.?0+$/, '');

const getItemKey = (takeId: string, itemId: string) => `${takeId}_${itemId}`;

const getStockTakeDateOption = (take: StockTake) => formatDateTime(take.startedAt).split(' ')[0] ?? '';

const getStockTakeTimeOption = (take: StockTake) => formatDateTime(take.startedAt).split(' ')[1] ?? '';

const getStockTakeHourOption = (take: StockTake) => getStockTakeTimeOption(take).split(':')[0] ?? '';

const getStockTakeMinuteOption = (take: StockTake) => getStockTakeTimeOption(take).split(':')[1] ?? '';

const parseStockTakeDate = (value: string) => {
  const [year, month, day] = value.split('/').map(Number);
  return new Date(year, (month || 1) - 1, day || 1);
};

const formatCalendarDate = (date: Date) => [
  date.getFullYear(),
  String(date.getMonth() + 1).padStart(2, '0'),
  String(date.getDate()).padStart(2, '0'),
].join('/');

const getStockTakeFilterTimeLabel = (hour: string, minute: string) => {
  if (!hour && !minute) return '全部時間';
  if (hour && minute) return `${hour}:${minute}`;
  if (hour) return `${hour}:全部分鐘`;
  return `全部小時:${minute}`;
};

const getCalendarMonthDays = (monthDate: Date) => {
  const year = monthDate.getFullYear();
  const month = monthDate.getMonth();
  const firstDay = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  return [
    ...Array.from({ length: firstDay }, () => null),
    ...Array.from({ length: daysInMonth }, (_, index) => new Date(year, month, index + 1)),
  ];
};

const calcMetrics = (take: StockTake) => {
  const total = take.items.length;
  const counted = take.items.filter(item => item.countedQty !== null && item.countedQty !== undefined).length;
  const variance = take.items.filter(item => Number(item.difference ?? 0) !== 0).length;
  const uncounted = total - counted;
  const progress = total === 0 ? 0 : Math.round((counted / total) * 100);
  return { total, counted, uncounted, variance, progress };
};

const getApiErrorMessage = (error: unknown, fallback: string) => {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    return response?.data?.message ?? fallback;
  }
  return fallback;
};

// ========================================
// 盤點單頁 / Stock take page
// ========================================
const StockTakePage: React.FC = () => {
  const navigate = useNavigate();
  const { stockTakeId } = useParams<{ stockTakeId?: string }>();
  const [takes, setTakes] = useState<StockTake[]>([]);
  const [products, setProducts] = useState<ProductItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selectedTakeId, setSelectedTakeId] = useState<string | null>(null);
  const [selectedStockTakeDate, setSelectedStockTakeDate] = useState('');
  const [selectedStockTakeHour, setSelectedStockTakeHour] = useState('');
  const [selectedStockTakeMinute, setSelectedStockTakeMinute] = useState('');
  const [calendarMonth, setCalendarMonth] = useState(() => new Date());
  const [filterPickerOpen, setFilterPickerOpen] = useState(false);
  const [stockTakePage, setStockTakePage] = useState(1);
  const [countInputs, setCountInputs] = useState<Record<string, string>>({});
  const [savingKeys, setSavingKeys] = useState<Record<string, boolean>>({});
  const [savingComplete, setSavingComplete] = useState(false);
  const isDetailMode = Boolean(stockTakeId);
  const posContext = useMemo(() => getActivePosContext(), []);

  const productMap = useMemo(
    () => new Map(products.map(product => [product.id, product])),
    [products]
  );

  const sortedTakes = useMemo(
    () => [...takes].sort((a, b) => new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime()),
    [takes]
  );

  const stockTakeDateOptions = useMemo(
    () => [...new Set(sortedTakes.map(getStockTakeDateOption).filter(Boolean))],
    [sortedTakes]
  );

  const stockTakeDateSet = useMemo(
    () => new Set(stockTakeDateOptions),
    [stockTakeDateOptions]
  );

  const calendarYearOptions = useMemo(() => {
    const baseYear = calendarMonth.getFullYear();
    const years = new Set([
      baseYear - 2,
      baseYear - 1,
      baseYear,
      baseYear + 1,
      ...stockTakeDateOptions.map(option => parseStockTakeDate(option).getFullYear()),
    ]);
    return [...years].sort((a, b) => a - b);
  }, [calendarMonth, stockTakeDateOptions]);

  const calendarDays = useMemo(
    () => getCalendarMonthDays(calendarMonth),
    [calendarMonth]
  );

  const filteredTakes = useMemo(() => sortedTakes.filter(take => {
    const dateMatched = !selectedStockTakeDate || getStockTakeDateOption(take) === selectedStockTakeDate;
    const hourMatched = !selectedStockTakeHour || getStockTakeHourOption(take) === selectedStockTakeHour;
    const minuteMatched = !selectedStockTakeMinute || getStockTakeMinuteOption(take) === selectedStockTakeMinute;
    return dateMatched && hourMatched && minuteMatched;
  }), [selectedStockTakeDate, selectedStockTakeHour, selectedStockTakeMinute, sortedTakes]);

  const stockTakePageCount = Math.max(1, Math.ceil(filteredTakes.length / STOCK_TAKE_PAGE_SIZE));
  const currentStockTakePage = Math.min(stockTakePage, stockTakePageCount);
  const pagedTakes = filteredTakes.slice(
    (currentStockTakePage - 1) * STOCK_TAKE_PAGE_SIZE,
    currentStockTakePage * STOCK_TAKE_PAGE_SIZE
  );
  const pageStart = filteredTakes.length === 0 ? 0 : (currentStockTakePage - 1) * STOCK_TAKE_PAGE_SIZE + 1;
  const pageEnd = Math.min(currentStockTakePage * STOCK_TAKE_PAGE_SIZE, filteredTakes.length);
  const hasStockTakeFilter = Boolean(selectedStockTakeDate || selectedStockTakeHour || selectedStockTakeMinute);
  const stockTakeFilterTimeLabel = getStockTakeFilterTimeLabel(selectedStockTakeHour, selectedStockTakeMinute);

  const inProgress = sortedTakes.find(take => take.status === 'IN_PROGRESS');
  const detailTakeId = stockTakeId ?? selectedTakeId;
  const activeTake = detailTakeId ? sortedTakes.find(take => take.id === detailTakeId) ?? null : null;

  const getProductName = useCallback(
    (itemId: string) => productMap.get(itemId)?.name ?? `商品 ${itemId.slice(0, 8)}`,
    [productMap]
  );
  const getProductSku = useCallback(
    (itemId: string) => productMap.get(itemId)?.sku ?? itemId,
    [productMap]
  );

  const getDraftCountValue = (takeId: string, itemId: string) => {
    const key = getItemKey(takeId, itemId);
    const domValue = document.querySelector<HTMLInputElement>(`input[data-stock-count-key="${key}"]`)?.value;
    return domValue ?? countInputs[key];
  };

  const getPendingCountLines = (take: StockTake) => take.items
    .map(item => {
      const rawValue = getDraftCountValue(take.id, item.itemId)?.trim();
      const countedQty = rawValue === undefined || rawValue === '' ? Number.NaN : Number(rawValue);
      const previousQty = item.countedQty === null || item.countedQty === undefined
        ? Number.NaN
        : Number(item.countedQty);
      return { item, rawValue, countedQty, previousQty };
    })
    .filter(line => line.rawValue !== undefined && line.rawValue !== ''
      && Number.isFinite(line.countedQty)
      && line.countedQty >= 0
      && (Number.isNaN(line.previousQty) || line.previousQty !== line.countedQty));

  const getInvalidCountLines = (take: StockTake) => take.items.filter(item => {
    const rawValue = getDraftCountValue(take.id, item.itemId)?.trim();
    if (rawValue === undefined || rawValue === '') return false;
    const countedQty = Number(rawValue);
    return !Number.isFinite(countedQty) || countedQty < 0;
  });

  const getEffectiveUncountedCount = (take: StockTake) => take.items.filter(item => {
    const rawValue = getDraftCountValue(take.id, item.itemId)?.trim();
    return (item.countedQty === null || item.countedQty === undefined)
      && (rawValue === undefined || rawValue === '');
  }).length;

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      const [takeRes, productRes] = await Promise.all([
        stockTakeApi.listByStore(posContext.storeId),
        productApi.getProducts({ page: 0, size: 500 }),
      ]);
      const nextTakes = takeRes.data ?? [];
      setTakes(nextTakes);
      setProducts(productRes.data?.content ?? []);
      setSelectedTakeId(prev => prev && nextTakes.some(take => take.id === prev) ? prev : null);
      setCountInputs(prev => {
        const next = { ...prev };
        nextTakes.forEach(take => {
          take.items.forEach(item => {
            if (item.countedQty !== null && item.countedQty !== undefined) {
              const key = getItemKey(take.id, item.itemId);
              next[key] = next[key] ?? String(item.countedQty);
            }
          });
        });
        return next;
      });
    } catch (err) {
      setError(getApiErrorMessage(err, '載入盤點單失敗'));
    } finally {
      setLoading(false);
    }
  }, [posContext.storeId]);

  useEffect(() => { loadData(); }, [loadData]);

  useEffect(() => {
    setStockTakePage(1);
    setSelectedTakeId(null);
  }, [selectedStockTakeDate, selectedStockTakeHour, selectedStockTakeMinute]);

  useEffect(() => {
    if (stockTakePage > stockTakePageCount) {
      setStockTakePage(stockTakePageCount);
    }
  }, [stockTakePage, stockTakePageCount]);

  useEffect(() => {
    if (selectedStockTakeDate) {
      setCalendarMonth(parseStockTakeDate(selectedStockTakeDate));
      return;
    }
    if (stockTakeDateOptions.length > 0) {
      setCalendarMonth(parseStockTakeDate(stockTakeDateOptions[0]));
    }
  }, [selectedStockTakeDate, stockTakeDateOptions]);

  const handleStart = async () => {
    try {
      const res = await stockTakeApi.start(posContext.storeId, posContext.employeeId);
      await loadData();
      setSuccess('新的盤點單已建立，請依現場實數逐項登記');
      if (res.data?.id) {
        navigate(`/pos/inventory/stock-takes/${res.data.id}`);
      }
    } catch (err) {
      await loadData();
      setError(getApiErrorMessage(err, '建立盤點單失敗，可能已有進行中的盤點單'));
    }
  };

  const handleOpenTake = (take: StockTake) => {
    if (take.status === 'IN_PROGRESS') {
      navigate(`/pos/inventory/stock-takes/${take.id}`);
      return;
    }
    setSelectedTakeId(prev => prev === take.id ? null : take.id);
  };

  const handleSubmitCount = async (takeId: string, item: StockTakeItem) => {
    const key = getItemKey(takeId, item.itemId);
    const value = countInputs[key];
    if (value === undefined || value === '') {
      setError('請先輸入實盤數量');
      return;
    }

    try {
      setSavingKeys(prev => ({ ...prev, [key]: true }));
      await stockTakeApi.submitCount(takeId, item.itemId, Number(value));
      await loadData();
      setSuccess(`${getProductName(item.itemId)} 已登記`);
    } catch {
      setError('登記盤點數量失敗');
    } finally {
      setSavingKeys(prev => ({ ...prev, [key]: false }));
    }
  };

  const handleComplete = async (take: StockTake) => {
    const invalidLines = getInvalidCountLines(take);
    if (invalidLines.length > 0) {
      setError('實盤數量不可為空、負數或非數字');
      return;
    }

    const pendingLines = getPendingCountLines(take);
    const uncountedCount = getEffectiveUncountedCount(take);
    const prefix = pendingLines.length > 0
      ? `將先登記 ${pendingLines.length} 筆畫面上的實盤數量，再完成盤點。`
      : '';
    const message = uncountedCount > 0
      ? `${prefix}仍有 ${uncountedCount} 個品項未盤。完成後未盤品項不會調整庫存，確定完成？`
      : `${prefix}確認完成盤點？完成後會依實盤差異調整庫存。`;
    if (!window.confirm(message)) return;

    try {
      setSavingComplete(true);
      for (const line of pendingLines) {
        await stockTakeApi.submitCount(take.id, line.item.itemId, line.countedQty);
      }
      await stockTakeApi.complete(take.id);
      await loadData();
      setSuccess('盤點單已完成，庫存已依實盤差異校正');
      navigate('/pos/inventory/stock-takes');
    } catch {
      setError('完成盤點失敗');
    } finally {
      setSavingComplete(false);
    }
  };

  const handleCancel = async (takeId: string) => {
    if (!window.confirm('確認取消這張盤點單？已登記的實盤數量不會調整庫存。')) return;
    try {
      await stockTakeApi.cancel(takeId);
      await loadData();
      setSuccess('盤點單已取消');
      navigate('/pos/inventory/stock-takes');
    } catch {
      setError('取消盤點單失敗');
    }
  };

  const renderItem = (take: StockTake, item: StockTakeItem) => {
    const key = getItemKey(take.id, item.itemId);
    const counted = item.countedQty !== null && item.countedQty !== undefined;
    const diff = Number(item.difference ?? 0);
    const editable = take.status === 'IN_PROGRESS';

    return (
      <Card key={item.id} variant="outlined" sx={{ borderRadius: 3 }}>
        <CardContent sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', lg: 'minmax(220px, 1.5fr) repeat(3, minmax(120px, 0.6fr)) minmax(150px, 0.75fr)' }, gap: 1.25, alignItems: 'center' }}>
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="body2" color="text.secondary" fontWeight={800}>{getProductSku(item.itemId)}</Typography>
            <Typography variant="h6" fontWeight={900} sx={{ overflowWrap: 'anywhere' }}>{getProductName(item.itemId)}</Typography>
          </Box>

          <Box>
            <Typography variant="caption" color="text.secondary">帳面庫存</Typography>
            <Typography variant="h6" fontWeight={900}>{formatQty(item.systemQty)}</Typography>
          </Box>

          <Box>
            <Typography variant="caption" color="text.secondary">實盤數量</Typography>
            {editable ? (
              <TextField
                type="number"
                value={countInputs[key] ?? ''}
                onChange={event => setCountInputs(prev => ({ ...prev, [key]: event.target.value }))}
                inputProps={{ min: 0, step: 0.001, 'data-stock-count-key': key }}
                size="small"
                fullWidth
              />
            ) : (
              <Typography variant="h6" fontWeight={900}>{counted ? formatQty(Number(item.countedQty)) : '未盤'}</Typography>
            )}
          </Box>

          <Box>
            <Typography variant="caption" color="text.secondary">差異</Typography>
            <Box sx={{ mt: 0.5 }}>
              {counted ? (
                <Chip
                  label={diff === 0 ? '相符' : `${diff > 0 ? '+' : ''}${formatQty(diff)}`}
                  color={diff === 0 ? 'success' : diff > 0 ? 'warning' : 'error'}
                />
              ) : (
                <Chip label="未盤" />
              )}
            </Box>
          </Box>

          <Box sx={{ display: 'flex', justifyContent: { xs: 'stretch', lg: 'flex-end' } }}>
            {editable && (
              <Button
                variant={counted ? 'outlined' : 'contained'}
                startIcon={<PlaylistAddCheck />}
                onClick={() => handleSubmitCount(take.id, item)}
                disabled={savingKeys[key] || savingComplete}
                fullWidth
                sx={{ minHeight: 48 }}
              >
                {counted ? '更新' : '登記'}
              </Button>
            )}
          </Box>
        </CardContent>
      </Card>
    );
  };

  const renderTakeDetail = (take: StockTake, embedded = false) => {
    const metrics = calcMetrics(take);
    const detailItems = [...take.items].sort((a, b) => {
      const aCounted = a.countedQty !== null && a.countedQty !== undefined;
      const bCounted = b.countedQty !== null && b.countedQty !== undefined;
      if (aCounted !== bCounted) return aCounted ? 1 : -1;
      return getProductName(a.itemId).localeCompare(getProductName(b.itemId), 'zh-Hant');
    });

    return (
      <Box sx={{ minWidth: 0, mt: embedded ? 1.25 : 0 }}>
        <Paper sx={{ p: { xs: 2, md: 3 }, borderRadius: 4, mb: 2 }}>
          <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, justifyContent: 'space-between', gap: 2 }}>
            <Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                <Typography variant={embedded ? 'h6' : 'h5'} fontWeight={900}>盤點基準 {formatDateTime(take.startedAt)}</Typography>
                <Chip label={STATUS_LABEL[take.status]} color={STATUS_COLOR[take.status]} />
              </Box>
              {take.completedAt && (
                <Typography color="text.secondary" sx={{ mt: 0.5 }}>完成時間：{formatDateTime(take.completedAt)}</Typography>
              )}
            </Box>
            {take.status === 'IN_PROGRESS' && (
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                <Button
                  variant="contained"
                  color="success"
                  startIcon={<CheckCircle />}
                  onClick={() => handleComplete(take)}
                  disabled={savingComplete}
                  sx={{ minHeight: 48 }}
                >
                  {savingComplete ? '完成中...' : '完成盤點'}
                </Button>
                <Button
                  variant="outlined"
                  color="error"
                  startIcon={<RestartAlt />}
                  onClick={() => handleCancel(take.id)}
                  disabled={savingComplete}
                  sx={{ minHeight: 48 }}
                >
                  取消
                </Button>
              </Box>
            )}
          </Box>

          <Box sx={{ mt: 2 }}>
            <LinearProgress
              variant="determinate"
              value={metrics.progress}
              color={metrics.progress >= 100 ? 'success' : 'secondary'}
              sx={{ height: 10, borderRadius: 5, mb: 1.5 }}
            />
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: 'repeat(2, minmax(0, 1fr))', md: 'repeat(4, minmax(0, 1fr))' }, gap: 1 }}>
              <Chip label={`進度 ${metrics.progress}%`} />
              <Chip label={`已盤 ${metrics.counted}`} color="success" variant="outlined" />
              <Chip label={`未盤 ${metrics.uncounted}`} color={metrics.uncounted > 0 ? 'warning' : 'success'} variant="outlined" />
              <Chip label={`差異 ${metrics.variance}`} color={metrics.variance > 0 ? 'warning' : 'success'} variant="outlined" />
            </Box>
          </Box>
        </Paper>

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          {detailItems.map(item => renderItem(take, item))}
          {detailItems.length === 0 && (
            <Card variant="outlined" sx={{ borderRadius: 3 }}>
              <CardContent>
                <Typography color="text.secondary">這張盤點單沒有品項。請先透過進貨驗收或商品庫存建立庫存資料。</Typography>
              </CardContent>
            </Card>
          )}
        </Box>
      </Box>
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
            {isDetailMode ? '盤點作業' : '盤點單'}
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 0.75 }}>
            {isDetailMode ? '逐項登記實盤數，完成後系統會依差異校正庫存。' : '檢視盤點紀錄；進行中的盤點會進入獨立作業頁。'}
          </Typography>
        </Box>
        {isDetailMode ? (
          <Button
            variant="outlined"
            startIcon={<ArrowBack />}
            onClick={() => navigate('/pos/inventory/stock-takes')}
            sx={{ minHeight: 56, px: 3, alignSelf: { xs: 'stretch', md: 'center' } }}
          >
            返回盤點列表
          </Button>
        ) : (
          <Button
            variant="contained"
            color="secondary"
            startIcon={inProgress ? <ChevronRight /> : <AssignmentTurnedIn />}
            onClick={inProgress ? () => navigate(`/pos/inventory/stock-takes/${inProgress.id}`) : handleStart}
            sx={{ minHeight: 56, px: 3, alignSelf: { xs: 'stretch', md: 'center' } }}
          >
            {inProgress ? '繼續進行中盤點' : '建立盤點單'}
          </Button>
        )}
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      {isDetailMode ? (
        activeTake ? renderTakeDetail(activeTake) : (
          <Paper sx={{ p: 4, borderRadius: 4, textAlign: 'center' }}>
            <AssignmentTurnedIn sx={{ fontSize: 52, color: 'text.secondary', mb: 1 }} />
            <Typography variant="h5" fontWeight={900}>找不到這張盤點單</Typography>
            <Typography color="text.secondary" sx={{ mt: 1, mb: 2 }}>可能已被取消、刪除，或目前門店沒有這筆盤點資料。</Typography>
            <Button variant="contained" onClick={() => navigate('/pos/inventory/stock-takes')}>返回盤點列表</Button>
          </Paper>
        )
      ) : sortedTakes.length === 0 ? (
        <Paper sx={{ p: 4, borderRadius: 4, textAlign: 'center' }}>
          <AssignmentTurnedIn sx={{ fontSize: 52, color: 'text.secondary', mb: 1 }} />
          <Typography variant="h5" fontWeight={900}>尚無盤點單</Typography>
          <Typography color="text.secondary" sx={{ mt: 1, mb: 2 }}>建立盤點單後，系統會擷取目前帳面庫存作為盤點基準。</Typography>
          <Button variant="contained" color="secondary" onClick={handleStart}>建立第一張盤點單</Button>
        </Paper>
      ) : (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          <Paper
            sx={{
              p: { xs: 1.25, md: 1.5 },
              borderRadius: 3,
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', lg: 'minmax(0, 1fr) auto' },
              alignItems: 'center',
              gap: 1.25,
            }}
          >
            <Box sx={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: 1 }}>
              <Typography fontWeight={900} sx={{ mr: 0.5 }}>盤點時間</Typography>
              <Chip
                size="small"
                label={selectedStockTakeDate || '全部日期'}
                variant={selectedStockTakeDate ? 'filled' : 'outlined'}
                sx={selectedStockTakeDate ? {
                  border: '1px solid',
                  borderColor: STOCK_TAKE_SELECTED_BG,
                  bgcolor: STOCK_TAKE_SELECTED_BG,
                  color: '#ffffff',
                  fontWeight: 900,
                } : { fontWeight: 900 }}
              />
              <Chip
                size="small"
                label={stockTakeFilterTimeLabel}
                variant={selectedStockTakeHour || selectedStockTakeMinute ? 'filled' : 'outlined'}
                sx={selectedStockTakeHour || selectedStockTakeMinute ? {
                  border: '1px solid',
                  borderColor: STOCK_TAKE_SELECTED_BG,
                  bgcolor: STOCK_TAKE_SELECTED_BG,
                  color: '#ffffff',
                  fontWeight: 900,
                } : { fontWeight: 900 }}
              />
              <Typography variant="body2" color="text.secondary" sx={{ ml: { xs: 0, md: 1 } }}>
                顯示 {pageStart}-{pageEnd} 筆，共 {filteredTakes.length} 筆
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', gap: 1, justifyContent: { xs: 'stretch', lg: 'flex-end' }, flexWrap: 'wrap' }}>
              <Button
                variant="contained"
                startIcon={<CalendarMonth />}
                onClick={() => setFilterPickerOpen(true)}
                sx={{ minHeight: 44, px: 2.25, flex: { xs: '1 1 180px', lg: '0 0 auto' } }}
              >
                選擇日期時間
              </Button>
              <Button
                variant="outlined"
                startIcon={<Close />}
                onClick={() => {
                  setSelectedStockTakeDate('');
                  setSelectedStockTakeHour('');
                  setSelectedStockTakeMinute('');
                }}
                disabled={!hasStockTakeFilter}
                sx={{ minHeight: 44, px: 2, flex: { xs: '1 1 120px', lg: '0 0 auto' } }}
              >
                清除
              </Button>
            </Box>
          </Paper>

          <Dialog
            open={filterPickerOpen}
            onClose={() => setFilterPickerOpen(false)}
            fullWidth
            maxWidth={false}
            PaperProps={{
              sx: {
                borderRadius: 3,
                width: { xs: 'calc(100vw - 28px)', sm: 720 },
                maxWidth: 'calc(100vw - 28px)',
                bgcolor: '#3f4350',
              },
            }}
          >
            <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1.25, px: 2, pt: 1.75, pb: 0.75 }}>
              <Box>
                <Typography variant="subtitle1" fontWeight={900}>選擇盤點日期時間</Typography>
                <Typography variant="body2" color="text.secondary">日期點選，時間用小時與分鐘滾動選取。</Typography>
              </Box>
              <IconButton
                aria-label="關閉日期時間選擇"
                onClick={() => setFilterPickerOpen(false)}
                sx={{
                  color: '#ffffff',
                  '&:hover': { bgcolor: 'rgba(255,255,255,0.1)' },
                }}
              >
                <Close />
              </IconButton>
            </DialogTitle>
            <DialogContent
              sx={{
                px: 2,
                pt: 1,
                display: 'grid',
                gridTemplateColumns: { xs: '1fr', md: 'minmax(280px, 1fr) 220px' },
                gap: 1.25,
              }}
            >
              <Box sx={{ border: '1px solid', borderColor: 'rgba(255,255,255,0.16)', borderRadius: 2.5, p: 1 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1, mb: 1 }}>
                  <Typography fontWeight={900}>日期</Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                    <IconButton
                      aria-label="上一個月"
                      onClick={() => setCalendarMonth(prev => new Date(prev.getFullYear(), prev.getMonth() - 1, 1))}
                      sx={{
                        width: 34,
                        height: 34,
                        color: '#ffffff',
                        '&:hover': { bgcolor: 'rgba(255,255,255,0.1)' },
                      }}
                    >
                      <ChevronLeft />
                    </IconButton>
                    <Select
                      size="small"
                      value={String(calendarMonth.getFullYear())}
                      onChange={(event: SelectChangeEvent) => {
                        setCalendarMonth(prev => new Date(Number(event.target.value), prev.getMonth(), 1));
                      }}
                      sx={{
                        height: 34,
                        minWidth: 104,
                        color: '#ffffff',
                        fontWeight: 900,
                        '.MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.26)' },
                        '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.52)' },
                        '.MuiSvgIcon-root': { color: '#ffffff' },
                      }}
                    >
                      {calendarYearOptions.map(year => (
                        <MenuItem key={year} value={String(year)}>{year} 年</MenuItem>
                      ))}
                    </Select>
                    <Select
                      size="small"
                      value={String(calendarMonth.getMonth() + 1)}
                      onChange={(event: SelectChangeEvent) => {
                        setCalendarMonth(prev => new Date(prev.getFullYear(), Number(event.target.value) - 1, 1));
                      }}
                      sx={{
                        height: 34,
                        minWidth: 82,
                        color: '#ffffff',
                        fontWeight: 900,
                        '.MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.26)' },
                        '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.52)' },
                        '.MuiSvgIcon-root': { color: '#ffffff' },
                      }}
                    >
                      {MONTH_OPTIONS.map(month => (
                        <MenuItem key={month} value={String(month)}>{month} 月</MenuItem>
                      ))}
                    </Select>
                    <IconButton
                      aria-label="下一個月"
                      onClick={() => setCalendarMonth(prev => new Date(prev.getFullYear(), prev.getMonth() + 1, 1))}
                      sx={{
                        width: 34,
                        height: 34,
                        color: '#ffffff',
                        '&:hover': { bgcolor: 'rgba(255,255,255,0.1)' },
                      }}
                    >
                      <ChevronRight />
                    </IconButton>
                  </Box>
                </Box>
                <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(7, minmax(0, 1fr))', gap: 0.4 }}>
                  {WEEKDAY_LABELS.map(label => (
                    <Typography key={label} variant="caption" color="text.secondary" fontWeight={900} textAlign="center">
                      {label}
                    </Typography>
                  ))}
                  {calendarDays.map((date, index) => {
                    if (!date) return <Box key={`blank-${index}`} sx={{ minHeight: 34 }} />;
                    const value = formatCalendarDate(date);
                    const selected = selectedStockTakeDate === value;
                    const hasTake = stockTakeDateSet.has(value);
                    return (
                      <Button
                        key={value}
                        variant="outlined"
                        onClick={() => setSelectedStockTakeDate(value)}
                        sx={{
                          ...pickerChoiceSx(selected),
                          minWidth: 0,
                          minHeight: 34,
                          p: 0,
                          opacity: hasTake || selected ? 1 : 0.55,
                        }}
                      >
                        {date.getDate()}
                      </Button>
                    );
                  })}
                </Box>
              </Box>

              <Box sx={{ border: '1px solid', borderColor: 'rgba(255,255,255,0.16)', borderRadius: 2.5, p: 1 }}>
                <Typography fontWeight={900} sx={{ mb: 1 }}>時間</Typography>
                <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1 }}>
                  {[
                    { label: '小時', value: selectedStockTakeHour, setValue: setSelectedStockTakeHour, options: HOUR_OPTIONS },
                    { label: '分鐘', value: selectedStockTakeMinute, setValue: setSelectedStockTakeMinute, options: MINUTE_OPTIONS },
                  ].map(group => (
                    <Box key={group.label}>
                      <Typography variant="caption" color="text.secondary" fontWeight={900}>{group.label}</Typography>
                      <Box
                        sx={{
                          mt: 0.75,
                          maxHeight: 200,
                          overflowY: 'auto',
                          pr: 0.5,
                          display: 'flex',
                          flexDirection: 'column',
                          gap: 0.4,
                        }}
                      >
                        <Button
                          variant="outlined"
                          onClick={() => group.setValue('')}
                          sx={pickerChoiceSx(group.value === '')}
                        >
                          全部
                        </Button>
                        {group.options.map(option => (
                          <Button
                            key={option}
                            variant="outlined"
                            onClick={() => group.setValue(option)}
                            sx={pickerChoiceSx(group.value === option)}
                          >
                            {option}
                          </Button>
                        ))}
                      </Box>
                    </Box>
                  ))}
                </Box>
              </Box>
            </DialogContent>
            <DialogActions sx={{ px: 2, py: 1.5, gap: 1, flexWrap: 'wrap' }}>
              <Box sx={{ mr: 'auto', color: 'rgba(255,255,255,0.82)', fontSize: 14, fontWeight: 800 }}>
                {selectedStockTakeDate || '全部日期'} · {stockTakeFilterTimeLabel}
              </Box>
              <Button
                variant="outlined"
                startIcon={<Close />}
                disabled={!hasStockTakeFilter}
                sx={{
                  borderColor: 'rgba(255,255,255,0.34)',
                  color: '#ffffff',
                  '&:hover': {
                    borderColor: '#ffffff',
                    bgcolor: 'rgba(255,255,255,0.08)',
                  },
                  '&.Mui-disabled': {
                    borderColor: 'rgba(255,255,255,0.14)',
                    color: 'rgba(255,255,255,0.38)',
                  },
                }}
                onClick={() => {
                  setSelectedStockTakeDate('');
                  setSelectedStockTakeHour('');
                  setSelectedStockTakeMinute('');
                }}
              >
                清除篩選
              </Button>
              <Button
                variant="contained"
                onClick={() => setFilterPickerOpen(false)}
                sx={{
                  bgcolor: STOCK_TAKE_SELECTED_BG,
                  color: '#ffffff',
                  '&:hover': { bgcolor: '#ff8a2a' },
                }}
              >
                套用
              </Button>
            </DialogActions>
          </Dialog>

          {filteredTakes.length === 0 ? (
            <Paper sx={{ p: 4, borderRadius: 4, textAlign: 'center' }}>
              <AssignmentTurnedIn sx={{ fontSize: 52, color: 'text.secondary', mb: 1 }} />
              <Typography variant="h5" fontWeight={900}>找不到盤點單</Typography>
              <Typography color="text.secondary" sx={{ mt: 1, mb: 2 }}>
                沒有符合目前日期與時間條件的盤點單，請改選日期或時間。
              </Typography>
              <Button
                variant="outlined"
                startIcon={<Close />}
                onClick={() => {
                  setSelectedStockTakeDate('');
                  setSelectedStockTakeHour('');
                  setSelectedStockTakeMinute('');
                }}
              >
                清除篩選
              </Button>
            </Paper>
          ) : pagedTakes.map(take => {
            const itemMetrics = calcMetrics(take);
            const selected = selectedTakeId === take.id;
            return (
              <Box key={take.id}>
                <Card
                  variant="outlined"
                  onClick={() => handleOpenTake(take)}
                  sx={{
                    borderRadius: 3,
                    cursor: 'pointer',
                    borderColor: selected ? 'secondary.main' : 'divider',
                    bgcolor: selected ? 'rgba(255,109,0,0.08)' : 'background.paper',
                  }}
                >
                  <CardContent>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1, mb: 1 }}>
                      <Typography fontWeight={900}>{formatDateTime(take.startedAt)}</Typography>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Chip label={STATUS_LABEL[take.status]} color={STATUS_COLOR[take.status]} size="small" />
                        <ChevronRight sx={{
                          color: 'text.secondary',
                          transform: selected && take.status !== 'IN_PROGRESS' ? 'rotate(90deg)' : 'none',
                          transition: 'transform 160ms ease',
                        }} />
                      </Box>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={itemMetrics.progress}
                      color={itemMetrics.progress >= 100 ? 'success' : 'secondary'}
                      sx={{ height: 8, borderRadius: 4, mb: 1 }}
                    />
                    <Typography variant="body2" color="text.secondary">
                      {itemMetrics.counted}/{itemMetrics.total} 已盤，差異 {itemMetrics.variance}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.75 }}>
                      {take.status === 'IN_PROGRESS' ? '點擊進入盤點作業' : selected ? '點擊收合詳情' : '點擊查看此盤點單詳情'}
                    </Typography>
                  </CardContent>
                </Card>
                {selected && take.status !== 'IN_PROGRESS' && renderTakeDetail(take, true)}
              </Box>
            );
          })}

          {filteredTakes.length > STOCK_TAKE_PAGE_SIZE && (
            <Paper
              sx={{
                p: { xs: 1.5, md: 2 },
                borderRadius: 4,
                display: 'flex',
                flexDirection: { xs: 'column', md: 'row' },
                alignItems: { xs: 'stretch', md: 'center' },
                justifyContent: 'space-between',
                gap: 1.5,
              }}
            >
              <Typography color="text.secondary" fontWeight={800}>
                第 {currentStockTakePage} / {stockTakePageCount} 頁
              </Typography>
              <Pagination
                count={stockTakePageCount}
                page={currentStockTakePage}
                color="secondary"
                size="large"
                onChange={(_, value) => {
                  setSelectedTakeId(null);
                  setStockTakePage(value);
                }}
                sx={{
                  alignSelf: { xs: 'center', md: 'auto' },
                  '& .MuiPaginationItem-root': {
                    minWidth: 44,
                    height: 44,
                    fontWeight: 900,
                  },
                }}
              />
            </Paper>
          )}
        </Box>
      )}
    </Box>
  );
};

export default StockTakePage;
