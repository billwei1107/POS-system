/**
 * @file GatewayConfigPage.tsx
 * @description 閘道配置頁 / Payment gateway configuration page
 * @description_en Manage store-level gateway configs through the payment gateway API
 * @description_zh 透過支付閘道 API 管理門店層級閘道設定
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
  FormControlLabel,
  IconButton,
  MenuItem,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import PowerSettingsNewIcon from '@mui/icons-material/PowerSettingsNew';
import RefreshIcon from '@mui/icons-material/Refresh';
import { getActivePosContext } from '../../pos-orders/posSession';
import { gatewayApi } from '../api/paymentApi';
import type {
  CreateGatewayConfigRequest,
  GatewayConfig,
  GatewayType,
  UpdateGatewayConfigRequest,
} from '../types';

const GATEWAY_TYPE_OPTIONS: { value: GatewayType; label: string }[] = [
  { value: 'CASH', label: '現金' },
  { value: 'MOCK_CARD', label: '模擬刷卡' },
  { value: 'LINE_PAY', label: 'LINE Pay' },
  { value: 'JKOPAY', label: '街口支付' },
  { value: 'TAIWAN_PAY', label: '台灣 Pay' },
];

const GATEWAY_COLOR: Record<GatewayType, 'default' | 'success' | 'primary' | 'warning' | 'info' | 'secondary'> = {
  CASH: 'success',
  MOCK_CARD: 'primary',
  LINE_PAY: 'info',
  JKOPAY: 'warning',
  TAIWAN_PAY: 'secondary',
};

interface GatewayFormState {
  gatewayType: GatewayType;
  displayName: string;
  merchantId: string;
  apiKey: string;
  apiSecret: string;
  endpointUrl: string;
  extraConfig: string;
  isSandbox: boolean;
  isActive: boolean;
}

const createEmptyForm = (): GatewayFormState => ({
  gatewayType: 'MOCK_CARD',
  displayName: '',
  merchantId: '',
  apiKey: '',
  apiSecret: '',
  endpointUrl: '',
  extraConfig: '',
  isSandbox: true,
  isActive: true,
});

const optionalText = (value: string) => {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : undefined;
};

const gatewayLabel = (gatewayType: GatewayType) =>
  GATEWAY_TYPE_OPTIONS.find((option) => option.value === gatewayType)?.label ?? gatewayType;

// ========================================
// 閘道配置頁 / Gateway config page
// ========================================
const GatewayConfigPage: React.FC = () => {
  const [gateways, setGateways] = useState<GatewayConfig[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingGateway, setEditingGateway] = useState<GatewayConfig | null>(null);
  const [form, setForm] = useState<GatewayFormState>(createEmptyForm);
  const [submitting, setSubmitting] = useState(false);
  const posContext = useMemo(() => getActivePosContext(), []);

  const usedGatewayTypes = useMemo(() => new Set(gateways.map((gateway) => gateway.gatewayType)), [gateways]);

  const selectableGatewayTypes = useMemo(
    () => GATEWAY_TYPE_OPTIONS.filter((option) => editingGateway?.gatewayType === option.value || !usedGatewayTypes.has(option.value)),
    [editingGateway?.gatewayType, usedGatewayTypes]
  );

  const loadGateways = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const res = await gatewayApi.list(posContext.storeId);
      setGateways(res.data ?? []);
    } catch {
      setError('載入閘道設定失敗');
    } finally {
      setLoading(false);
    }
  }, [posContext.storeId]);

  useEffect(() => {
    loadGateways();
  }, [loadGateways]);

  const openCreateDialog = () => {
    const firstAvailable = selectableGatewayTypes[0]?.value ?? 'MOCK_CARD';
    setEditingGateway(null);
    setForm({ ...createEmptyForm(), gatewayType: firstAvailable });
    setDialogOpen(true);
  };

  const openEditDialog = (gateway: GatewayConfig) => {
    setEditingGateway(gateway);
    setForm({
      gatewayType: gateway.gatewayType,
      displayName: gateway.displayName,
      merchantId: gateway.merchantId ?? '',
      apiKey: '',
      apiSecret: '',
      endpointUrl: gateway.endpointUrl ?? '',
      extraConfig: gateway.extraConfig ?? '',
      isSandbox: gateway.isSandbox,
      isActive: gateway.isActive,
    });
    setDialogOpen(true);
  };

  const closeDialog = () => {
    if (submitting) return;
    setDialogOpen(false);
    setEditingGateway(null);
    setForm(createEmptyForm());
  };

  const handleSubmit = async () => {
    if (!form.displayName.trim()) return;

    setSubmitting(true);
    setError('');

    try {
      if (editingGateway) {
        const req: UpdateGatewayConfigRequest = {
          displayName: form.displayName.trim(),
          merchantId: optionalText(form.merchantId),
          apiKey: optionalText(form.apiKey),
          apiSecret: optionalText(form.apiSecret),
          endpointUrl: optionalText(form.endpointUrl),
          extraConfig: optionalText(form.extraConfig),
          isSandbox: form.isSandbox,
          isActive: form.isActive,
        };
        await gatewayApi.update(editingGateway.id, req);
      } else {
        const req: CreateGatewayConfigRequest = {
          storeId: posContext.storeId,
          gatewayType: form.gatewayType,
          displayName: form.displayName.trim(),
          merchantId: optionalText(form.merchantId),
          apiKey: optionalText(form.apiKey),
          apiSecret: optionalText(form.apiSecret),
          endpointUrl: optionalText(form.endpointUrl),
          extraConfig: optionalText(form.extraConfig),
          isSandbox: form.isSandbox,
          isActive: form.isActive,
        };
        await gatewayApi.create(req);
      }
      closeDialog();
      await loadGateways();
    } catch {
      setError(editingGateway ? '更新閘道設定失敗' : '建立閘道設定失敗');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeactivate = async (gateway: GatewayConfig) => {
    if (!window.confirm(`確定停用 ${gateway.displayName}？`)) return;

    try {
      setError('');
      await gatewayApi.deactivate(gateway.id);
      await loadGateways();
    } catch {
      setError('停用閘道設定失敗');
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* ======================================== */}
      {/* 頁面標題列 / Page header */}
      {/* ======================================== */}
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        justifyContent="space-between"
        alignItems={{ xs: 'stretch', sm: 'center' }}
        spacing={2}
        sx={{ mb: 2 }}
      >
        <Typography variant="h5" fontWeight="bold">閘道配置</Typography>
        <Stack direction="row" spacing={1} justifyContent="flex-end">
          <Tooltip title="重新整理">
            <IconButton aria-label="重新整理閘道設定" onClick={loadGateways}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={openCreateDialog}
            disabled={selectableGatewayTypes.length === 0}
          >
            新增閘道
          </Button>
        </Stack>
      </Stack>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      {/* ======================================== */}
      {/* 閘道列表 / Gateway list table */}
      {/* ======================================== */}
      <TableContainer sx={{ overflowX: 'auto' }}>
        <Table sx={{ minWidth: 920 }}>
          <TableHead>
            <TableRow>
              <TableCell>閘道類型</TableCell>
              <TableCell>顯示名稱</TableCell>
              <TableCell>商店代號</TableCell>
              <TableCell>端點</TableCell>
              <TableCell>密鑰</TableCell>
              <TableCell>環境</TableCell>
              <TableCell>狀態</TableCell>
              <TableCell align="right">操作</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {gateways.map((gateway) => (
              <TableRow key={gateway.id}>
                <TableCell>
                  <Chip
                    label={gatewayLabel(gateway.gatewayType)}
                    color={GATEWAY_COLOR[gateway.gatewayType]}
                    size="small"
                  />
                </TableCell>
                <TableCell>{gateway.displayName}</TableCell>
                <TableCell>{gateway.merchantId || '-'}</TableCell>
                <TableCell>{gateway.endpointUrl || '-'}</TableCell>
                <TableCell>
                  <Stack direction="row" spacing={1}>
                    <Chip
                      label={gateway.apiKeyConfigured ? 'Key 已設定' : 'Key 未設定'}
                      color={gateway.apiKeyConfigured ? 'success' : 'default'}
                      size="small"
                      variant="outlined"
                    />
                    <Chip
                      label={gateway.apiSecretConfigured ? 'Secret 已設定' : 'Secret 未設定'}
                      color={gateway.apiSecretConfigured ? 'success' : 'default'}
                      size="small"
                      variant="outlined"
                    />
                  </Stack>
                </TableCell>
                <TableCell>
                  <Chip
                    label={gateway.isSandbox ? 'Sandbox' : '正式'}
                    color={gateway.isSandbox ? 'warning' : 'success'}
                    size="small"
                    variant="outlined"
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={gateway.isActive ? '啟用' : '停用'}
                    color={gateway.isActive ? 'success' : 'default'}
                    size="small"
                  />
                </TableCell>
                <TableCell align="right">
                  <Tooltip title="編輯">
                    <IconButton aria-label={`編輯 ${gateway.displayName}`} onClick={() => openEditDialog(gateway)}>
                      <EditIcon />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="停用">
                    <span>
                      <IconButton
                        aria-label={`停用 ${gateway.displayName}`}
                        color="error"
                        onClick={() => handleDeactivate(gateway)}
                        disabled={!gateway.isActive}
                      >
                        <PowerSettingsNewIcon />
                      </IconButton>
                    </span>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
            {gateways.length === 0 && (
              <TableRow>
                <TableCell colSpan={8} align="center">尚無閘道設定</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* ======================================== */}
      {/* 閘道設定對話框 / Gateway config dialog */}
      {/* ======================================== */}
      <Dialog open={dialogOpen} onClose={closeDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingGateway ? '編輯閘道' : '新增閘道'}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField
            select
            label="閘道類型"
            value={form.gatewayType}
            onChange={(event) => setForm({ ...form, gatewayType: event.target.value as GatewayType })}
            disabled={Boolean(editingGateway)}
          >
            {selectableGatewayTypes.map((option) => (
              <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>
            ))}
          </TextField>
          <TextField
            label="顯示名稱"
            value={form.displayName}
            onChange={(event) => setForm({ ...form, displayName: event.target.value })}
            required
          />
          <TextField
            label="商店代號"
            value={form.merchantId}
            onChange={(event) => setForm({ ...form, merchantId: event.target.value })}
          />
          <TextField
            label="API Key"
            value={form.apiKey}
            onChange={(event) => setForm({ ...form, apiKey: event.target.value })}
            helperText={editingGateway ? '留空保留既有值' : undefined}
          />
          <TextField
            label="API Secret"
            type="password"
            value={form.apiSecret}
            onChange={(event) => setForm({ ...form, apiSecret: event.target.value })}
            helperText={editingGateway ? '留空保留既有值' : undefined}
          />
          <TextField
            label="端點 URL"
            value={form.endpointUrl}
            onChange={(event) => setForm({ ...form, endpointUrl: event.target.value })}
          />
          <TextField
            label="額外設定 JSON"
            value={form.extraConfig}
            onChange={(event) => setForm({ ...form, extraConfig: event.target.value })}
            multiline
            minRows={3}
          />
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <FormControlLabel
              control={
                <Switch
                  checked={form.isSandbox}
                  onChange={(event) => setForm({ ...form, isSandbox: event.target.checked })}
                />
              }
              label="Sandbox"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={form.isActive}
                  onChange={(event) => setForm({ ...form, isActive: event.target.checked })}
                />
              }
              label="啟用"
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeDialog} disabled={submitting}>取消</Button>
          <Button variant="contained" onClick={handleSubmit} disabled={submitting || !form.displayName.trim()}>
            {submitting ? '儲存中...' : '儲存'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default GatewayConfigPage;
