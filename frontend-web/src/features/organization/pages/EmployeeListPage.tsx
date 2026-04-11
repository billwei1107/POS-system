import { useEffect, useState } from 'react';
import { Box, Typography } from '@mui/material';
import { EmployeeTable } from '../components/EmployeeTable';
import { organizationApi } from '../api/organizationApi';
import type { Employee } from '../types';

export const EmployeeListPage = () => {
    const [employees, setEmployees] = useState<Employee[]>([]);

    useEffect(() => {
        organizationApi.getEmployees().then(res => {
            setEmployees(res.data || []);
        }).catch(console.error);
    }, []);

    return (
        <Box sx={{ p: 4 }}>
            <Typography variant="h5" sx={{ mb: 3, fontWeight: 'bold' }}>
                全體員工總覽
            </Typography>
            <EmployeeTable employees={employees} />
        </Box>
    );
};
