/**
 * @file MemberPage.tsx
 * @description 會員與點數管理頁 / Member and loyalty points management page
 * @description_en Provides member search, quick registration, point ledger and point mutation actions
 * @description_zh 提供會員搜尋、快速註冊、點數流水與點數異動操作
 */
import React, { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  IconButton,
  MenuItem,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import LoyaltyIcon from '@mui/icons-material/Loyalty';
import RefreshIcon from '@mui/icons-material/Refresh';
import SearchIcon from '@mui/icons-material/Search';
import { DataTable, PageHeader } from '@shared/components';
import { formatMoney, formatNumber } from '@shared/utils';
import { memberApi } from '../api/memberApi';
import type { Member, MemberRequest, MemberTier, PointLedger, PointReason } from '../types';

const TIER_OPTIONS: { value: MemberTier; label: string }[] = [
  { value: 'BRONZE', label: '一般' },
  { value: 'SILVER', label: '銀卡' },
  { value: 'GOLD', label: '金卡' },
  { value: 'PLATINUM', label: '白金' },
];

const TIER_COLOR: Record<MemberTier, 'default' | 'info' | 'warning' | 'secondary'> = {
  BRONZE: 'default',
  SILVER: 'info',
  GOLD: 'warning',
  PLATINUM: 'secondary',
};

const REASON_LABEL: Record<PointReason, string> = {
  ORDER_EARN: '消費累點',
  MANUAL_ADJUST: '手動調整',
  REDEEM: '點數兌換',
  REFUND_REVERSE: '退款回沖',
  EXPIRE: '點數到期',
};

interface MemberFormState {
  memberNo: string;
  name: string;
  phone: string;
  email: string;
  birthday: string;
  cardNo: string;
  barcode: string;
  tier: MemberTier;
  discountPercent: string;
}

interface PointDialogState {
  mode: 'adjust' | 'redeem';
  amount: string;
  orderId: string;
  note: string;
}

const createEmptyMemberForm = (): MemberFormState => ({
  memberNo: '',
  name: '',
  phone: '',
  email: '',
  birthday: '',
  cardNo: '',
  barcode: '',
  tier: 'BRONZE',
  discountPercent: '',
});

const trimOrUndefined = (value: string) => {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : undefined;
};

const formatDateTime = (value: string) =>
  new Intl.DateTimeFormat('zh-TW', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));

const buildMemberRequest = (form: MemberFormState): MemberRequest => ({
  memberNo: trimOrUndefined(form.memberNo),
  name: form.name.trim(),
  phone: form.phone.trim(),
  email: trimOrUndefined(form.email),
  birthday: trimOrUndefined(form.birthday),
  cardNo: trimOrUndefined(form.cardNo),
  barcode: trimOrUndefined(form.barcode),
  tier: form.tier,
  discountPercent: form.discountPercent.trim() ? Number(form.discountPercent) : undefined,
});

