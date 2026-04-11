import { Table, TableHead, TableRow, TableCell, TableBody, Paper, TableContainer, Typography } from '@mui/material';
import type { Employee } from '../types';

export const EmployeeTable = ({ employees }: { employees: Employee[] }) => {
    return (
        <TableContainer component={Paper} sx={{ boxShadow: 'none', border: '1px solid #e0e0e0' }}>
            <Table>
                <TableHead>
                    <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                        <TableCell><strong>工號</strong></TableCell>
                        <TableCell><strong>姓名</strong></TableCell>
                        <TableCell><strong>狀態</strong></TableCell>
                        <TableCell><strong>信箱</strong></TableCell>
                        <TableCell><strong>入職日期</strong></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {employees.length > 0 ? employees.map(emp => (
                        <TableRow key={emp.employeeNo}>
                            <TableCell>{emp.employeeNo}</TableCell>
                            <TableCell>{emp.name}</TableCell>
                            <TableCell>{emp.status}</TableCell>
                            <TableCell>{emp.email}</TableCell>
                            <TableCell>{emp.hireDate}</TableCell>
                        </TableRow>
                    )) : (
                        <TableRow>
                            <TableCell colSpan={5} align="center">
                                <Typography color="textSecondary" sx={{ py: 3 }}>找不到符合條件的員工紀錄</Typography>
                            </TableCell>
                        </TableRow>
                    )}
                </TableBody>
            </Table>
        </TableContainer>
    );
};
