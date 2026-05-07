/**
 * @file GatewayConfigPage.tsx
 * @description 閘道配置頁 / Payment gateway configuration page
 * @description_en Display gateway configs per store; real API wired in Phase 3
 * @description_zh 顯示門店閘道設定，Phase 3 再串接真實閘道 API
 */
import React from 'react';
import {
  Box, Typography, Table, TableHead, TableRow, TableCell, TableBody,
  Chip, Alert,
} from '@mui/material';
import type { GatewayType } from '../types';

// ========================================
// 閘道類型標籤配色 / Gateway type color mapping
// ========================================
const GATEWAY_COLOR: Record<GatewayType, 'default' | 'success' | 'primary' | 'warning' | 'info' | 'secondary'> = {
  CASH: 'success',
  MOCK_CARD: 'primary',
  LINE_PAY: 'info',
  JKOPAY: 'warning',
  TAIWAN_PAY: 'secondary',
};

interface GatewayRow {
  id: string;
  gatewayType: GatewayType;
  displayName: string;
  isSandbox: boolean;
  isActive: boolean;
}

// ========================================
// 預設閘道列表（Phase 1 靜態展示）/ Default gateway list (Phase 1 static display)
// ========================================
const DEFAULT_GATEWAYS: GatewayRow[] = [
  { id: '1', gatewayType: 'CASH', displayName: '現金', isSandbox: false, isActive: true },
  { id: '2', gatewayType: 'MOCK_CARD', displayName: '模擬刷卡（測試）', isSandbox: true, isActive: true },
  { id: '3', gatewayType: 'LINE_PAY', displayName: 'LINE Pay', isSandbox: true, isActive: false },
  { id: '4', gatewayType: 'JKOPAY', displayName: '街口支付', isSandbox: true, isActive: false },
  { id: '5', gatewayType: 'TAIWAN_PAY', displayName: '台灣 Pay', isSandbox: true, isActive: false },
];

// ========================================
// 閘道配置頁 / Gateway config page
// ========================================
const GatewayConfigPage: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" fontWeight="bold" sx={{ mb: 2 }}>閘道配置</Typography>

      <Alert severity="info" sx={{ mb: 3 }}>
        Phase 1 僅啟用現金與模擬刷卡閘道。LINE Pay、街口、台灣 Pay 將於 Phase 3 依客戶需求串接。
      </Alert>

      {/* ======================================== */}
      {/* 閘道列表 / Gateway list table */}
      {/* ======================================== */}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>閘道類型</TableCell>
            <TableCell>顯示名稱</TableCell>
            <TableCell>環境</TableCell>
            <TableCell>狀態</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {DEFAULT_GATEWAYS.map((gw) => (
            <TableRow key={gw.id}>
              <TableCell>
                <Chip label={gw.gatewayType} color={GATEWAY_COLOR[gw.gatewayType]} size="small" />
              </TableCell>
              <TableCell>{gw.displayName}</TableCell>
              <TableCell>
                <Chip
                  label={gw.isSandbox ? 'Sandbox' : '正式'}
                  color={gw.isSandbox ? 'warning' : 'success'}
                  size="small"
                  variant="outlined"
                />
              </TableCell>
              <TableCell>
                <Chip
                  label={gw.isActive ? '啟用' : '未啟用'}
                  color={gw.isActive ? 'success' : 'default'}
                  size="small"
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Box>
  );
};

export default GatewayConfigPage;
