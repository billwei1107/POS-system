import { useEffect, useState } from 'react';
import { Box, Paper, Typography, CircularProgress } from '@mui/material';
import axiosInstance from '../../../shared/api/axiosInstance';
import type { Role } from '../types';

/**
 * @file RolePermissionTree.tsx
 * @description 角色清單與權限樹 / Roles and permission tree
 */
export const RolePermissionTree = () => {
    const [roles, setRoles] = useState<Role[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchRoles = async () => {
            try {
                const res = await axiosInstance.get('/v1/roles');
                if (res && (res as any).data) {
                    const payload = (res as any).data;
                    setRoles(Array.isArray(payload) ? payload : payload.content || []);
                }
            } catch (error) {
                console.error(error);
            } finally {
                setLoading(false);
            }
        };
        fetchRoles();
    }, []);

    if (loading) return <CircularProgress />;

    return (
        <Paper elevation={3} sx={{ p: 4, borderRadius: 2 }}>
            <Typography variant="h6" gutterBottom sx={{ fontWeight: 700, mb: 3 }}>
                現有角色與權限架構
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                {roles.map(role => (
                    <Box key={role.id} sx={{
                        p: 2.5,
                        border: '1px solid #e2e8f0',
                        borderRadius: 2,
                        transition: 'box-shadow 0.2s',
                        '&:hover': { boxShadow: 2, borderColor: '#cbd5e1' }
                    }}>
                        <Typography variant="subtitle1" sx={{ fontWeight: 'bold', color: '#0d9488' }}>
                            {role.name} <Box component="span" sx={{ color: 'text.secondary', fontWeight: 'normal', fontSize: '0.9rem' }}>({role.code})</Box>
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                            {role.description || '暫無描述'}
                        </Typography>
                    </Box>
                ))}
                {roles.length === 0 && (
                    <Typography color="text.secondary" align="center" sx={{ py: 3 }}>
                        系統目前無建立任何角色
                    </Typography>
                )}
            </Box>
        </Paper>
    );
};
