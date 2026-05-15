import { Box, Stack, Typography } from '@mui/material';
import { RolePermissionTree } from '../components/RolePermissionTree';

/**
 * @file RoleListPage.tsx
 * @description 角色列表頁面 / Role listing page
 * @description_en Shows RBAC role and permission summaries in the admin console
 * @description_zh 顯示後台角色與權限摘要，供管理者檢視 RBAC 設定
 */
export const RoleListPage = () => {
    return (
        <Box sx={{ px: { xs: 2, md: 4 }, py: { xs: 2.5, md: 4 }, maxWidth: 1440, mx: 'auto' }}>
            <Stack spacing={1} sx={{ mb: 3 }}>
                <Typography sx={{ fontWeight: 900, color: '#FFFFFF', fontSize: { xs: 32, md: 42 }, lineHeight: 1.1 }}>
                    角色與權限管理
                </Typography>
                <Typography sx={{ color: '#AEB4C4', fontWeight: 700 }}>
                    集中檢視後台角色、權限分組與管理範圍，避免權限設定散落在不同功能頁。
                </Typography>
            </Stack>
            <RolePermissionTree />
        </Box>
    );
};
