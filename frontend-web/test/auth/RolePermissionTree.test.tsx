/**
 * @file RolePermissionTree.test.tsx
 * @description 角色權限管理測試 / Role permission management tests
 * @description_en Verifies custom role creation from the RBAC management page
 * @description_zh 驗證 RBAC 管理頁可建立自訂角色
 */
import { ThemeProvider } from '@mui/material';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { RolePermissionTree } from '../../src/features/auth/components/RolePermissionTree';
import { roleApi } from '../../src/features/auth/api/authApi';
import { createPosTheme } from '../../src/shared/theme';
import type { Permission, RolePermissionSummary } from '../../src/features/auth/types';

vi.mock('../../src/features/auth/api/authApi', () => ({
  roleApi: {
    getPermissionSummaries: vi.fn(),
    getPermissions: vi.fn(),
    create: vi.fn(),
    updatePermissions: vi.fn(),
  },
}));

const mockRoleApi = vi.mocked(roleApi);

const refundPermission: Permission = {
  id: '10000000-0000-0000-0000-000000000001',
  name: '退款處理',
  code: 'pos:order:refund',
  type: 'POS',
  resource: 'order',
  action: 'refund',
};

const cashierRole: RolePermissionSummary = {
  id: '20000000-0000-0000-0000-000000000001',
  name: '收銀員',
  code: 'CASHIER',
  description: '門市收銀操作',
  permissions: [],
};

const shiftManagerRole: RolePermissionSummary = {
  id: '20000000-0000-0000-0000-000000000002',
  name: '班長',
  code: 'SHIFT_MANAGER',
  description: '門市班長',
  permissions: [refundPermission],
};

const renderRolePermissionTree = () => render(
  <ThemeProvider theme={createPosTheme('dark')}>
    <RolePermissionTree />
  </ThemeProvider>
);

beforeEach(() => {
  vi.clearAllMocks();
  mockRoleApi.getPermissionSummaries.mockResolvedValue([cashierRole]);
  mockRoleApi.getPermissions.mockResolvedValue([refundPermission]);
  mockRoleApi.create.mockResolvedValue(shiftManagerRole);
});

describe('RolePermissionTree', () => {
  it('creates a custom role from the role permission page', async () => {
    const user = userEvent.setup();
    renderRolePermissionTree();

    expect(await screen.findByText('現有角色權限架構')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: '新增角色' }));

    const dialog = screen.getByRole('dialog', { name: '新增角色 建立後可立即指派給帳號，也能在此先勾選初始權限。' });
    const createButton = within(dialog).getByRole('button', { name: '建立角色' });
    expect(createButton).toBeDisabled();

    await user.type(within(dialog).getByLabelText('角色名稱 *'), '班長');
    await user.type(within(dialog).getByLabelText('角色代碼 *'), 'shift_manager');
    await user.type(within(dialog).getByLabelText('角色描述'), '門市班長');
    await user.click(within(dialog).getByLabelText('退款處理 pos:order:refund'));
    await user.click(createButton);

    await waitFor(() => {
      expect(mockRoleApi.create).toHaveBeenCalledWith({
        name: '班長',
        code: 'SHIFT_MANAGER',
        description: '門市班長',
        permissionIds: [refundPermission.id],
      });
    });
    expect(await screen.findByText('已建立 班長 角色。')).toBeInTheDocument();
    expect(screen.getByText('SHIFT_MANAGER')).toBeInTheDocument();
  });
});
