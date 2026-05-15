/**
 * @file PromotionRulePage.tsx
 * @description POS 促銷規則頁 / POS promotion rule page
 * @description_en Manage store promotion rules and evaluate order-level discounts
 * @description_zh 管理門店促銷規則並試算訂單層級折扣
 */
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
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
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import { getActivePosContext } from '../../pos-orders/posSession';
import { formatMoney } from '@shared/utils';
import { promotionApi } from '../api/promotionApi';
import type {
  PromotionDiscountType,
  PromotionEvaluationResult,
  PromotionRule,
  PromotionRuleRequest,
  PromotionTriggerType,
} from '../types';

const TRIGGER_LABELS: Record<PromotionTriggerType, string> = {
  AUTO: '自動套用',
  CODE: '優惠碼',
};

const DISCOUNT_LABELS: Record<PromotionDiscountType, string> = {
  PERCENT: '百分比',
  AMOUNT: '固定金額',
};

const createInitialForm = (storeId: string): PromotionRuleRequest => ({
  storeId,
  name: '',
  code: '',
  triggerType: 'AUTO',
  discountType: 'PERCENT',
  discountValue: 10,
  minimumSubtotal: 0,
  maxDiscountAmount: null,
  active: true,
});

const formatDiscount = (rule: PromotionRule) => {
  if (rule.discountType === 'PERCENT') {
    return `${rule.discountValue}%${rule.maxDiscountAmount ? `，最高 ${formatMoney(rule.maxDiscountAmount)}` : ''}`;
  }
  return formatMoney(rule.discountValue);
};

