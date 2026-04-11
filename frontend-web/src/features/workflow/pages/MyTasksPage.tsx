import { useEffect, useState } from 'react';
import { Box, Typography, Card, CardContent, CircularProgress, Button, Stack, TextField } from '@mui/material';
import { workflowApi } from '../api/workflowApi';
import type { WorkflowTask } from '../types';

export const MyTasksPage = () => {
    const [tasks, setTasks] = useState<WorkflowTask[]>([]);
    const [loading, setLoading] = useState(false);
    const [comment, setComment] = useState('');

    // 未來應與認證模塊 Token 繫結以取得此 operatorId
    const currentUserId = 'd290f1ee-6c54-4b01-90e6-d701748f0851';

    const fetchTasks = () => {
        setLoading(true);
        workflowApi.getMyTasks(currentUserId)
            .then(res => setTasks(res.data.data || []))
            .catch(console.error)
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        fetchTasks();
    }, []);

    const handleApprove = (taskId: string) => {
        workflowApi.approveTask(taskId, { operatorId: currentUserId, comment })
            .then(() => fetchTasks())
            .catch(console.error);
    };

    const handleReject = (taskId: string) => {
        workflowApi.rejectTask(taskId, { operatorId: currentUserId, comment })
            .then(() => fetchTasks())
            .catch(console.error);
    };

    if (loading) return <CircularProgress />;

    return (
        <Box sx={{ p: 4, maxWidth: 800, margin: '0 auto' }}>
            <Typography variant="h4" sx={{ mb: 4, fontWeight: 'bold' }}>首頁 / 我的待辦簽核</Typography>
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
                                value={comment}
                                onChange={(e) => setComment(e.target.value)}
                                sx={{ mb: 2 }}
                            />

                            <Box sx={{ display: 'flex', gap: 2 }}>
                                <Button
                                    variant="contained"
                                    color="success"
                                    onClick={() => handleApprove(task.id)}
                                >
                                    同意過卡
                                </Button>
                                <Button
                                    variant="contained"
                                    color="error"
                                    onClick={() => handleReject(task.id)}
                                >
                                    退件駁回
                                </Button>
                            </Box>
                        </CardContent>
                    </Card>
                ))}
                {tasks.length === 0 && (
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
