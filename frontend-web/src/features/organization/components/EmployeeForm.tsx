import { Box, TextField, Button, Typography } from '@mui/material';
import { useState } from 'react';
import type { Employee } from '../types';

interface EmployeeFormProps {
    onSubmit: (employee: Partial<Employee>) => void;
}

export const EmployeeForm = ({ onSubmit }: EmployeeFormProps) => {
    const [formData, setFormData] = useState<Partial<Employee>>({});

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSubmit(formData);
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ mt: 2 }}>
            <Typography variant="h6" sx={{ mb: 2 }}>新增/編輯員工資料</Typography>
            <Box sx={{ display: 'flex', gap: 2, mb: 2, flexDirection: 'row' }}>
                <Box sx={{ flex: 1 }}>
                    <TextField
                        fullWidth
                        label="姓名"
                        required
                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    />
                </Box>
                <Box sx={{ flex: 1 }}>
                    <TextField
                        fullWidth
                        label="信箱"
                        type="email"
                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    />
                </Box>
            </Box>
            <Box sx={{ width: '100%' }}>
                <Button variant="contained" color="primary" type="submit">
                    儲存變更
                </Button>
            </Box>
        </Box>
    );
};
