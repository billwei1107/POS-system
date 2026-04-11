import { useEffect, useState } from 'react';
import { Box, Typography, Card, CardContent } from '@mui/material';
import { organizationApi } from '../api/organizationApi';
import type { Company } from '../types';

export const CompanyPage = () => {
    const [companies, setCompanies] = useState<Company[]>([]);

    useEffect(() => {
        organizationApi.getCompanies().then(res => {
            setCompanies(res.data || []);
        }).catch(console.error);
    }, []);

    return (
        <Box sx={{ p: 4 }}>
            <Typography variant="h5" sx={{ mb: 3, fontWeight: 'bold' }}>
                企業與公司列表
            </Typography>
            {companies.map(c => (
                <Card key={c.id} sx={{ mb: 2 }}>
                    <CardContent>
                        <Typography variant="h6">{c.name} ({c.code})</Typography>
                        <Typography color="textSecondary">負責人：{c.legalPerson || '未設定'}</Typography>
                    </CardContent>
                </Card>
            ))}
        </Box>
    );
};
