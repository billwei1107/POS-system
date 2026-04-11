import { useEffect, useState } from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Typography, CircularProgress, Box } from '@mui/material';
import axiosInstance from '../../../shared/api/axiosInstance';
import type { User } from '../types';

/**
 * @file UserTable.tsx
 * @description 使用者資料表格組件 / User datatable component
 */
export const UserTable = () => {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const res = await axiosInstance.get('/v1/users');
                if (res && (res as any).data) {
                    const payload = (res as any).data;
                    setUsers(Array.isArray(payload) ? payload : payload.content || []);
                }
            } catch (err) {
                console.error("Failed to fetch users", err);
            } finally {
                setLoading(false);
            }
        };
        fetchUsers();
    }, []);

    return (
        <TableContainer component={Paper} elevation={3} sx={{ borderRadius: 2, overflow: 'hidden' }}>
            {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                    <CircularProgress />
                </Box>
            ) : (
                <Table>
                    <TableHead sx={{ backgroundColor: '#f8fafc' }}>
                        <TableRow>
                            <TableCell><Typography fontWeight="bold" color="#334155">使用者名稱</Typography></TableCell>
                            <TableCell><Typography fontWeight="bold" color="#334155">信箱</Typography></TableCell>
                            <TableCell><Typography fontWeight="bold" color="#334155">手機</Typography></TableCell>
                            <TableCell><Typography fontWeight="bold" color="#334155">狀態</Typography></TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {users.map((user, idx) => (
                            <TableRow key={user.id || idx} hover sx={{ '&:last-child td, &:last-child th': { border: 0 }, transition: 'background-color 0.2s' }}>
                                <TableCell>{user.username}</TableCell>
                                <TableCell>{user.email || '-'}</TableCell>
                                <TableCell>{user.phone || '-'}</TableCell>
                                <TableCell>
                                    <Box component="span" sx={{
                                        px: 1.5, py: 0.5,
                                        backgroundColor: user.status === 'ACTIVE' ? '#dcfce7' : '#fee2e2',
                                        color: user.status === 'ACTIVE' ? '#166534' : '#991b1b',
                                        borderRadius: 4,
                                        fontSize: '0.85rem',
                                        fontWeight: 'bold'
                                    }}>
                                        {user.status}
                                    </Box>
                                </TableCell>
                            </TableRow>
                        ))}
                        {users.length === 0 && (
                            <TableRow>
                                <TableCell colSpan={4} align="center" sx={{ py: 4, color: 'text.secondary' }}>暫無資料</TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            )}
        </TableContainer>
    );
};
