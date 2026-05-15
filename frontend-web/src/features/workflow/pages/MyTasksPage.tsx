import { useCallback, useEffect, useState } from 'react';
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    CircularProgress,
    Stack,
    TextField,
    Tooltip,
    Typography,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import RefreshIcon from '@mui/icons-material/Refresh';
import UndoIcon from '@mui/icons-material/Undo';
import { organizationApi } from '../../organization/api/organizationApi';
import { workflowApi } from '../api/workflowApi';
import type { WorkflowTask } from '../types';

const getErrorMessage = (error: unknown) => error instanceof Error ? error.message : '作業失敗，請稍後再試';

export const MyTasksPage = () => {
    const [tasks, setTasks] = useState<WorkflowTask[]>([]);
    const [employeeId, setEmployeeId] = useState('');
    const [employeeName, setEmployeeName] = useState('');
    const [loadingEmployee, setLoadingEmployee] = useState(true);
    const [loadingTasks, setLoadingTasks] = useState(false);
    const [commentByTask, setCommentByTask] = useState<Record<string, string>>({});
    const [actionTaskId, setActionTaskId] = useState('');
    const [message, setMessage] = useState('');

    const fetchTasks = useCallback(async (targetEmployeeId: string, showLoading = true) => {
        if (!targetEmployeeId) return;
        if (showLoading) setLoadingTasks(true);
        setMessage('');
        try {
            const response = await workflowApi.getMyTasks(targetEmployeeId);
            setTasks(response.data || []);
        } catch (err) {
            setMessage(getErrorMessage(err));
        } finally {
            if (showLoading) setLoadingTasks(false);
        }
    }, []);

    useEffect(() => {
        let ignore = false;

        const loadPage = async () => {
            setLoadingEmployee(true);
            setLoadingTasks(true);
            setMessage('');
            try {
                const employee = await organizationApi.getCurrentEmployee();
                if (ignore) return;
                setEmployeeId(employee.id);
                setEmployeeName(employee.name);
                await fetchTasks(employee.id, false);
            } catch (err) {
                if (!ignore) setMessage(`無法取得目前登入者的員工資料，請手動輸入員工 ID 後重新整理。${getErrorMessage(err)}`);
            } finally {
                if (!ignore) {
                    setLoadingEmployee(false);
                    setLoadingTasks(false);
                }
            }
        };

        loadPage();
        return () => {
            ignore = true;
        };
    }, [fetchTasks]);

    const handleApprove = async (taskId: string) => {
        if (!employeeId) return;
        setActionTaskId(taskId);
        setMessage('');
        try {
            await workflowApi.approveTask(taskId, { operatorId: employeeId, comment: commentByTask[taskId] });
            setMessage('待辦已核准');
            await fetchTasks(employeeId);
        } catch (err) {
            setMessage(getErrorMessage(err));
        } finally {
            setActionTaskId('');
        }
    };

    const handleReject = async (taskId: string) => {
        if (!employeeId) return;
        setActionTaskId(taskId);
        setMessage('');
        try {
            await workflowApi.rejectTask(taskId, { operatorId: employeeId, comment: commentByTask[taskId] });
            setMessage('待辦已駁回');
            await fetchTasks(employeeId);
        } catch (err) {
            setMessage(getErrorMessage(err));
        } finally {
            setActionTaskId('');
        }
    };

    const loading = loadingEmployee || loadingTasks;

    return (
        <Box sx={{ p: 4, maxWidth: 800, margin: '0 auto' }}>
            <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" spacing={2} sx={{ mb: 3 }}>
                <Box>
                    <Typography variant="h4" sx={{ mb: 1, fontWeight: 'bold' }}>首頁 / 我的待辦簽核</Typography>
                    <Chip
                        label={employeeName ? `審核人：${employeeName}` : (loadingEmployee ? '正在讀取審核人' : '未自動帶入審核人')}
                        color={employeeName ? 'primary' : 'default'}
                        variant={employeeName ? 'filled' : 'outlined'}
                    />
                    <TextField
                        size="small"
                        label="審核員工 ID"
                        value={employeeId}
                        onChange={(event) => {
                            setEmployeeId(event.target.value);
                            setEmployeeName('');
                        }}
                        sx={{ mt: 1, minWidth: { xs: '100%', sm: 360 } }}
                    />
                </Box>
                <Tooltip title="重新整理待辦">
                    <span>
                        <Button
                            variant="outlined"
                            startIcon={<RefreshIcon />}
                            disabled={!employeeId || loading}
                            onClick={() => fetchTasks(employeeId)}
                        >
                            重新整理
                        </Button>
                    </span>
                </Tooltip>
            </Stack>
            {message && (
                <Alert severity={message.includes('已') ? 'success' : 'warning'} sx={{ mb: 2 }}>
                    {message}
                </Alert>
            )}
            {loading && <CircularProgress size={28} sx={{ mb: 2 }} />}
            <Stack spacing={3}>
                {tasks.map(task => (
                    <Card key={task.id} variant="outlined">
                        <CardContent>
                            <Typography variant="h6">追蹤實例 ID: {task.instanceId}</Typography>
                            <Typography color="text.secondary" sx={{ mb: 2 }}>節點代碼: {task.nodeId}</Typography>

                            <TextField
                                fullWidth
                                multiline
                                rows={2}
                                label="審核意見 (選填)"
                                variant="outlined"
                                value={commentByTask[task.id] || ''}
                                onChange={(e) => setCommentByTask(prev => ({ ...prev, [task.id]: e.target.value }))}
                                sx={{ mb: 2 }}
                            />

                            <Box sx={{ display: 'flex', gap: 2 }}>
                                <Button
                                    variant="contained"
                                    color="success"
                                    startIcon={<CheckCircleIcon />}
                                    disabled={Boolean(actionTaskId)}
                                    onClick={() => handleApprove(task.id)}
                                >
                                    {actionTaskId === task.id ? '處理中' : '同意過卡'}
                                </Button>
                                <Button
                                    variant="contained"
                                    color="error"
                                    startIcon={<UndoIcon />}
                                    disabled={Boolean(actionTaskId)}
                                    onClick={() => handleReject(task.id)}
                                >
                                    退件駁回
                                </Button>
                            </Box>
                        </CardContent>
                    </Card>
                ))}
                {!loading && tasks.length === 0 && (
                    <Card variant="outlined">
                        <CardContent>
                            <Typography color="text.secondary" align="center">
                                太棒了！您目前沒有任何待處理的表單簽核任務。
                            </Typography>
                        </CardContent>
                    </Card>
                )}
            </Stack>
        </Box>
    );
};
