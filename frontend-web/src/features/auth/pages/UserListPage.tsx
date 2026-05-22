import { Box, Typography } from '@mui/material';
import { UserTable } from '../components/UserTable';

/**
 * @file UserListPage.tsx
 * @description 使用者列表頁面 / User listing page
 */
export const UserListPage = () => {
    return (
        <Box>
            <Typography variant="h3" gutterBottom sx={{ fontWeight: 900, color: 'text.primary', mb: 1 }}>
                帳號管理
            </Typography>
            <Typography sx={{ color: 'text.secondary', fontWeight: 700, mb: 4 }}>
                檢視後台登入帳號與角色指派，角色權限請至角色權限頁調整。
            </Typography>
            <UserTable />
        </Box>
    );
};
