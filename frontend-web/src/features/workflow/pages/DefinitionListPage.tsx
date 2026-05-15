import { useCallback, useEffect, useRef, useState } from 'react';
import { Alert, Box, Button, Card, CardContent, Chip, CircularProgress, Stack, TextField, Typography } from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import { organizationApi } from '../../organization/api/organizationApi';
import { workflowApi } from '../api/workflowApi';
import type { WorkflowDefinition } from '../types';

const getErrorMessage = (error: unknown) => error instanceof Error ? error.message : '作業失敗，請稍後再試';

export const DefinitionListPage = () => {
    const [definitions, setDefinitions] = useState<WorkflowDefinition[]>([]);
    const [loadingDefinitions, setLoadingDefinitions] = useState(true);
    const [loadingEmployee, setLoadingEmployee] = useState(true);
    const [employeeId, setEmployeeId] = useState('');
    const [employeeName, setEmployeeName] = useState('');
    const [error, setError] = useState('');
    const [startingCode, setStartingCode] = useState('');
    const businessSequence = useRef(0);

    const loadDefinitions = useCallback(async () => {
        setLoadingDefinitions(true);
        try {
            const response = await workflowApi.getDefinitions();
            setDefinitions(response.data || []);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setLoadingDefinitions(false);
        }
    }, []);

    useEffect(() => {
        let ignore = false;

        const loadPage = async () => {
            setError('');
            setLoadingEmployee(true);
            await loadDefinitions();

            try {
                const employee = await organizationApi.getCurrentEmployee();
                if (ignore) return;
                setEmployeeId(employee.id);
                setEmployeeName(employee.name);
            } catch (err) {
                if (!ignore) setError(`無法取得目前登入者的員工資料，請手動輸入員工 ID。${getErrorMessage(err)}`);
            } finally {
                if (!ignore) setLoadingEmployee(false);
            }
        };

        loadPage();
        return () => {
            ignore = true;
        };
    }, [loadDefinitions]);

    const handleStartWorkflow = async (code: string) => {
        if (!employeeId) return;

        businessSequence.current += 1;
        setStartingCode(code);
        setError('');
        try {
            const response = await workflowApi.startWorkflow({
                definitionCode: code,
                businessType: 'GENERAL',
                businessId: `BIZ-${Date.now()}-${businessSequence.current}`,
                initiatorId: employeeId,
            });
            setError(`表單已成功發起，流程實例 ID：${response.data}`);
            await loadDefinitions();
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setStartingCode('');
        }
    };

    const loading = loadingDefinitions || loadingEmployee;

    return (
        <Box sx={{ p: 4, maxWidth: 800, margin: '0 auto' }}>
            <Typography variant="h4" sx={{ mb: 4, fontWeight: 'bold' }}>首頁 / 流程表單申請</Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} alignItems={{ xs: 'stretch', sm: 'center' }} sx={{ mb: 2 }}>
                <Chip
                    label={employeeName ? `申請人：${employeeName}` : (loadingEmployee ? '正在讀取申請人' : '未自動帶入申請人')}
                    color={employeeName ? 'primary' : 'default'}
                    variant={employeeName ? 'filled' : 'outlined'}
                />
                <TextField
                    size="small"
                    label="申請員工 ID"
                    value={employeeId}
                    onChange={(event) => {
                        setEmployeeId(event.target.value);
                        setEmployeeName('');
                    }}
                    sx={{ minWidth: { xs: '100%', sm: 360 } }}
                />
            </Stack>
            {error && <Alert severity={error.startsWith('表單已成功') ? 'success' : 'warning'} sx={{ mb: 2 }}>{error}</Alert>}
            {loading && <CircularProgress size={28} sx={{ mb: 2 }} />}
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
                                startIcon={<PlayArrowIcon />}
                                disabled={def.status !== 'PUBLISHED' || !employeeId || Boolean(startingCode)}
                                onClick={() => handleStartWorkflow(def.code)}
                            >
                                {startingCode === def.code ? '發起中' : '發起此流程'}
                            </Button>
                        </CardContent>
                    </Card>
                ))}
                {!loading && definitions.length === 0 && (
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
