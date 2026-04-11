import { useEffect, useState } from 'react';
import { Box, Typography, Card, CardContent, CircularProgress, Button, Stack, Chip } from '@mui/material';
import { workflowApi } from '../api/workflowApi';
import type { WorkflowDefinition } from '../types';

export const DefinitionListPage = () => {
    const [definitions, setDefinitions] = useState<WorkflowDefinition[]>([]);
    const [loading, setLoading] = useState(false);

    // 假定目前的登入者 ID
    const currentUserId = 'd290f1ee-6c54-4b01-90e6-d701748f0851';

    useEffect(() => {
        setLoading(true);
        workflowApi.getDefinitions()
            .then(res => setDefinitions(res.data.data || []))
            .catch(console.error)
            .finally(() => setLoading(false));
    }, []);

    const handleStartWorkflow = (code: string) => {
        workflowApi.startWorkflow({
            definitionCode: code,
            businessType: 'GENERAL',
            businessId: `BIZ-${Date.now()}`,
            initiatorId: currentUserId
        })
            .then(res => alert('表單已成功發起！流程實例 ID: ' + res.data))
            .catch(err => alert('發起失敗: ' + err.message));
    };

    if (loading) return <CircularProgress />;

    return (
        <Box sx={{ p: 4, maxWidth: 800, margin: '0 auto' }}>
            <Typography variant="h4" sx={{ mb: 4, fontWeight: 'bold' }}>首頁 / 流程表單申請</Typography>
            <Stack spacing={3}>
                {definitions.map(def => (
                    <Card key={def.id} variant="outlined">
                        <CardContent>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                                <Typography variant="h6">{def.name}</Typography>
                                <Chip label={def.status} color={def.status === 'PUBLISHED' ? 'success' : 'default'} />
                            </Box>
                            <Typography color="text.secondary" sx={{ mb: 2 }}>代碼: {def.code} | 版本: v{def.version}</Typography>
                            <Typography sx={{ mb: 3 }}>{def.description}</Typography>

                            <Button
                                variant="contained"
                                color="primary"
                                disabled={def.status !== 'PUBLISHED'}
                                onClick={() => handleStartWorkflow(def.code)}
                            >
                                發起此流程
                            </Button>
                        </CardContent>
                    </Card>
                ))}
                {definitions.length === 0 && (
                    <Card variant="outlined">
                        <CardContent>
                            <Typography color="text.secondary" align="center">
                                目前伺服器中沒有已發布的表單模板可供申請。
                            </Typography>
                        </CardContent>
                    </Card>
                )}
            </Stack>
        </Box>
    );
};
