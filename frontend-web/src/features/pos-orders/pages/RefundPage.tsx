/**
 * @file RefundPage.tsx
 * @description 退款管理頁面 / Refund management page
 * @description_en Page for creating and processing order refunds
 * @description_zh 退款申請與處理頁面
 */
import React, { useState } from 'react';
import {
  Box, Typography, TextField, Button, Alert,
  Card, CardContent, Divider, MenuItem, Select,
  FormControl, InputLabel, InputAdornment, Chip,
} from '@mui/material';
import { useSearchParams } from 'react-router-dom';
import { refundApi } from '../api/orderApi';
import type { CreateRefundRequest, OrderRefund } from '../types';
import { DEFAULT_EMPLOYEE_ID } from '../config';
import { formatMoney } from '@shared/utils';

const PAY_METHODS = ['CASH', 'CREDIT_CARD', 'LINE_PAY', 'JKOPAY', 'EASYCARD', 'OTHER'];

const parseAmount = (value: string | null) => {
  const amount = Number(value);
  return Number.isFinite(amount) && amount > 0 ? amount : undefined;
};

const RefundPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const orderNo = searchParams.get('orderNo');
  const [form, setForm] = useState<Partial<CreateRefundRequest>>({
    orderId: searchParams.get('orderId') ?? '',
    refundAmount: parseAmount(searchParams.get('amount')),
    refundMethod: 'CASH',
    approvedBy: DEFAULT_EMPLOYEE_ID,
  });
  const [result, setResult] = useState<OrderRefund | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // ========================================
  // 提交退款申請 / Submit refund request
  // ========================================
  const handleSubmit = async () => {
    if (!form.orderId || !form.refundAmount || !form.refundMethod) {
      setError('請填寫必要欄位（訂單 ID、退款金額、退款方式）');
      return;
    }
    if (form.refundAmount <= 0) {
      setError('退款金額必須大於 0');
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
    try {
      const res = await refundApi.complete(result.id);
      if (res.success && res.data) setResult(res.data);
    } catch {
      setError('完成退款失敗');
    } finally {
      setLoading(false);
    }
  };

  const update = (key: keyof CreateRefundRequest, value: unknown) =>
    setForm(f => ({ ...f, [key]: value }));

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, maxWidth: 760 }}>
      <Box>
        <Typography variant="h4" fontWeight={900} sx={{ mb: 0.5 }}>
          退款申請
        </Typography>
        <Typography variant="body2" color="text.secondary">
          從訂單列表帶入訂單資料，建立退款申請後可立即完成退款處理。
        </Typography>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {orderNo && <Chip label={`訂單 ${orderNo}`} sx={{ alignSelf: 'flex-start', fontFamily: 'monospace', fontWeight: 800 }} />}

      {result ? (
        // 退款結果 / Refund result
        <Card sx={{ bgcolor: 'background.paper', borderRadius: 3, border: '1px solid rgba(255,255,255,0.06)' }}>
          <CardContent>
            <Typography variant="h6" mb={1}>退款申請已建立</Typography>
            <Typography>退款單號：<strong>{result.refundNo}</strong></Typography>
            <Typography>狀態：<strong>{result.status}</strong></Typography>
            <Typography>退款金額：<strong>{formatMoney(result.refundAmount)}</strong></Typography>
            {result.status === 'APPROVED' && (
              <>
                <Divider sx={{ my: 2 }} />
                <Button variant="contained" color="primary" onClick={handleComplete} disabled={loading}>
                  確認退款完成
                </Button>
              </>
            )}
          </CardContent>
        </Card>
      ) : (
        // 退款表單 / Refund form
        <Card sx={{ bgcolor: 'background.paper', borderRadius: 3, border: '1px solid rgba(255,255,255,0.06)' }}>
          <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              fullWidth label="訂單 ID" required
              value={form.orderId ?? ''}
              onChange={e => update('orderId', e.target.value)}
            />

            <TextField
              fullWidth label="退款金額" required type="number"
              InputProps={{ startAdornment: <InputAdornment position="start">$</InputAdornment> }}
              value={form.refundAmount ?? ''}
              onChange={e => update('refundAmount', Number(e.target.value))}
              inputProps={{ min: 0.01, step: 0.01 }}
            />

            <FormControl fullWidth>
              <InputLabel>退款方式</InputLabel>
              <Select value={form.refundMethod ?? 'CASH'} label="退款方式"
                onChange={e => update('refundMethod', e.target.value)}>
                {PAY_METHODS.map(m => <MenuItem key={m} value={m}>{m}</MenuItem>)}
              </Select>
            </FormControl>

            <TextField
              fullWidth label="退款原因" multiline rows={3}
              value={form.reason ?? ''}
              onChange={e => update('reason', e.target.value)}
            />

            <TextField
              fullWidth label="核准人 UUID"
              value={form.approvedBy ?? ''}
              onChange={e => update('approvedBy', e.target.value || undefined)}
              helperText="目前本地測試預設使用操作員 UUID；留空會建立待核准退款，不能直接完成。"
            />

            <Button variant="contained" onClick={handleSubmit} disabled={loading}>
              {loading ? '處理中...' : '提交退款申請'}
            </Button>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default RefundPage;
