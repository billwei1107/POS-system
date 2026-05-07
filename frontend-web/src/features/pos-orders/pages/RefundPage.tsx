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
  FormControl, InputLabel, InputAdornment,
} from '@mui/material';
import { refundApi } from '../api/orderApi';
import type { CreateRefundRequest, OrderRefund } from '../types';

const PAY_METHODS = ['CASH', 'CARD', 'LINE_PAY', 'GIFT_CARD', 'OTHER'];

const RefundPage: React.FC = () => {
  const [form, setForm] = useState<Partial<CreateRefundRequest>>({ refundMethod: 'CASH' });
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
    setLoading(true);
    setError('');
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
    <Box sx={{ p: 3, maxWidth: 600 }}>
      <Typography variant="h5" fontWeight="bold" mb={3}>退款申請</Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {result ? (
        // 退款結果 / Refund result
        <Card>
          <CardContent>
            <Typography variant="h6" mb={1}>退款申請已建立</Typography>
            <Typography>退款單號：<strong>{result.refundNo}</strong></Typography>
            <Typography>狀態：<strong>{result.status}</strong></Typography>
            <Typography>退款金額：<strong>NT$ {result.refundAmount.toLocaleString()}</strong></Typography>
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
        <Card>
          <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              fullWidth label="訂單 ID" required
              value={form.orderId ?? ''}
              onChange={e => update('orderId', e.target.value)}
            />

            <TextField
              fullWidth label="退款金額" required type="number"
              InputProps={{ startAdornment: <InputAdornment position="start">NT$</InputAdornment> }}
              value={form.refundAmount ?? ''}
              onChange={e => update('refundAmount', parseFloat(e.target.value))}
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
              fullWidth label="審批人 UUID（選填）"
              value={form.approvedBy ?? ''}
              onChange={e => update('approvedBy', e.target.value || undefined)}
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