// ========================================
// 促銷規則頁 / Promotion rule page
// ========================================
const PromotionRulePage: React.FC = () => {
  const posContext = useMemo(() => getActivePosContext(), []);
  const [rules, setRules] = useState<PromotionRule[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState<PromotionRuleRequest>(() => createInitialForm(posContext.storeId));
  const [evaluateSubtotal, setEvaluateSubtotal] = useState('145');
  const [evaluateCode, setEvaluateCode] = useState('');
  const [evaluation, setEvaluation] = useState<PromotionEvaluationResult | null>(null);

  const loadRules = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const res = await promotionApi.list(posContext.storeId);
      setRules(res.data ?? []);
    } catch {
      setError('載入促銷規則失敗');
    } finally {
      setLoading(false);
    }
  }, [posContext.storeId]);

  useEffect(() => { loadRules(); }, [loadRules]);

  const handleOpenCreate = () => {
    setForm(createInitialForm(posContext.storeId));
    setDialogOpen(true);
  };

  const handleCreate = async () => {
    if (!form.name.trim()) return;
    setSubmitting(true);
    try {
      const payload: PromotionRuleRequest = {
        ...form,
        code: form.triggerType === 'CODE' ? form.code?.trim().toUpperCase() : null,
        maxDiscountAmount: form.maxDiscountAmount || null,
      };
      await promotionApi.create(payload);
      setDialogOpen(false);
      await loadRules();
    } catch {
      setError('建立促銷規則失敗');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeactivate = async (rule: PromotionRule) => {
    if (!window.confirm(`確定停用「${rule.name}」？`)) return;
    try {
      await promotionApi.deactivate(rule.id);
      await loadRules();
    } catch {
      setError('停用促銷規則失敗');
    }
  };

  const handleEvaluate = async () => {
    try {
      setError('');
      const res = await promotionApi.evaluate({
        storeId: posContext.storeId,
        subtotal: Number(evaluateSubtotal) || 0,
        code: evaluateCode.trim() || null,
      });
      setEvaluation(res.data);
    } catch {
      setError('促銷試算失敗');
    }
  };

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>;
  }

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2, mb: 2 }}>
        <Box>
          <Typography variant="h5" fontWeight={900}>促銷規則</Typography>
          <Typography color="text.secondary">門店：{posContext.storeId}</Typography>
        </Box>
        <Button variant="contained" startIcon={<LocalOfferIcon />} onClick={handleOpenCreate}>
          新增促銷
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      <Paper sx={{ p: 2, mb: 3, bgcolor: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.08)' }}>
        <Typography variant="h6" fontWeight={900} sx={{ mb: 2 }}>訂單促銷試算</Typography>
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '180px 180px auto 1fr' }, gap: 2, alignItems: 'center' }}>
          <TextField
            type="number"
            label="訂單小計"
            value={evaluateSubtotal}
            onChange={(e) => setEvaluateSubtotal(e.target.value)}
            inputProps={{ min: 0, step: 1 }}
          />
          <TextField
            label="優惠碼"
            value={evaluateCode}
            onChange={(e) => setEvaluateCode(e.target.value)}
            placeholder="例如 CAFE20"
          />
          <Button variant="outlined" onClick={handleEvaluate}>試算</Button>
          {evaluation && (
            <Alert severity={evaluation.applied ? 'success' : 'info'} sx={{ py: 0.5 }}>
              {evaluation.applied
                ? `${evaluation.name}，折抵 ${formatMoney(evaluation.discountAmount)}`
                : '沒有可套用促銷'}
            </Alert>
          )}
        </Box>
      </Paper>

      <Table>
        <TableHead>
          <TableRow>
            <TableCell>名稱</TableCell>
            <TableCell>觸發</TableCell>
            <TableCell>折扣</TableCell>
            <TableCell>最低小計</TableCell>
            <TableCell>狀態</TableCell>
            <TableCell align="right">操作</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rules.map((rule) => (
            <TableRow key={rule.id}>
              <TableCell>
                <Typography fontWeight={900}>{rule.name}</Typography>
                {rule.code && <Typography variant="caption" color="text.secondary">{rule.code}</Typography>}
              </TableCell>
              <TableCell>
                <Chip label={TRIGGER_LABELS[rule.triggerType]} size="small" color={rule.triggerType === 'AUTO' ? 'success' : 'warning'} />
              </TableCell>
              <TableCell>{DISCOUNT_LABELS[rule.discountType]} · {formatDiscount(rule)}</TableCell>
              <TableCell>{formatMoney(rule.minimumSubtotal)}</TableCell>
              <TableCell>
                <Chip label={rule.active ? '啟用' : '停用'} color={rule.active ? 'success' : 'default'} size="small" />
              </TableCell>
              <TableCell align="right">
                <Button size="small" color="error" onClick={() => handleDeactivate(rule)}>
                  停用
                </Button>
              </TableCell>
            </TableRow>
          ))}
          {rules.length === 0 && (
            <TableRow>
              <TableCell colSpan={6} align="center">尚無促銷規則</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>新增促銷規則</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField
            label="促銷名稱"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
          />
          <TextField
            select
            label="觸發方式"
            value={form.triggerType}
            onChange={(e) => setForm({
              ...form,
              triggerType: e.target.value as PromotionTriggerType,
              code: e.target.value === 'AUTO' ? '' : form.code,
            })}
          >
            <MenuItem value="AUTO">自動套用</MenuItem>
            <MenuItem value="CODE">優惠碼</MenuItem>
          </TextField>
          {form.triggerType === 'CODE' && (
            <TextField
              label="優惠碼"
              value={form.code ?? ''}
              onChange={(e) => setForm({ ...form, code: e.target.value })}
              required
            />
          )}
          <TextField
            select
            label="折扣類型"
            value={form.discountType}
            onChange={(e) => setForm({ ...form, discountType: e.target.value as PromotionDiscountType })}
          >
            <MenuItem value="PERCENT">百分比</MenuItem>
            <MenuItem value="AMOUNT">固定金額</MenuItem>
          </TextField>
          <TextField
            type="number"
            label={form.discountType === 'PERCENT' ? '折扣百分比' : '折扣金額'}
            value={form.discountValue}
            onChange={(e) => setForm({ ...form, discountValue: Number(e.target.value) })}
            inputProps={{ min: 0.01, max: form.discountType === 'PERCENT' ? 100 : undefined, step: 0.01 }}
            required
          />
          <TextField
            type="number"
            label="最低訂單小計"
            value={form.minimumSubtotal}
            onChange={(e) => setForm({ ...form, minimumSubtotal: Number(e.target.value) })}
            inputProps={{ min: 0, step: 1 }}
          />
          <TextField
            type="number"
            label="最高折抵金額"
            value={form.maxDiscountAmount ?? ''}
            onChange={(e) => setForm({ ...form, maxDiscountAmount: e.target.value ? Number(e.target.value) : null })}
            inputProps={{ min: 0, step: 1 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleCreate} disabled={submitting || !form.name.trim()}>
            {submitting ? '建立中...' : '建立'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default PromotionRulePage;
