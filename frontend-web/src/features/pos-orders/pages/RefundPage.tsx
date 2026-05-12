/**
 * @file RefundPage.tsx
 * @description 退款管理頁面 / Refund management page
 * @description_en Page for creating and processing order refunds with order context
 * @description_zh 退款申請與處理頁面，提供訂單脈絡、金額檢查與處理進度
 */
import React, { useEffect, useMemo, useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress, Divider, InputAdornment,
  LinearProgress, Paper, Stack, Step, StepLabel, Stepper, TextField, ToggleButton, ToggleButtonGroup,
  Tooltip, Typography,
} from '@mui/material';
import {
  AccountBalanceWallet, CheckCircle, CreditCard, ErrorOutline, InfoOutlined,
  LocalAtm, Payments, Person, QrCode2, ReceiptLong, Search, ShieldOutlined, Undo,
} from '@mui/icons-material';
import { useSearchParams } from 'react-router-dom';
import { orderApi, refundApi } from '../api/orderApi';
import type { CreateRefundRequest, Order, OrderRefund } from '../types';
import { DEFAULT_EMPLOYEE_ID } from '../config';
import { formatMoney } from '@shared/utils';

interface RefundMethodOption {
  value: string;
  label: string;
  helper: string;
  icon: React.ReactNode;
}

const PAY_METHODS: RefundMethodOption[] = [
  { value: 'CASH', label: '現金', helper: '由錢櫃退還', icon: <LocalAtm fontSize="small" /> },
  { value: 'CREDIT_CARD', label: '信用卡', helper: '原卡刷退', icon: <CreditCard fontSize="small" /> },
  { value: 'LINE_PAY', label: 'LINE Pay', helper: '原支付退回', icon: <QrCode2 fontSize="small" /> },
  { value: 'JKOPAY', label: '街口支付', helper: '電子支付退回', icon: <QrCode2 fontSize="small" /> },
  { value: 'EASYCARD', label: '悠遊卡', helper: '票證退回', icon: <AccountBalanceWallet fontSize="small" /> },
  { value: 'OTHER', label: '其他', helper: '人工備註處理', icon: <Payments fontSize="small" /> },
];

const REASON_PRESETS = ['顧客取消', '商品錯誤', '重複結帳', '服務補償'];

const parseAmount = (value: string | null) => {
  const amount = Number(value);
  return Number.isFinite(amount) && amount > 0 ? amount : undefined;
};

const parsePositiveAmount = (value: string) => {
  if (value.trim() === '') return undefined;
  const amount = Number(value);
  return Number.isFinite(amount) && amount > 0 ? amount : undefined;
};

const methodLabel = (method: string | undefined) =>
  PAY_METHODS.find(option => option.value === method)?.label ?? method ?? '-';

const statusLabel = (status: OrderRefund['status'] | undefined) => {
  switch (status) {
    case 'PENDING': return '待核准';
    case 'APPROVED': return '已核准';
    case 'REJECTED': return '已拒絕';
    case 'COMPLETED': return '退款完成';
    default: return '尚未建立';
  }
};

const RefundPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const initialOrderId = searchParams.get('orderId') ?? '';
  const initialAmount = parseAmount(searchParams.get('amount'));
  const orderNo = searchParams.get('orderNo');
  const [form, setForm] = useState<Partial<CreateRefundRequest>>({
    orderId: initialOrderId,
    refundAmount: initialAmount,
    refundMethod: 'CASH',
    approvedBy: DEFAULT_EMPLOYEE_ID,
  });
  const [order, setOrder] = useState<Order | null>(null);
  const [orderLoading, setOrderLoading] = useState(false);
  const [orderError, setOrderError] = useState('');
  const [result, setResult] = useState<OrderRefund | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const refundAmount = form.refundAmount ?? 0;
  const maxRefundAmount = order?.grandTotal ?? initialAmount ?? 0;
  const isFullRefund = maxRefundAmount > 0 && refundAmount >= maxRefundAmount;
  const canSubmit = Boolean(form.orderId && form.refundAmount && form.refundMethod && form.refundAmount > 0);
  const activeStep = result ? (result.status === 'COMPLETED' ? 3 : 2) : 1;
  const refundRatio = maxRefundAmount > 0 ? Math.min((refundAmount / maxRefundAmount) * 100, 100) : 0;

  // ========================================
  // 載入訂單脈絡 / Load order context
  // ========================================
  useEffect(() => {
    if (!form.orderId) {
      setOrder(null);
      return;
    }

    let alive = true;
    setOrderLoading(true);
    setOrderError('');
    orderApi.getById(form.orderId)
      .then((res) => {
        if (!alive) return;
        if (res.success && res.data) {
          setOrder(res.data);
          setOrderError('');
        } else {
          setOrder(null);
          setOrderError(res.message || '找不到此訂單，仍可手動建立退款申請。');
        }
      })
      .catch(() => {
        if (!alive) return;
        setOrder(null);
        setOrderError('訂單資料載入失敗，請確認訂單 ID。');
      })
      .finally(() => {
        if (alive) setOrderLoading(false);
      });

    return () => { alive = false; };
  }, [form.orderId]);

  const amountHints = useMemo(() => {
    if (!maxRefundAmount) return [];
    return [
      { label: '全額', value: maxRefundAmount },
      { label: '半額', value: Math.round(maxRefundAmount / 2) },
      { label: '只退稅額', value: order?.taxTotal ?? 0 },
    ].filter(item => item.value > 0);
  }, [maxRefundAmount, order?.taxTotal]);

  // ========================================
  // 提交退款申請 / Submit refund request
  // ========================================
  const handleSubmit = async () => {
    if (!canSubmit) {
      setError('請填寫必要欄位（訂單 ID、退款金額、退款方式）');
      return;
    }
    if (maxRefundAmount > 0 && refundAmount > maxRefundAmount) {
      setError('退款金額不可超過訂單總額');
      return;
    }
    setLoading(true);
    setError('');
    setResult(null);
    try {
      const res = await refundApi.create(form as CreateRefundRequest);
      if (res.success && res.data) {
        setResult(res.data);
      } else {
        setError(res.message || '退款申請失敗');
      }
    } catch {
      setError('退款申請失敗，請確認訂單資訊');
    } finally {
      setLoading(false);
    }
  };

  // ========================================
  // 完成退款 / Complete refund
  // ========================================
  const handleComplete = async () => {
    if (!result) return;
    setLoading(true);
    setError('');
    try {
      const res = await refundApi.complete(result.id);
      if (res.success && res.data) setResult(res.data);
      else setError(res.message || '完成退款失敗');
    } catch {
      setError('完成退款失敗');
    } finally {
      setLoading(false);
    }
  };

  const update = (key: keyof CreateRefundRequest, value: unknown) => {
    setResult(null);
    setError('');
    setForm(previous => ({ ...previous, [key]: value }));
  };

  const applyReasonPreset = (reason: string) => {
    update('reason', form.reason === reason ? '' : reason);
  };

  const renderOrderSummary = () => (
    <Paper
      elevation={0}
      sx={{
        p: { xs: 2, md: 2.5 },
        borderRadius: 2,
        border: '1px solid rgba(255,255,255,0.08)',
        bgcolor: 'rgba(255,255,255,0.035)',
      }}
    >
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <ReceiptLong fontSize="small" />
        <Typography variant="subtitle1" fontWeight={900}>訂單摘要</Typography>
        {orderLoading && <CircularProgress size={16} />}
      </Stack>

      {orderError && <Alert severity="warning" sx={{ mb: 2 }}>{orderError}</Alert>}

      <Stack spacing={1.5}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2 }}>
          <Typography color="text.secondary">訂單編號</Typography>
          <Typography fontWeight={800} sx={{ textAlign: 'right', overflowWrap: 'anywhere' }}>
            {order?.orderNo ?? orderNo ?? '尚未載入'}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2 }}>
          <Typography color="text.secondary">訂單狀態</Typography>
          <Chip
            size="small"
            label={order?.status ?? 'UNKNOWN'}
            color={order?.status === 'COMPLETED' || order?.status === 'CLOSED' ? 'success' : 'default'}
          />
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2 }}>
          <Typography color="text.secondary">原訂單總額</Typography>
          <Typography fontWeight={900}>{formatMoney(maxRefundAmount)}</Typography>
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2 }}>
          <Typography color="text.secondary">稅額 / 折扣</Typography>
          <Typography>{formatMoney(order?.taxTotal ?? 0)} / {formatMoney(order?.discountTotal ?? 0)}</Typography>
        </Box>
      </Stack>

      {order?.items?.length ? (
        <>
          <Divider sx={{ my: 2 }} />
          <Stack spacing={1.2}>
            {order.items.slice(0, 4).map(item => (
              <Box key={item.id} sx={{ display: 'grid', gridTemplateColumns: '1fr auto', gap: 1 }}>
                <Box sx={{ minWidth: 0 }}>
                  <Typography fontWeight={800} noWrap>{item.itemNameSnapshot}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {item.skuSnapshot || item.itemId} · x{item.quantity}
                  </Typography>
                </Box>
                <Typography fontWeight={800}>{formatMoney(item.lineTotal)}</Typography>
              </Box>
            ))}
            {order.items.length > 4 && (
              <Typography variant="caption" color="text.secondary">
                另有 {order.items.length - 4} 個品項
              </Typography>
            )}
          </Stack>
        </>
      ) : null}
    </Paper>
  );

  const renderRefundStatus = () => (
    <Paper
      elevation={0}
      sx={{
        p: { xs: 2, md: 2.5 },
        borderRadius: 2,
        border: '1px solid rgba(255,255,255,0.08)',
        bgcolor: 'rgba(255,255,255,0.035)',
      }}
    >
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        {result?.status === 'COMPLETED'
          ? <CheckCircle color="success" fontSize="small" />
          : <ShieldOutlined color="warning" fontSize="small" />}
        <Typography variant="subtitle1" fontWeight={900}>處理狀態</Typography>
      </Stack>

      <Stack spacing={1.5}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2 }}>
          <Typography color="text.secondary">退款狀態</Typography>
          <Chip
            label={statusLabel(result?.status)}
            color={result?.status === 'COMPLETED' ? 'success' : result?.status === 'APPROVED' ? 'warning' : 'default'}
            size="small"
          />
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2 }}>
          <Typography color="text.secondary">退款單號</Typography>
          <Typography fontWeight={800} sx={{ textAlign: 'right', overflowWrap: 'anywhere' }}>
            {result?.refundNo ?? '提交後產生'}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2 }}>
          <Typography color="text.secondary">退款金額</Typography>
          <Typography fontWeight={900} color={refundAmount > 0 ? 'warning.main' : 'text.primary'}>
            {formatMoney(result?.refundAmount ?? refundAmount)}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2 }}>
          <Typography color="text.secondary">退款方式</Typography>
          <Typography fontWeight={800}>{methodLabel(result?.refundMethod ?? form.refundMethod)}</Typography>
        </Box>
      </Stack>

      <Divider sx={{ my: 2 }} />
      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
        退款占訂單總額
      </Typography>
      <LinearProgress
        variant="determinate"
        value={refundRatio}
        color={isFullRefund ? 'warning' : 'primary'}
        sx={{ height: 8, borderRadius: 999, bgcolor: 'rgba(255,255,255,0.08)' }}
      />
      <Typography variant="caption" color="text.secondary">
        {maxRefundAmount > 0 ? `${Math.round(refundRatio)}% · ${isFullRefund ? '全額退款會觸發庫存回補檢查' : '部分退款不會自動回補整單庫存'}` : '輸入訂單後顯示比例'}
      </Typography>
    </Paper>
  );

  return (
    <Box sx={{
      display: 'flex',
      flexDirection: 'column',
      gap: 3,
      maxWidth: 1320,
      minHeight: '100%',
    }}>
      <Box sx={{
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', md: '1fr auto' },
        gap: 2,
        alignItems: 'start',
      }}>
        <Box>
          <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 1 }}>
            <Undo sx={{ color: 'secondary.main' }} />
            <Typography variant="h4" fontWeight={900} letterSpacing={0}>
              退款工作台
            </Typography>
          </Stack>
          <Typography variant="body2" color="text.secondary">
            核對訂單、確認退款金額與方式，建立申請後可立即完成已核准退款。
          </Typography>
        </Box>
        <Stepper
          activeStep={activeStep}
          alternativeLabel
          sx={{
            minWidth: { md: 420 },
            '& .MuiStepLabel-label': { color: 'text.secondary', fontSize: 12 },
            '& .MuiStepLabel-label.Mui-active, & .MuiStepLabel-label.Mui-completed': { color: 'text.primary' },
          }}
        >
          {['訂單', '申請', '核准', '完成'].map(label => (
            <Step key={label}><StepLabel>{label}</StepLabel></Step>
          ))}
        </Stepper>
      </Box>

      {error && <Alert severity="error" icon={<ErrorOutline />}>{error}</Alert>}

      <Box sx={{
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', lg: 'minmax(0, 1.1fr) minmax(360px, 0.9fr)' },
        gap: 3,
        alignItems: 'start',
      }}>
        <Paper
          elevation={0}
          sx={{
            p: { xs: 2, md: 3 },
            borderRadius: 2,
            border: '1px solid rgba(255,255,255,0.08)',
            bgcolor: 'background.paper',
            boxShadow: '0 18px 50px rgba(0,0,0,0.18)',
          }}
        >
          <Stack spacing={3}>
            <Box>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
                <Search fontSize="small" />
                <Typography variant="h6" fontWeight={900}>1. 確認退款訂單</Typography>
              </Stack>
              {orderNo && (
                <Chip
                  label={`來自訂單 ${orderNo}`}
                  sx={{ mb: 2, fontFamily: 'monospace', fontWeight: 800 }}
                />
              )}
              <TextField
                fullWidth
                label="訂單 ID"
                required
                value={form.orderId ?? ''}
                onChange={event => update('orderId', event.target.value)}
                InputProps={{
                  startAdornment: <InputAdornment position="start"><ReceiptLong fontSize="small" /></InputAdornment>,
                }}
                helperText="從訂單列表點擊退款時會自動帶入，也可以手動貼上訂單 UUID。"
              />
            </Box>

            <Divider />

            <Box>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
                <Payments fontSize="small" />
                <Typography variant="h6" fontWeight={900}>2. 設定退款金額</Typography>
              </Stack>
              <TextField
                fullWidth
                label="退款金額"
                required
                type="number"
                InputProps={{ startAdornment: <InputAdornment position="start">$</InputAdornment> }}
                value={form.refundAmount ?? ''}
                onChange={event => update('refundAmount', parsePositiveAmount(event.target.value))}
                inputProps={{ min: 0.01, step: 1 }}
                error={maxRefundAmount > 0 && refundAmount > maxRefundAmount}
                helperText={maxRefundAmount > 0
                  ? `可退款上限 ${formatMoney(maxRefundAmount)}`
                  : '載入訂單後會自動顯示退款上限。'}
              />
              <Stack direction="row" flexWrap="wrap" gap={1} sx={{ mt: 1.5 }}>
                {amountHints.map(hint => (
                  <Button
                    key={hint.label}
                    size="small"
                    variant={refundAmount === hint.value ? 'contained' : 'outlined'}
                    onClick={() => update('refundAmount', hint.value)}
                  >
                    {hint.label} {formatMoney(hint.value)}
                  </Button>
                ))}
              </Stack>
            </Box>

            <Box>
              <Typography variant="h6" fontWeight={900} sx={{ mb: 2 }}>
                3. 選擇退款方式
              </Typography>
              <ToggleButtonGroup
                exclusive
                fullWidth
                value={form.refundMethod ?? 'CASH'}
                onChange={(_, value: string | null) => { if (value) update('refundMethod', value); }}
                sx={{
                  display: 'grid',
                  gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' },
                  gap: 1,
                  '& .MuiToggleButtonGroup-grouped': {
                    border: '1px solid rgba(255,255,255,0.1) !important',
                    borderRadius: '8px !important',
                    justifyContent: 'flex-start',
                    p: 1.4,
                    color: 'text.primary',
                    bgcolor: 'rgba(255,255,255,0.03)',
                  },
                  '& .Mui-selected': {
                    bgcolor: 'rgba(112,72,232,0.28) !important',
                    borderColor: 'rgba(151,117,250,0.8) !important',
                  },
                }}
              >
                {PAY_METHODS.map(option => (
                  <ToggleButton key={option.value} value={option.value}>
                    <Stack direction="row" spacing={1.2} alignItems="center">
                      {option.icon}
                      <Box sx={{ textAlign: 'left' }}>
                        <Typography fontWeight={900}>{option.label}</Typography>
                        <Typography variant="caption" color="text.secondary">{option.helper}</Typography>
                      </Box>
                    </Stack>
                  </ToggleButton>
                ))}
              </ToggleButtonGroup>
            </Box>

            <Box>
              <Typography variant="h6" fontWeight={900} sx={{ mb: 2 }}>
                4. 退款原因與核准
              </Typography>
              <Stack direction="row" flexWrap="wrap" gap={1} sx={{ mb: 1.5 }}>
                {REASON_PRESETS.map(reason => (
                  <Chip
                    key={reason}
                    label={reason}
                    color={form.reason === reason ? 'secondary' : 'default'}
                    onClick={() => applyReasonPreset(reason)}
                    sx={{ fontWeight: 800 }}
                  />
                ))}
              </Stack>
              <TextField
                fullWidth
                label="退款原因"
                multiline
                rows={3}
                value={form.reason ?? ''}
                onChange={event => update('reason', event.target.value)}
                placeholder="例如：顧客取消、品項錯誤、重複結帳、主管核准備註"
              />
              <TextField
                fullWidth
                label="核准人 UUID"
                value={form.approvedBy ?? ''}
                onChange={event => update('approvedBy', event.target.value || undefined)}
                sx={{ mt: 2 }}
                InputProps={{
                  startAdornment: <InputAdornment position="start"><Person fontSize="small" /></InputAdornment>,
                }}
                helperText="本地測試預設使用操作員 UUID；留空會建立待核准退款，不會立即完成。"
              />
            </Box>

            <Box sx={{
              display: 'flex',
              gap: 1.5,
              flexDirection: { xs: 'column', sm: 'row' },
              alignItems: { sm: 'center' },
            }}>
              <Button
                variant="contained"
                size="large"
                onClick={handleSubmit}
                disabled={loading || !canSubmit}
                startIcon={loading ? <CircularProgress size={18} color="inherit" /> : <ShieldOutlined />}
                sx={{ minHeight: 52, flex: 1, fontWeight: 900 }}
              >
                {loading ? '處理中...' : '建立退款申請'}
              </Button>
              {result?.status === 'APPROVED' && (
                <Button
                  variant="outlined"
                  color="warning"
                  size="large"
                  onClick={handleComplete}
                  disabled={loading}
                  startIcon={<CheckCircle />}
                  sx={{ minHeight: 52, flex: 1, fontWeight: 900 }}
                >
                  確認退款完成
                </Button>
              )}
            </Box>
          </Stack>
        </Paper>

        <Stack spacing={2}>
          {renderOrderSummary()}
          {renderRefundStatus()}
          <Alert
            severity={isFullRefund ? 'warning' : 'info'}
            icon={<InfoOutlined />}
            sx={{ borderRadius: 2 }}
          >
            {isFullRefund
              ? '目前為全額退款。完成退款後，後端會檢查並回補追蹤庫存的品項。'
              : '目前為部分退款。因尚未支援品項級退款明細，完成後不會自動回補整張訂單庫存。'}
          </Alert>
          <Paper
            elevation={0}
            sx={{
              p: { xs: 2, md: 2.5 },
              borderRadius: 2,
              border: '1px solid rgba(255,255,255,0.08)',
              bgcolor: 'rgba(255,255,255,0.035)',
            }}
          >
            <Stack direction="row" alignItems="center" justifyContent="space-between" spacing={2}>
              <Box>
                <Typography fontWeight={900}>退款前檢查</Typography>
                <Typography variant="body2" color="text.secondary">
                  確認訂單、金額、方式與核准人後再建立申請。
                </Typography>
              </Box>
              <Tooltip title="若要避免現場誤退，正式環境應串接主管核准、發票折讓與金流刷退。">
                <InfoOutlined color="disabled" />
              </Tooltip>
            </Stack>
          </Paper>
        </Stack>
      </Box>
    </Box>
  );
};

export default RefundPage;
