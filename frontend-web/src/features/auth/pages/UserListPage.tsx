import { Box, Typography } from '@mui/material';
import { UserTable } from '../components/UserTable';

/**
 * @file UserListPage.tsx
 * @description 使用者列表頁面 / User listing page
 */
export const UserListPage = () => {
    return (
        <Box sx={{ p: 4, maxWidth: 1200, margin: '0 auto' }}>
            <Typography variant="h4" gutterBottom sx={{ fontWeight: 800, color: '#0f172a', mb: 4 }}>
                系統使用者管理
            </Typography>
            <UserTable />
        </Box>
    );
};
