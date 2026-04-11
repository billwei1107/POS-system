import { useEffect, useState } from 'react';
import { Box, Typography, CircularProgress } from '@mui/material';
import { DepartmentTree } from '../components/DepartmentTree';
import { EmployeeTable } from '../components/EmployeeTable';
import { organizationApi } from '../api/organizationApi';
import type { DepartmentTreeDTO, Employee } from '../types';

export const DepartmentPage = () => {
    const [treeData, setTreeData] = useState<DepartmentTreeDTO[]>([]);
    const [employees, setEmployees] = useState<Employee[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        // Note: 'dummy-company-id' is used for demonstration
        // It should be fetched from user session context.
        const companyId = 'edcc0fa6-17b5-47e1-ae76-78b172a1cd34';
        organizationApi.getDepartmentTree(companyId)
            .then(res => setTreeData(res.data || []))
            .catch(console.error);
    }, []);

    const handleSelectDepartment = (deptId: string) => {
        setLoading(true);
        organizationApi.getEmployees({ departmentId: deptId })
            .then(res => setEmployees(res.data || []))
            .catch(console.error)
            .finally(() => setLoading(false));
    };

    return (
        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' } }}>
            <Box sx={{ width: { xs: '100%', md: '25%' }, flexShrink: 0 }}>
                <DepartmentTree data={treeData} onSelect={handleSelectDepartment} />
            </Box>
            <Box sx={{ flexGrow: 1, p: 4, width: '100%' }}>
                <Typography variant="h5" sx={{ mb: 3, fontWeight: 'bold' }}>
                    員工管理表
                </Typography>
                {loading ? (
                    <Box display="flex" justifyContent="center" mt={5}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <EmployeeTable employees={employees} />
                )}
            </Box>
        </Box>
    );
};
