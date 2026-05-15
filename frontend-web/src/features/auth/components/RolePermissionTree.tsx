import { useEffect, useMemo, useState } from 'react';
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
    Divider,
    FormControlLabel,
    Paper,
    Stack,
    Typography,
} from '@mui/material';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import EditIcon from '@mui/icons-material/Edit';
import GroupsIcon from '@mui/icons-material/Groups';
import KeyIcon from '@mui/icons-material/Key';
import SaveIcon from '@mui/icons-material/Save';
import SecurityIcon from '@mui/icons-material/Security';
import { roleApi } from '../api/authApi';
import type { Permission, RolePermissionSummary } from '../types';

/**
 * @file RolePermissionTree.tsx
 * @description 角色權限摘要 / Role permission summary
 * @description_en Displays roles with assigned permission groups for admin RBAC review
 * @description_zh 以角色為單位呈現已指派權限，供後台檢視 RBAC 設定
 */
type PermissionGroups = Record<string, Permission[]>;

const permissionTypeLabels: Record<string, string> = {
    SYSTEM: '系統',
    POS: 'POS',
    INVENTORY: '庫存',
    PAYMENT: '支付',
    ORGANIZATION: '組織',
    REPORT: '報表',
};

const getPermissionTypeLabel = (type: string) => permissionTypeLabels[type] ?? type;

const groupPermissionsByType = (permissions: Permission[]): PermissionGroups => {
    return permissions.reduce<PermissionGroups>((groups, permission) => {
        const key = permission.type || 'OTHER';
        return {
            ...groups,
            [key]: [...(groups[key] ?? []), permission],
        };
    }, {});
};

const StatCard = ({
    label,
    value,
    icon,
}: {
    label: string;
    value: number;
    icon: React.ReactNode;
}) => (
    <Paper
        elevation={0}
        sx={{
            flex: '1 1 220px',
            minWidth: 0,
            p: 2.5,
            borderRadius: 2,
            bgcolor: '#242837',
            border: '1px solid rgba(255,255,255,0.1)',
        }}
    >
        <Stack direction="row" spacing={1.5} alignItems="center">
            <Box
                sx={{
                    width: 42,
                    height: 42,
                    borderRadius: 2,
                    display: 'grid',
                    placeItems: 'center',
                    color: '#FFFFFF',
                    bgcolor: 'rgba(255, 109, 0, 0.2)',
                }}
            >
                {icon}
            </Box>
            <Box>
                <Typography sx={{ color: '#AEB4C4', fontWeight: 800, fontSize: 13 }}>
                    {label}
                </Typography>
                <Typography sx={{ color: '#FFFFFF', fontWeight: 900, fontSize: 28, lineHeight: 1.1 }}>
                    {value}
                </Typography>
            </Box>
        </Stack>
    </Paper>
);

