import { Box, Typography } from '@mui/material';
import { RolePermissionTree } from '../components/RolePermissionTree';

/**
 * @file RoleListPage.tsx
 * @description 角色列表頁面 / Role listing page
 */
export const RoleListPage = () => {
    return (
        <Box sx={{ p: 4, maxWidth: 1000, margin: '0 auto' }}>
            <Typography variant="h4" gutterBottom sx={{ fontWeight: 800, color: '#0f172a', mb: 4 }}>
                角色與權限管理
            </Typography>
            <RolePermissionTree />
        </Box>
    );
};
