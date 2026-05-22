/**
 * @file UserTable.test.tsx
 * @description 帳號管理表格測試 / User management table tests
 * @description_en Verifies account management rendering and guarded status updates
 * @description_zh 驗證帳號管理列表呈現與狀態更新確認流程
 */
import { ThemeProvider } from '@mui/material';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { UserTable } from '../../src/features/auth/components/UserTable';
import { roleApi, userApi } from '../../src/features/auth/api/authApi';
import { createPosTheme } from '../../src/shared/theme';
import type { PageResponse, Role, UserRoleSummary } from '../../src/features/auth/types';

vi.mock('../../src/features/auth/api/authApi', () => ({
  roleApi: {
    getRoles: vi.fn(),
  },
  userApi: {
    getRoleSummaries: vi.fn(),
    create: vi.fn(),
    updateRoles: vi.fn(),
    updateStatus: vi.fn(),
    resetPassword: vi.fn(),
  },
}));

const mockRoleApi = vi.mocked(roleApi);
const mockUserApi = vi.mocked(userApi);

const cashierRole: Role = {
  id: '10000000-0000-0000-0000-000000000001',
  name: '收銀員',
  code: 'CASHIER',
  description: '門市收銀操作',
};

const lockedUser: UserRoleSummary = {
  user: {
    id: '20000000-0000-0000-0000-000000000001',
    username: 'cashier.demo',
    email: 'cashier@example.test',
    phone: '0912***123',
    status: 'ACTIVE',
    failedAttempts: 5,
    lockedUntil: '2999-01-01T00:00:00',
    mfaEnabled: false,
    createdAt: '2026-05-22T09:00:00',
  },
  roles: [cashierRole],
};

const userPage: PageResponse<UserRoleSummary> = {
  content: [lockedUser],
  pageNumber: 0,
  pageSize: 10,
  totalElements: 1,
  totalPages: 1,
  last: true,
};

const renderUserTable = () => render(
  <ThemeProvider theme={createPosTheme('light')}>
    <UserTable />
  </ThemeProvider>
);

beforeEach(() => {
  vi.clearAllMocks();
  mockRoleApi.getRoles.mockResolvedValue([cashierRole]);
  mockUserApi.getRoleSummaries.mockResolvedValue(userPage);
  mockUserApi.updateStatus.mockResolvedValue({
    ...lockedUser,
    user: {
      ...lockedUser.user,
      lockedUntil: null,
    },
  });
});

describe('UserTable', () => {
  it('renders locked account state from lockedUntil metadata', async () => {
    renderUserTable();

    expect(await screen.findByText('cashier.demo')).toBeInTheDocument();
    expect(screen.getByText('鎖定')).toBeInTheDocument();
    expect(screen.getByText('失敗 5 次')).toBeInTheDocument();
  });

  it('keeps create action disabled until required fields are filled', async () => {
    const user = userEvent.setup();
    renderUserTable();

    await screen.findByText('cashier.demo');
    await user.click(screen.getByRole('button', { name: '新增帳號' }));

    const dialog = screen.getByRole('dialog', { name: '新增帳號' });
    const createButton = within(dialog).getByRole('button', { name: '建立帳號' });
    expect(createButton).toBeDisabled();

    await user.type(within(dialog).getByLabelText('帳號 *'), 'manager.demo');
    await user.type(within(dialog).getByLabelText('初始密碼 *'), 'secret123');

    expect(createButton).not.toBeDisabled();
  });

  it('confirms before unlocking a locked account', async () => {
    const user = userEvent.setup();
    renderUserTable();

    await screen.findByText('cashier.demo');
    await user.click(screen.getByRole('button', { name: '解除鎖定 cashier.demo' }));
    await user.click(screen.getByRole('button', { name: '解除鎖定' }));

    await waitFor(() => {
      expect(mockUserApi.updateStatus).toHaveBeenCalledWith(lockedUser.user.id, { status: 'ACTIVE' });
    });
  });
});