const RoleCard = ({
    role,
    onEdit,
}: {
    role: RolePermissionSummary;
    onEdit: (role: RolePermissionSummary) => void;
}) => {
    const isSuperAdmin = role.code === 'SUPER_ADMIN';
    const permissionGroups = groupPermissionsByType(role.permissions);
    const groupEntries = Object.entries(permissionGroups).sort(([left], [right]) => left.localeCompare(right));

    return (
        <Paper
            elevation={0}
            sx={{
                p: { xs: 2, md: 2.5 },
                borderRadius: 2,
                bgcolor: '#222532',
                border: '1px solid rgba(255,255,255,0.1)',
                transition: 'border-color 0.2s, transform 0.2s',
                '&:hover': {
                    borderColor: 'rgba(255, 109, 0, 0.58)',
                    transform: 'translateY(-1px)',
                },
            }}
        >
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} justifyContent="space-between">
                <Box sx={{ minWidth: 0 }}>
                    <Stack direction="row" alignItems="center" spacing={1.25} flexWrap="wrap">
                        <Typography sx={{ color: '#FFFFFF', fontWeight: 900, fontSize: 20 }}>
                            {role.name}
                        </Typography>
                        <Chip
                            size="small"
                            label={role.code}
                            sx={{
                                bgcolor: '#FFF3E6',
                                color: '#B24700',
                                fontWeight: 900,
                                letterSpacing: 0,
                            }}
                        />
                    </Stack>
                    <Typography sx={{ mt: 0.75, color: '#C7CBD6', fontWeight: 600 }}>
                        {role.description || '尚未填寫角色說明'}
                    </Typography>
                </Box>
                <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
                    <Chip
                        icon={<KeyIcon />}
                        label={isSuperAdmin ? '全部權限' : `${role.permissions.length} 項權限`}
                        sx={{
                            minHeight: 36,
                            px: 1,
                            bgcolor: 'rgba(46, 204, 113, 0.18)',
                            color: '#DFFFEA',
                            border: '1px solid rgba(46, 204, 113, 0.42)',
                            fontWeight: 900,
                            '& .MuiChip-icon': { color: '#2ECC71' },
                        }}
                    />
                    <Button
                        variant="outlined"
                        startIcon={<EditIcon />}
                        disabled={isSuperAdmin}
                        onClick={() => onEdit(role)}
                        sx={{
                            minHeight: 36,
                            borderRadius: 2,
                            color: isSuperAdmin ? '#8F96A8' : '#FFFFFF',
                            borderColor: isSuperAdmin ? 'rgba(255,255,255,0.14)' : 'rgba(255, 109, 0, 0.62)',
                            fontWeight: 900,
                            '&:hover': {
                                borderColor: '#FF8A2A',
                                bgcolor: 'rgba(255, 109, 0, 0.12)',
                            },
                        }}
                    >
                        編輯權限
                    </Button>
                </Stack>
            </Stack>

            <Divider sx={{ my: 2, borderColor: 'rgba(255,255,255,0.08)' }} />

            {isSuperAdmin ? (
                <Box
                    sx={{
                        p: 1.75,
                        borderRadius: 2,
                        bgcolor: 'rgba(46, 204, 113, 0.1)',
                        border: '1px solid rgba(46, 204, 113, 0.32)',
                    }}
                >
                    <Typography sx={{ color: '#DFFFEA', fontWeight: 900 }}>
                        系統管理員透過後端權限守衛擁有所有系統資源的支配權限。
                    </Typography>
                </Box>
            ) : groupEntries.length === 0 ? (
                <Typography sx={{ color: '#C7CBD6', fontWeight: 700 }}>
                    此角色尚未指派權限
                </Typography>
            ) : (
                <Stack spacing={1.75}>
                    {groupEntries.map(([type, permissions]) => (
                        <Box key={`${role.id}-${type}`}>
                            <Typography sx={{ mb: 1, color: '#FFFFFF', fontWeight: 900, fontSize: 14 }}>
                                {getPermissionTypeLabel(type)}
                            </Typography>
                            <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
                                {permissions.map((permission) => (
                                    <Chip
                                        key={permission.id}
                                        label={`${permission.name} | ${permission.code}`}
                                        variant="outlined"
                                        sx={{
                                            maxWidth: '100%',
                                            minHeight: 34,
                                            color: '#F8FAFC',
                                            borderColor: 'rgba(255,255,255,0.2)',
                                            bgcolor: 'rgba(255,255,255,0.04)',
                                            fontWeight: 700,
                                            '& .MuiChip-label': {
                                                display: 'block',
                                                overflow: 'hidden',
                                                textOverflow: 'ellipsis',
                                            },
                                        }}
                                    />
                                ))}
                            </Stack>
                        </Box>
                    ))}
                </Stack>
            )}
        </Paper>
    );
};