// ========================================
// 會員管理頁 / Member management page
// ========================================
const MemberPage: React.FC = () => {
  const [query, setQuery] = useState('');
  const [members, setMembers] = useState<Member[]>([]);
  const [selectedMember, setSelectedMember] = useState<Member | null>(null);
  const [ledgers, setLedgers] = useState<PointLedger[]>([]);
  const [loading, setLoading] = useState(false);
  const [ledgerLoading, setLedgerLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [memberDialogOpen, setMemberDialogOpen] = useState(false);
  const [memberForm, setMemberForm] = useState<MemberFormState>(createEmptyMemberForm);
  const [pointDialog, setPointDialog] = useState<PointDialogState | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const visibleLedgers = useMemo(() => ledgers.slice(0, 20), [ledgers]);

  const loadMembers = async (searchQuery = query) => {
    const normalizedQuery = searchQuery.trim();
    if (!normalizedQuery) {
      setMembers([]);
      setSelectedMember(null);
      setLedgers([]);
      return;
    }

    setLoading(true);
    setError('');
    try {
      const res = await memberApi.search(normalizedQuery, 20);
      const rows = res.data ?? [];
      setMembers(rows);
      if (selectedMember && !rows.some((member) => member.id === selectedMember.id)) {
        setSelectedMember(null);
        setLedgers([]);
      }
    } catch {
      setError('會員搜尋失敗');
    } finally {
      setLoading(false);
    }
  };

  const loadLedgers = async (member: Member) => {
    setLedgerLoading(true);
    setError('');
    try {
      const [memberResponse, ledgerResponse] = await Promise.all([
        memberApi.getById(member.id),
        memberApi.listPointLedgers(member.id),
      ]);
      const freshMember = memberResponse.data;
      setSelectedMember(freshMember);
      setLedgers(ledgerResponse.data ?? []);
      setMembers((prev) => prev.map((row) => (row.id === freshMember.id ? freshMember : row)));
    } catch {
      setError('載入會員點數流水失敗');
    } finally {
      setLedgerLoading(false);
    }
  };

  useEffect(() => {
    if (members.length > 0 && !selectedMember) {
      loadLedgers(members[0]);
    }
  }, [members, selectedMember]);

  const openCreateDialog = () => {
    setMemberForm(createEmptyMemberForm());
    setMemberDialogOpen(true);
  };

  const closeCreateDialog = () => {
    if (submitting) return;
    setMemberDialogOpen(false);
    setMemberForm(createEmptyMemberForm());
  };

  const handleCreateMember = async () => {
    if (!memberForm.name.trim() || !memberForm.phone.trim()) {
      setError('會員姓名與電話必填');
      return;
    }

    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      const res = await memberApi.create(buildMemberRequest(memberForm));
      const created = res.data;
      setSuccess(`會員 ${created.name} 已建立`);
      setQuery(created.phone);
      setMemberDialogOpen(false);
      setMemberForm(createEmptyMemberForm());
      setMembers([created]);
      await loadLedgers(created);
    } catch {
      setError('建立會員失敗，請確認電話、會員編號、卡號或條碼是否重複');
    } finally {
      setSubmitting(false);
    }
  };

  const handlePointSubmit = async () => {
    if (!selectedMember || !pointDialog) return;
    const amount = Number(pointDialog.amount);
    if (!Number.isFinite(amount) || (pointDialog.mode === 'adjust' ? amount === 0 : amount <= 0)) {
      setError(pointDialog.mode === 'adjust' ? '點數異動需填入非 0 數字' : '兌換點數必須大於 0');
      return;
    }

    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      if (pointDialog.mode === 'adjust') {
        await memberApi.adjustPoints(selectedMember.id, {
          pointsDelta: amount,
          note: trimOrUndefined(pointDialog.note),
        });
        setSuccess('點數已完成手動調整');
      } else {
        await memberApi.redeemPoints(selectedMember.id, {
          points: amount,
          orderId: trimOrUndefined(pointDialog.orderId),
          note: trimOrUndefined(pointDialog.note),
        });
        setSuccess('點數已完成兌換');
      }
      setPointDialog(null);
      await loadLedgers(selectedMember);
      await loadMembers(query);
    } catch {
      setError(pointDialog.mode === 'adjust' ? '點數調整失敗' : '點數兌換失敗，請確認餘額足夠');
    } finally {
      setSubmitting(false);
    }
  };

  const memberColumns = [
    {
      key: 'memberNo',
      label: '會員編號',
      render: (row: Member) => (
        <Button color="inherit" onClick={() => loadLedgers(row)} sx={{ fontFamily: 'monospace', fontWeight: 800 }}>
          {row.memberNo}
        </Button>
      ),
    },
    { key: 'name', label: '姓名' },
    { key: 'phoneMasked', label: '電話' },
    {
      key: 'tier',
      label: '等級',
      render: (row: Member) => <Chip label={row.tierLabel} color={TIER_COLOR[row.tier]} size="small" />,
    },
    {
      key: 'pointsBalance',
      label: '點數',
      align: 'right' as const,
      render: (row: Member) => <Typography fontWeight={900}>{formatNumber(row.pointsBalance)}</Typography>,
    },
    {
      key: 'annualSpend',
      label: '年度消費',
      align: 'right' as const,
      render: (row: Member) => formatMoney(row.annualSpend),
    },
    {
      key: 'active',
      label: '狀態',
      render: (row: Member) => <Chip label={row.active ? '啟用' : '停用'} color={row.active ? 'success' : 'default'} size="small" />,
    },
  ];

  const ledgerColumns = [
    {
      key: 'occurredAt',
      label: '時間',
      render: (row: PointLedger) => formatDateTime(row.occurredAt),
    },
    {
      key: 'reason',
      label: '原因',
      render: (row: PointLedger) => <Chip label={REASON_LABEL[row.reason]} size="small" />,
    },
    {
      key: 'pointsDelta',
      label: '異動',
      align: 'right' as const,
      render: (row: PointLedger) => (
        <Typography fontWeight={900} color={row.pointsDelta >= 0 ? 'success.main' : 'error.main'}>
          {row.pointsDelta >= 0 ? '+' : ''}{formatNumber(row.pointsDelta)}
        </Typography>
      ),
    },
    {
      key: 'balanceAfter',
      label: '結餘',
      align: 'right' as const,
      render: (row: PointLedger) => formatNumber(row.balanceAfter),
    },
    {
      key: 'referenceType',
      label: '來源',
      render: (row: PointLedger) => row.referenceType ?? '-',
    },
    {
      key: 'note',
      label: '備註',
      render: (row: PointLedger) => row.note ?? '-',
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <PageHeader
        title="會員管理"
        subtitle="搜尋會員、快速註冊並追蹤忠誠點數流水。"
        actions={(
          <>
            <Tooltip title="重新整理">
              <span>
                <IconButton
                  aria-label="重新整理會員資料"
                  onClick={() => selectedMember ? loadLedgers(selectedMember) : loadMembers(query)}
                  disabled={loading || ledgerLoading}
                >
                  <RefreshIcon />
                </IconButton>
              </span>
            </Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={openCreateDialog}>
              新增會員
            </Button>
          </>
        )}
      />

      <Stack direction={{ xs: 'column', md: 'row' }} spacing={1.5} sx={{ mb: 2 }}>
        <TextField
          label="搜尋會員"
          placeholder="輸入姓名、電話、會員編號、卡號或條碼"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onKeyDown={(event) => event.key === 'Enter' && loadMembers()}
          sx={{ flex: 1 }}
        />
        <Button
          variant="contained"
          startIcon={<SearchIcon />}
          onClick={() => loadMembers()}
          disabled={loading}
          sx={{ minHeight: 56, px: 3 }}
        >
          搜尋
        </Button>
      </Stack>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', xl: 'minmax(0, 1.35fr) minmax(360px, 0.65fr)' }, gap: 2.5 }}>
        <DataTable
          columns={memberColumns}
          rows={members}
          loading={loading}
          emptyMessage={query.trim() ? '找不到符合條件的會員' : '輸入關鍵字後開始搜尋會員'}
          rowKey={(row) => row.id}
        />

        <Card sx={{ bgcolor: 'background.paper', border: '1px solid rgba(255,255,255,0.06)', boxShadow: 'none' }}>
          <CardContent>
            {selectedMember ? (
              <Stack spacing={2}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2, alignItems: 'flex-start' }}>
                  <Box>
                    <Typography variant="h6" fontWeight={900}>{selectedMember.name}</Typography>
                    <Typography color="text.secondary" fontFamily="monospace">{selectedMember.memberNo}</Typography>
                  </Box>
                  <Chip label={selectedMember.tierLabel} color={TIER_COLOR[selectedMember.tier]} />
                </Box>

                <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 1.5 }}>
                  <Box>
                    <Typography variant="caption" color="text.secondary">電話</Typography>
                    <Typography fontWeight={800}>{selectedMember.phoneMasked}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">折扣</Typography>
                    <Typography fontWeight={800}>{Number(selectedMember.discountPercent).toFixed(2)}%</Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">點數餘額</Typography>
                    <Typography variant="h5" fontWeight={900} color="secondary.main">{formatNumber(selectedMember.pointsBalance)}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">年度消費</Typography>
                    <Typography variant="h5" fontWeight={900}>{formatMoney(selectedMember.annualSpend)}</Typography>
                  </Box>
                </Box>

                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<LoyaltyIcon />}
                    onClick={() => setPointDialog({ mode: 'adjust', amount: '', orderId: '', note: '' })}
                  >
                    調整點數
                  </Button>
                  <Button
                    fullWidth
                    variant="outlined"
                    color="secondary"
                    startIcon={<LoyaltyIcon />}
                    onClick={() => setPointDialog({ mode: 'redeem', amount: '', orderId: '', note: '' })}
                  >
                    兌換點數
                  </Button>
                </Stack>

                <Divider />
                <Typography fontWeight={900}>最近點數流水</Typography>
                <DataTable
                  columns={ledgerColumns}
                  rows={visibleLedgers}
                  loading={ledgerLoading}
                  emptyMessage="目前沒有點數流水"
                  rowKey={(row) => row.id}
                />
              </Stack>
            ) : (
              <Box sx={{ py: 8, textAlign: 'center', color: 'text.secondary' }}>
                <Typography fontWeight={900} color="text.primary">尚未選擇會員</Typography>
                <Typography variant="body2">搜尋並點選會員編號後，可查看點數與流水。</Typography>
              </Box>
            )}
          </CardContent>
        </Card>
      </Box>

      <Dialog open={memberDialogOpen} onClose={closeCreateDialog} maxWidth="md" fullWidth>
        <DialogTitle>新增會員</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)' }, gap: 2 }}>
            <TextField label="會員姓名" required value={memberForm.name} onChange={(event) => setMemberForm({ ...memberForm, name: event.target.value })} />
            <TextField label="電話" required value={memberForm.phone} onChange={(event) => setMemberForm({ ...memberForm, phone: event.target.value })} />
            <TextField label="會員編號（選填）" value={memberForm.memberNo} onChange={(event) => setMemberForm({ ...memberForm, memberNo: event.target.value })} />
            <TextField label="Email（選填）" value={memberForm.email} onChange={(event) => setMemberForm({ ...memberForm, email: event.target.value })} />
            <TextField type="date" label="生日（選填）" value={memberForm.birthday} onChange={(event) => setMemberForm({ ...memberForm, birthday: event.target.value })} InputLabelProps={{ shrink: true }} />
            <TextField select label="會員等級" value={memberForm.tier} onChange={(event) => setMemberForm({ ...memberForm, tier: event.target.value as MemberTier })}>
              {TIER_OPTIONS.map((option) => <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>)}
            </TextField>
            <TextField label="卡號（選填）" value={memberForm.cardNo} onChange={(event) => setMemberForm({ ...memberForm, cardNo: event.target.value })} />
            <TextField label="條碼（選填）" value={memberForm.barcode} onChange={(event) => setMemberForm({ ...memberForm, barcode: event.target.value })} />
            <TextField
              type="number"
              label="折扣百分比（選填）"
              value={memberForm.discountPercent}
              onChange={(event) => setMemberForm({ ...memberForm, discountPercent: event.target.value })}
              inputProps={{ min: 0, step: 0.01 }}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeCreateDialog}>取消</Button>
          <Button variant="contained" onClick={handleCreateMember} disabled={submitting}>
            {submitting ? '建立中...' : '建立會員'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(pointDialog)} onClose={() => !submitting && setPointDialog(null)} maxWidth="sm" fullWidth>
        <DialogTitle>{pointDialog?.mode === 'adjust' ? '調整會員點數' : '兌換會員點數'}</DialogTitle>
        {pointDialog && (
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
            <TextField
              type="number"
              label={pointDialog.mode === 'adjust' ? '點數異動（可輸入負數）' : '兌換點數'}
              value={pointDialog.amount}
              onChange={(event) => setPointDialog({ ...pointDialog, amount: event.target.value })}
              inputProps={{ step: 1 }}
              required
            />
            {pointDialog.mode === 'redeem' && (
              <TextField
                label="訂單 ID（選填）"
                value={pointDialog.orderId}
                onChange={(event) => setPointDialog({ ...pointDialog, orderId: event.target.value })}
              />
            )}
            <TextField
              label="備註"
              value={pointDialog.note}
              onChange={(event) => setPointDialog({ ...pointDialog, note: event.target.value })}
              multiline
              minRows={2}
            />
          </DialogContent>
        )}
        <DialogActions>
          <Button onClick={() => setPointDialog(null)} disabled={submitting}>取消</Button>
          <Button variant="contained" onClick={handlePointSubmit} disabled={submitting}>
            {submitting ? '處理中...' : '確認'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default MemberPage;
