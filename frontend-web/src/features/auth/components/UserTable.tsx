import { useCallback, useEffect, useMemo, useState } from 'react';
import {
    Alert,
    Box,
    Button,
    Checkbox,
    Chip,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormControlLabel,
    InputLabel,
    MenuItem,
    TextField,
    Paper,
    Pagination,
    Select,
    Stack,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Tooltip,
    Typography,
    IconButton,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import ClearIcon from '@mui/icons-material/Clear';
import EditIcon from '@mui/icons-material/Edit';
import LockResetIcon from '@mui/icons-material/LockReset';
import PowerSettingsNewIcon from '@mui/icons-material/PowerSettingsNew';
import SaveIcon from '@mui/icons-material/Save';
import SearchIcon from '@mui/icons-material/Search';
import { roleApi, userApi } from '../api/authApi';
import type { Role, UserRoleSummary } from '../types';

/**
 * @file UserTable.tsx
 * @description 使用者資料表格組件 / User datatable component
 * @description_en Displays account role assignments and allows admin role updates
 * @description_zh 顯示帳號角色指派，並提供後台更新角色功能
 */
const statusLabels: Record<string, string> = {
    ACTIVE: '啟用',
    INACTIVE: '停用',
    LOCKED: '鎖定',
};

const roleLabel = (role: Role) => `${role.name} | ${role.code}`;

export const UserTable = () => {
    const [users, setUsers] = useState<UserRoleSummary[]>([]);
    const [roles, setRoles] = useState<Role[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [editingUser, setEditingUser] = useState<UserRoleSummary | null>(null);
    const [creatingUser, setCreatingUser] = useState(false);
    const [resettingUser, setResettingUser] = useState<UserRoleSummary | null>(null);
    const [selectedRoleIds, setSelectedRoleIds] = useState<string[]>([]);
    const [newPassword, setNewPassword] = useState('');
    const [createForm, setCreateForm] = useState({
        username: '',
        password: '',
        email: '',
        phone: '',
    });
    const [searchDraft, setSearchDraft] = useState('');
    const [searchKeyword, setSearchKeyword] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [roleFilter, setRoleFilter] = useState('');
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const roleIds = useMemo(() => new Set(selectedRoleIds), [selectedRoleIds]);

    const fetchUsers = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const userPage = await userApi.getRoleSummaries({
                keyword: searchKeyword,
                status: statusFilter,
                roleId: roleFilter,
                page,
                size: pageSize,
            });
            setUsers(userPage.content);
            setTotalElements(userPage.totalElements);
            setTotalPages(userPage.totalPages);
            if (userPage.totalPages > 0 && page >= userPage.totalPages) {
                setPage(userPage.totalPages - 1);
            }
        } catch (err) {
            console.error('Failed to fetch users', err);
            setError('載入使用者資料失敗，請稍後再試。');
        } finally {
            setLoading(false);
        }
    }, [page, pageSize, roleFilter, searchKeyword, statusFilter]);

    useEffect(() => {
        const fetchRoles = async () => {
            try {
                const roleList = await roleApi.getRoles();
                setRoles(roleList);
            } catch (err) {
                console.error('Failed to fetch roles', err);
                setError('載入角色資料失敗，請稍後再試。');
            }
        };
        fetchRoles();
    }, []);

    useEffect(() => {
        fetchUsers();
    }, [fetchUsers]);

    const openEditDialog = (user: UserRoleSummary) => {
        setEditingUser(user);
        setSelectedRoleIds(user.roles.map((role) => role.id));
        setSuccessMessage(null);
        setError(null);
    };

    const toggleRole = (roleId: string) => {
        setSelectedRoleIds((current) =>
            current.includes(roleId) ? current.filter((id) => id !== roleId) : [...current, roleId]
        );
    };

    const openCreateDialog = () => {
        setCreateForm({ username: '', password: '', email: '', phone: '' });
        setSelectedRoleIds([]);
        setCreatingUser(true);
        setSuccessMessage(null);
        setError(null);
    };

    const saveRoles = async () => {
        if (!editingUser) {
            return;
        }
        setSaving(true);
        setError(null);
        try {
            const updated = await userApi.updateRoles(editingUser.user.id, { roleIds: selectedRoleIds });
            setSuccessMessage(`已更新 ${updated.user.username} 的角色指派。`);
            setEditingUser(null);
            await fetchUsers();
        } catch (err) {
            console.error('Failed to update user roles', err);
            setError('更新角色指派失敗，請確認至少保留一位系統管理員。');
        } finally {
            setSaving(false);
        }
    };

    const createUser = async () => {
        setSaving(true);
        setError(null);
        try {
            const created = await userApi.create({
                username: createForm.username.trim(),
                password: createForm.password,
                email: createForm.email.trim() || undefined,
                phone: createForm.phone.trim() || undefined,
                roleIds: selectedRoleIds,
            });
            setSuccessMessage(`已建立 ${created.user.username} 帳號。`);
            setCreatingUser(false);
            await fetchUsers();
        } catch (err) {
            console.error('Failed to create user', err);
            setError('建立帳號失敗，請確認帳號未重複且密碼已填寫。');
        } finally {
            setSaving(false);
        }
    };

    const toggleStatus = async (item: UserRoleSummary) => {
        const nextStatus = item.user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
        setSaving(true);
        setError(null);
        try {
            const updated = await userApi.updateStatus(item.user.id, { status: nextStatus });
            setSuccessMessage(`已${nextStatus === 'ACTIVE' ? '啟用' : '停用'} ${updated.user.username}。`);
            await fetchUsers();
        } catch (err) {
            console.error('Failed to update user status', err);
            setError('更新帳號狀態失敗，請確認至少保留一位啟用中的系統管理員。');
        } finally {
            setSaving(false);
        }
    };

    const openResetDialog = (item: UserRoleSummary) => {
        setResettingUser(item);
        setNewPassword('');
        setSuccessMessage(null);
        setError(null);
    };

    const resetPassword = async () => {
        if (!resettingUser) {
            return;
        }
        setSaving(true);
        setError(null);
        try {
            const updated = await userApi.resetPassword(resettingUser.user.id, { password: newPassword });
            setSuccessMessage(`已重設 ${updated.user.username} 的密碼。`);
            setResettingUser(null);
            setNewPassword('');
            await fetchUsers();
        } catch (err) {
            console.error('Failed to reset password', err);
            setError('重設密碼失敗，請確認新密碼已填寫。');
        } finally {
            setSaving(false);
        }
    };

    const roleCheckboxes = (
        <Stack spacing={1}>
            {roles.map((role) => (
                <FormControlLabel
                    key={role.id}
                    control={
                        <Checkbox
                            checked={roleIds.has(role.id)}
                            onChange={() => toggleRole(role.id)}
                            sx={{
                                color: '#AEB4C4',
                                '&.Mui-checked': { color: '#FF6D00' },
                            }}
                        />
                    }
                    label={
                        <Box>
                            <Typography sx={{ color: '#FFFFFF', fontWeight: 900 }}>
                                {roleLabel(role)}
                            </Typography>
                            <Typography sx={{ color: '#AEB4C4', fontSize: 13, fontWeight: 700 }}>
                                {role.description || '未填寫描述'}
                            </Typography>
                        </Box>
                    }
                    sx={{
                        m: 0,
                        px: 1.5,
                        py: 1,
                        borderRadius: 1.5,
                        bgcolor: roleIds.has(role.id) ? 'rgba(255, 109, 0, 0.18)' : 'rgba(255,255,255,0.04)',
                        border: roleIds.has(role.id)
                            ? '1px solid rgba(255, 109, 0, 0.7)'
                            : '1px solid rgba(255,255,255,0.08)',
                    }}
                />
            ))}
        </Stack>
    );

    const applySearch = () => {
        setPage(0);
        setSearchKeyword(searchDraft.trim());
    };

    const clearFilters = () => {
        setSearchDraft('');
        setSearchKeyword('');
        setStatusFilter('');
        setRoleFilter('');
        setPage(0);
    };

    const hasActiveFilters = Boolean(searchKeyword || statusFilter || roleFilter);

    return (
        <Box>
            <Stack direction={{ xs: 'column', lg: 'row' }} spacing={1.5} justifyContent="space-between" sx={{ mb: 2 }}>
                <Stack direction={{ xs: 'column', md: 'row' }} spacing={1.25} sx={{ flex: 1 }}>
                    <TextField
                        label="搜尋帳號"
                        placeholder="帳號、Email 或手機"
                        value={searchDraft}
                        onChange={(event) => setSearchDraft(event.target.value)}
                        onKeyDown={(event) => {
                            if (event.key === 'Enter') {
                                applySearch();
                            }
                        }}
                        sx={{ minWidth: { md: 260 } }}
                    />
                    <FormControl sx={{ minWidth: 140 }}>
                        <InputLabel>帳號狀態</InputLabel>
                        <Select
                            label="帳號狀態"
                            value={statusFilter}
                            onChange={(event) => {
                                setPage(0);
                                setStatusFilter(event.target.value);
                            }}
                        >
                            <MenuItem value="">全部狀態</MenuItem>
                            <MenuItem value="ACTIVE">啟用</MenuItem>
                            <MenuItem value="INACTIVE">停用</MenuItem>
                            <MenuItem value="LOCKED">鎖定</MenuItem>
                        </Select>
                    </FormControl>
                    <FormControl sx={{ minWidth: 180 }}>
                        <InputLabel>角色</InputLabel>
                        <Select
                            label="角色"
                            value={roleFilter}
                            onChange={(event) => {
                                setPage(0);
                                setRoleFilter(event.target.value);
                            }}
                        >
                            <MenuItem value="">全部角色</MenuItem>
                            {roles.map((role) => (
                                <MenuItem key={role.id} value={role.id}>
                                    {role.name}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                    <Button
                        variant="outlined"
                        startIcon={<SearchIcon />}
                        onClick={applySearch}
                        sx={{ borderColor: 'rgba(255,109,0,0.7)', color: '#FFB15C', fontWeight: 900 }}
                    >
                        搜尋
                    </Button>
                    <Button
                        variant="outlined"
                        startIcon={<ClearIcon />}
                        onClick={clearFilters}
                        disabled={!hasActiveFilters && !searchDraft}
                        sx={{ borderColor: 'rgba(174,180,196,0.45)', color: '#FFFFFF', fontWeight: 900 }}
                    >
                        清除
                    </Button>
                </Stack>
                <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={openCreateDialog}
                    sx={{ bgcolor: '#FF6D00', color: '#FFFFFF', fontWeight: 900, minWidth: 132 }}
                >
                    新增帳號
                </Button>
            </Stack>
            <Stack spacing={1.5} sx={{ mb: 2 }}>
                {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}
                {successMessage && <Alert severity="success" onClose={() => setSuccessMessage(null)}>{successMessage}</Alert>}
            </Stack>
        <TableContainer
            component={Paper}
            elevation={0}
            sx={{
                borderRadius: 2,
                overflow: 'hidden',
                bgcolor: '#222532',
                border: '1px solid rgba(255,255,255,0.1)',
            }}
        >
            {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                    <CircularProgress />
                </Box>
            ) : (
                <Table sx={{ tableLayout: 'fixed' }}>
                    <TableHead sx={{ backgroundColor: 'rgba(255,255,255,0.04)' }}>
                        <TableRow>
                            <TableCell sx={{ width: '24%' }}><Typography fontWeight="bold" color="#FFFFFF">使用者名稱</Typography></TableCell>
                            <TableCell sx={{ width: '25%' }}><Typography fontWeight="bold" color="#FFFFFF">聯絡方式</Typography></TableCell>
                            <TableCell sx={{ width: 92 }}><Typography fontWeight="bold" color="#FFFFFF">狀態</Typography></TableCell>
                            <TableCell><Typography fontWeight="bold" color="#FFFFFF">角色</Typography></TableCell>
                            <TableCell align="right" sx={{ width: 168 }}><Typography fontWeight="bold" color="#FFFFFF">操作</Typography></TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {users.map((item) => (
                            <TableRow
                                key={item.user.id}
                                hover
                                sx={{
                                    '& td': { borderColor: 'rgba(255,255,255,0.08)' },
                                    '&:hover': { bgcolor: 'rgba(255,255,255,0.04)' },
                                }}
                            >
                                <TableCell>
                                    <Typography sx={{ color: '#FFFFFF', fontWeight: 900 }}>
                                        {item.user.username}
                                    </Typography>
                                    <Typography sx={{ color: '#AEB4C4', fontWeight: 700, fontSize: 13, wordBreak: 'break-all' }}>
                                        ID {item.user.id.slice(0, 8)}...{item.user.id.slice(-6)}
                                    </Typography>
                                </TableCell>
                                <TableCell sx={{ color: '#D4D6E2', fontWeight: 700 }}>
                                    <Stack spacing={0.5}>
                                        <span>{item.user.email || '-'}</span>
                                        <span>{item.user.phone || '-'}</span>
                                    </Stack>
                                </TableCell>
                                <TableCell>
                                    <Chip
                                        label={statusLabels[item.user.status] ?? item.user.status}
                                        sx={{
                                            bgcolor: item.user.status === 'ACTIVE' ? '#D7FBE5' : '#FFE4E6',
                                            color: item.user.status === 'ACTIVE' ? '#0F5132' : '#8B1020',
                                            fontWeight: 900,
                                        }}
                                    />
                                </TableCell>
                                <TableCell>
                                    <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                                        {item.roles.map((role) => (
                                            <Chip
                                                key={role.id}
                                                label={roleLabel(role)}
                                                sx={{ bgcolor: '#FFF3E6', color: '#7A3500', fontWeight: 900 }}
                                            />
                                        ))}
                                        {item.roles.length === 0 && (
                                            <Typography sx={{ color: '#AEB4C4', fontWeight: 700 }}>尚未指派</Typography>
                                        )}
                                    </Stack>
                                </TableCell>
                                <TableCell align="right">
                                    <Stack direction="row" spacing={1} justifyContent="flex-end">
                                        <Tooltip title={`編輯 ${item.user.username} 角色`}>
                                            <IconButton
                                                aria-label={`編輯 ${item.user.username} 角色`}
                                                onClick={() => openEditDialog(item)}
                                                sx={{
                                                    width: 44,
                                                    height: 44,
                                                    border: '1px solid rgba(255, 109, 0, 0.7)',
                                                    color: '#FFB15C',
                                                    bgcolor: 'rgba(255, 109, 0, 0.08)',
                                                    '&:hover': { bgcolor: 'rgba(255, 109, 0, 0.18)' },
                                                }}
                                            >
                                                <EditIcon fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                        <Tooltip title={`重設 ${item.user.username} 密碼`}>
                                            <IconButton
                                                aria-label={`重設 ${item.user.username} 密碼`}
                                                onClick={() => openResetDialog(item)}
                                                sx={{
                                                    width: 44,
                                                    height: 44,
                                                    border: '1px solid rgba(174,180,196,0.55)',
                                                    color: '#FFFFFF',
                                                    bgcolor: 'rgba(255,255,255,0.04)',
                                                    '&:hover': { bgcolor: 'rgba(255,255,255,0.1)' },
                                                }}
                                            >
                                                <LockResetIcon fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                        <Tooltip title={`${item.user.status === 'ACTIVE' ? '停用' : '啟用'} ${item.user.username}`}>
                                            <span>
                                                <IconButton
                                                    aria-label={`${item.user.status === 'ACTIVE' ? '停用' : '啟用'} ${item.user.username}`}
                                                    disabled={saving}
                                                    onClick={() => toggleStatus(item)}
                                                    sx={{
                                                        width: 44,
                                                        height: 44,
                                                        border: item.user.status === 'ACTIVE' ? '1px solid rgba(255, 120, 120, 0.75)' : '1px solid rgba(91, 255, 159, 0.75)',
                                                        color: item.user.status === 'ACTIVE' ? '#FF9A9A' : '#9BFFC4',
                                                        bgcolor: item.user.status === 'ACTIVE' ? 'rgba(255, 120, 120, 0.08)' : 'rgba(91, 255, 159, 0.08)',
                                                        '&:hover': {
                                                            bgcolor: item.user.status === 'ACTIVE' ? 'rgba(255, 120, 120, 0.16)' : 'rgba(91, 255, 159, 0.16)',
                                                        },
                                                        '&.Mui-disabled': {
                                                            color: 'rgba(255,255,255,0.28)',
                                                            borderColor: 'rgba(255,255,255,0.14)',
                                                        },
                                                    }}
                                                >
                                                    <PowerSettingsNewIcon fontSize="small" />
                                                </IconButton>
                                            </span>
                                        </Tooltip>
                                    </Stack>
                                </TableCell>
                            </TableRow>
                        ))}
                        {users.length === 0 && (
                            <TableRow>
                                <TableCell colSpan={5} align="center" sx={{ py: 4, color: '#AEB4C4' }}>暫無資料</TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            )}
        </TableContainer>
            <Stack
                direction={{ xs: 'column', sm: 'row' }}
                spacing={1.5}
                justifyContent="space-between"
                alignItems={{ xs: 'stretch', sm: 'center' }}
                sx={{ mt: 2 }}
            >
                <Typography sx={{ color: '#D4D6E2', fontWeight: 800 }}>
                    共 {totalElements} 筆帳號
                </Typography>
                <Stack direction="row" spacing={1.5} alignItems="center" justifyContent="flex-end">
                    <FormControl size="small" sx={{ minWidth: 112 }}>
                        <InputLabel>每頁</InputLabel>
                        <Select
                            label="每頁"
                            value={String(pageSize)}
                            onChange={(event) => {
                                setPage(0);
                                setPageSize(Number(event.target.value));
                            }}
                        >
                            <MenuItem value="5">5 筆</MenuItem>
                            <MenuItem value="10">10 筆</MenuItem>
                            <MenuItem value="20">20 筆</MenuItem>
                        </Select>
                    </FormControl>
                    <Pagination
                        count={Math.max(totalPages, 1)}
                        page={Math.min(page + 1, Math.max(totalPages, 1))}
                        onChange={(_, nextPage) => setPage(nextPage - 1)}
                        shape="rounded"
                        sx={{
                            '& .MuiPaginationItem-root': {
                                color: '#D4D6E2',
                                borderColor: 'rgba(255,255,255,0.16)',
                            },
                            '& .Mui-selected': {
                                bgcolor: '#FF6D00 !important',
                                color: '#FFFFFF',
                            },
                        }}
                    />
                </Stack>
            </Stack>
            <Dialog
                open={Boolean(editingUser)}
                onClose={() => !saving && setEditingUser(null)}
                maxWidth="sm"
                fullWidth
                PaperProps={{
                    sx: {
                        bgcolor: '#2A2E3D',
                        color: '#FFFFFF',
                        borderRadius: 2,
                        border: '1px solid rgba(255,255,255,0.12)',
                    },
                }}
            >
                <DialogTitle sx={{ fontWeight: 900 }}>
                    編輯角色：{editingUser?.user.username}
                </DialogTitle>
                <DialogContent>
                    <Typography sx={{ color: '#D4D6E2', mb: 2, fontWeight: 700 }}>
                        儲存後會覆蓋此帳號的完整角色清單。
                    </Typography>
                    {roleCheckboxes}
                </DialogContent>
                <DialogActions sx={{ px: 3, pb: 3 }}>
                    <Button onClick={() => setEditingUser(null)} disabled={saving} sx={{ color: '#D4D6E2', fontWeight: 900 }}>
                        取消
                    </Button>
                    <Button
                        variant="contained"
                        startIcon={saving ? <CircularProgress size={18} color="inherit" /> : <SaveIcon />}
                        onClick={saveRoles}
                        disabled={saving}
                        sx={{ bgcolor: '#FF6D00', color: '#FFFFFF', fontWeight: 900 }}
                    >
                        儲存角色
                    </Button>
                </DialogActions>
            </Dialog>
            <Dialog
                open={creatingUser}
                onClose={() => !saving && setCreatingUser(false)}
                maxWidth="sm"
                fullWidth
                PaperProps={{
                    sx: {
                        bgcolor: '#2A2E3D',
                        color: '#FFFFFF',
                        borderRadius: 2,
                        border: '1px solid rgba(255,255,255,0.12)',
                    },
                }}
            >
                <DialogTitle sx={{ fontWeight: 900 }}>新增帳號</DialogTitle>
                <DialogContent>
                    <Stack spacing={2} sx={{ mt: 1 }}>
                        <TextField
                            label="帳號"
                            value={createForm.username}
                            onChange={(event) => setCreateForm((current) => ({ ...current, username: event.target.value }))}
                            fullWidth
                            required
                        />
                        <TextField
                            label="初始密碼"
                            type="password"
                            value={createForm.password}
                            onChange={(event) => setCreateForm((current) => ({ ...current, password: event.target.value }))}
                            fullWidth
                            required
                        />
                        <TextField
                            label="Email"
                            value={createForm.email}
                            onChange={(event) => setCreateForm((current) => ({ ...current, email: event.target.value }))}
                            fullWidth
                        />
                        <TextField
                            label="手機"
                            value={createForm.phone}
                            onChange={(event) => setCreateForm((current) => ({ ...current, phone: event.target.value }))}
                            fullWidth
                        />
                        <Typography sx={{ color: '#D4D6E2', fontWeight: 900 }}>初始角色</Typography>
                        {roleCheckboxes}
                    </Stack>
                </DialogContent>
                <DialogActions sx={{ px: 3, pb: 3 }}>
                    <Button onClick={() => setCreatingUser(false)} disabled={saving} sx={{ color: '#D4D6E2', fontWeight: 900 }}>
                        取消
                    </Button>
                    <Button
                        variant="contained"
                        startIcon={saving ? <CircularProgress size={18} color="inherit" /> : <AddIcon />}
                        onClick={createUser}
                        disabled={saving}
                        sx={{ bgcolor: '#FF6D00', color: '#FFFFFF', fontWeight: 900 }}
                    >
                        建立帳號
                    </Button>
                </DialogActions>
            </Dialog>
            <Dialog
                open={Boolean(resettingUser)}
                onClose={() => !saving && setResettingUser(null)}
                maxWidth="xs"
                fullWidth
                PaperProps={{
                    sx: {
                        bgcolor: '#2A2E3D',
                        color: '#FFFFFF',
                        borderRadius: 2,
                        border: '1px solid rgba(255,255,255,0.12)',
                    },
                }}
            >
                <DialogTitle sx={{ fontWeight: 900 }}>
                    重設密碼：{resettingUser?.user.username}
                </DialogTitle>
                <DialogContent>
                    <TextField
                        label="新密碼"
                        type="password"
                        value={newPassword}
                        onChange={(event) => setNewPassword(event.target.value)}
                        fullWidth
                        required
                        sx={{ mt: 1 }}
                    />
                </DialogContent>
                <DialogActions sx={{ px: 3, pb: 3 }}>
                    <Button onClick={() => setResettingUser(null)} disabled={saving} sx={{ color: '#D4D6E2', fontWeight: 900 }}>
                        取消
                    </Button>
                    <Button
                        variant="contained"
                        startIcon={saving ? <CircularProgress size={18} color="inherit" /> : <LockResetIcon />}
                        onClick={resetPassword}
                        disabled={saving}
                        sx={{ bgcolor: '#FF6D00', color: '#FFFFFF', fontWeight: 900 }}
                    >
                        重設密碼
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};