const RolePermissionDialog = ({
    role,
    permissions,
    selectedPermissionIds,
    saving,
    onClose,
    onToggle,
    onSave,
}: {
    role: RolePermissionSummary | null;
    permissions: Permission[];
    selectedPermissionIds: Set<string>;
    saving: boolean;
    onClose: () => void;
    onToggle: (permissionId: string) => void;
    onSave: () => void;
}) => {
    const permissionGroups = useMemo(() => groupPermissionsByType(permissions), [permissions]);
    const groupEntries = Object.entries(permissionGroups).sort(([left], [right]) => left.localeCompare(right));

    return (
        <Dialog
            open={Boolean(role)}
            onClose={saving ? undefined : onClose}
            maxWidth="md"
            fullWidth
            PaperProps={{
                sx: {
                    bgcolor: '#232633',
                    color: '#FFFFFF',
                    borderRadius: 3,
                    border: '1px solid rgba(255,255,255,0.12)',
                },
            }}
        >
            <DialogTitle sx={{ pb: 1 }}>
                <Typography sx={{ fontWeight: 900, fontSize: 26 }}>
                    編輯角色權限
                </Typography>
                <Typography sx={{ mt: 0.5, color: '#C7CBD6', fontWeight: 700 }}>
                    {role ? `${role.name} | ${role.code}` : ''}
                </Typography>
            </DialogTitle>
            <DialogContent sx={{ pt: 1 }}>
                <Alert severity="warning" sx={{ mb: 2, bgcolor: 'rgba(255, 152, 0, 0.12)', color: '#FFE5C2' }}>
                    儲存會以目前勾選結果覆蓋此角色的完整權限清單。
                </Alert>
                <Stack spacing={2}>
                    {groupEntries.map(([type, groupPermissions]) => (
                        <Paper
                            key={type}
                            elevation={0}
                            sx={{
                                p: 2,
                                borderRadius: 2,
                                bgcolor: 'rgba(255,255,255,0.04)',
                                border: '1px solid rgba(255,255,255,0.1)',
                            }}
                        >
                            <Typography sx={{ mb: 1.25, color: '#FFFFFF', fontWeight: 900 }}>
                                {getPermissionTypeLabel(type)}
                            </Typography>
                            <Box
                                sx={{
                                    display: 'grid',
                                    gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, minmax(0, 1fr))' },
                                    gap: 1,
                                }}
                            >
                                {groupPermissions.map((permission) => (
                                    <FormControlLabel
                                        key={permission.id}
                                        control={
                                            <Checkbox
                                                checked={selectedPermissionIds.has(permission.id)}
                                                onChange={() => onToggle(permission.id)}
                                                sx={{
                                                    color: '#AEB4C4',
                                                    '&.Mui-checked': { color: '#FF8A2A' },
                                                }}
                                            />
                                        }
                                        label={
                                            <Box sx={{ minWidth: 0 }}>
                                                <Typography sx={{ color: '#FFFFFF', fontWeight: 800, lineHeight: 1.25 }}>
                                                    {permission.name}
                                                </Typography>
                                                <Typography sx={{ color: '#B8BDCA', fontWeight: 700, fontSize: 13, lineHeight: 1.25 }}>
                                                    {permission.code}
                                                </Typography>
                                            </Box>
                                        }
                                        sx={{
                                            m: 0,
                                            minHeight: 58,
                                            px: 1,
                                            py: 0.5,
                                            borderRadius: 2,
                                            border: selectedPermissionIds.has(permission.id)
                                                ? '1px solid rgba(255, 138, 42, 0.62)'
                                                : '1px solid rgba(255,255,255,0.08)',
                                            bgcolor: selectedPermissionIds.has(permission.id)
                                                ? 'rgba(255, 109, 0, 0.14)'
                                                : 'rgba(255,255,255,0.02)',
                                        }}
                                    />
                                ))}
                            </Box>
                        </Paper>
                    ))}
                </Stack>
            </DialogContent>
            <DialogActions sx={{ px: 3, pb: 3 }}>
                <Button onClick={onClose} disabled={saving} sx={{ color: '#D4D6E2', fontWeight: 900 }}>
                    取消
                </Button>
                <Button
                    variant="contained"
                    startIcon={saving ? <CircularProgress size={18} color="inherit" /> : <SaveIcon />}
                    onClick={onSave}
                    disabled={saving}
                    sx={{
                        minHeight: 44,
                        borderRadius: 2,
                        bgcolor: '#FF6D00',
                        color: '#FFFFFF',
                        fontWeight: 900,
                        '&:hover': { bgcolor: '#FF8A2A' },
                    }}
                >
                    儲存權限
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export const RolePermissionTree = () => {
    const [roles, setRoles] = useState<RolePermissionSummary[]>([]);
    const [permissions, setPermissions] = useState<Permission[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [editingRole, setEditingRole] = useState<RolePermissionSummary | null>(null);
    const [selectedPermissionIds, setSelectedPermissionIds] = useState<Set<string>>(new Set());

    useEffect(() => {
        const fetchRoles = async () => {
            try {
                const [summaries, allPermissions] = await Promise.all([
                    roleApi.getPermissionSummaries(),
                    roleApi.getPermissions(),
                ]);
                setRoles(summaries);
                setPermissions(allPermissions.sort((left, right) => left.code.localeCompare(right.code)));
                setErrorMessage('');
            } catch (error) {
                console.error(error);
                setErrorMessage('無法載入角色權限資料，請稍後再試。');
            } finally {
                setLoading(false);
            }
        };
        fetchRoles();
    }, []);

    const openPermissionEditor = (role: RolePermissionSummary) => {
        setEditingRole(role);
        setSelectedPermissionIds(new Set(role.permissions.map((permission) => permission.id)));
        setErrorMessage('');
        setSuccessMessage('');
    };

    const togglePermission = (permissionId: string) => {
        setSelectedPermissionIds((current) => {
            const next = new Set(current);
            if (next.has(permissionId)) {
                next.delete(permissionId);
            } else {
                next.add(permissionId);
            }
            return next;
        });
    };

    const savePermissionChanges = async () => {
        if (!editingRole) {
            return;
        }
        setSaving(true);
        try {
            const updatedRole = await roleApi.updatePermissions(editingRole.id, {
                permissionIds: Array.from(selectedPermissionIds),
            });
            setRoles((current) => current.map((role) => (role.id === updatedRole.id ? updatedRole : role)));
            setEditingRole(null);
            setSelectedPermissionIds(new Set());
            setErrorMessage('');
            setSuccessMessage(`已更新 ${updatedRole.name} 的權限設定。`);
        } catch (error) {
            console.error(error);
            setErrorMessage('權限儲存失敗，請確認帳號具備管理角色權限。');
        } finally {
            setSaving(false);
        }
    };

    const stats = useMemo(() => {
        const uniquePermissionCodes = new Set(permissions.map((permission) => permission.code));
        const privilegedRoles = roles.filter((role) => role.code.includes('ADMIN') || role.code.includes('MANAGER')).length;

        return {
            roleCount: roles.length,
            permissionCount: uniquePermissionCodes.size,
            privilegedRoles,
        };
    }, [permissions, roles]);

    if (loading) {
        return (
            <Box sx={{ py: 8, display: 'grid', placeItems: 'center' }}>
                <CircularProgress sx={{ color: '#FF6D00' }} />
            </Box>
        );
    }

    return (
        <Stack spacing={2.5}>
            {errorMessage && (
                <Alert severity="error" sx={{ bgcolor: 'rgba(244, 67, 54, 0.12)', color: '#FFD9D9' }}>
                    {errorMessage}
                </Alert>
            )}
            {successMessage && (
                <Alert severity="success" onClose={() => setSuccessMessage('')} sx={{ bgcolor: 'rgba(46, 204, 113, 0.12)', color: '#DFFFEA' }}>
                    {successMessage}
                </Alert>
            )}

            <Stack direction={{ xs: 'column', lg: 'row' }} spacing={2}>
                <StatCard label="角色總數" value={stats.roleCount} icon={<GroupsIcon />} />
                <StatCard label="可用權限" value={stats.permissionCount} icon={<SecurityIcon />} />
                <StatCard label="管理角色" value={stats.privilegedRoles} icon={<AdminPanelSettingsIcon />} />
            </Stack>

            <Paper
                elevation={0}
                sx={{
                    p: { xs: 2, md: 2.5 },
                    borderRadius: 2,
                    bgcolor: '#1F222E',
                    border: '1px solid rgba(255,255,255,0.1)',
                }}
            >
                <Stack direction={{ xs: 'column', md: 'row' }} justifyContent="space-between" spacing={1.5} sx={{ mb: 2 }}>
                    <Box>
                        <Typography sx={{ color: '#FFFFFF', fontWeight: 900, fontSize: 22 }}>
                            現有角色權限架構
                        </Typography>
                        <Typography sx={{ mt: 0.5, color: '#AEB4C4', fontWeight: 700 }}>
                            檢視各角色目前繫結的操作權限，後續可擴充為編輯與審批流程。
                        </Typography>
                    </Box>
                </Stack>

                <Stack spacing={1.5}>
                    {roles.map((role) => (
                        <RoleCard key={role.id} role={role} onEdit={openPermissionEditor} />
                    ))}
                    {roles.length === 0 && (
                        <Box sx={{ py: 6, textAlign: 'center' }}>
                            <Typography sx={{ color: '#FFFFFF', fontWeight: 900 }}>
                                系統目前尚未建立角色
                            </Typography>
                            <Typography sx={{ mt: 1, color: '#AEB4C4', fontWeight: 700 }}>
                                完成角色初始化後，這裡會顯示可用角色與權限摘要。
                            </Typography>
                        </Box>
                    )}
                </Stack>
            </Paper>

            <RolePermissionDialog
                role={editingRole}
                permissions={permissions}
                selectedPermissionIds={selectedPermissionIds}
                saving={saving}
                onClose={() => {
                    setEditingRole(null);
                    setSelectedPermissionIds(new Set());
                }}
                onToggle={togglePermission}
                onSave={savePermissionChanges}
            />
        </Stack>
    );
};
