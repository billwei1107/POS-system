import React, { useState, useEffect } from 'react';
import { Badge, IconButton, Popover, List, ListItem, ListItemText, Typography, Box, Button } from '@mui/material';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { useWebSocket } from '../hooks/useWebSocket';
import { getUnreadCount, getUnreadNotifications, markAsRead, markAllAsRead, testPush } from '../api/notificationApi';
import type { NotificationDTO } from '../types';

export const NotificationBell: React.FC = () => {
    const [unreadCount, setUnreadCount] = useState(0);
    const [notifications, setNotifications] = useState<NotificationDTO[]>([]);
    const [anchorEl, setAnchorEl] = useState<HTMLButtonElement | null>(null);

    // Initial load for accurate initial count before WS arrives
    useEffect(() => {
        getUnreadCount().then(setUnreadCount).catch(console.error);
    }, []);

    // Fetch details strictly on popover open
    useEffect(() => {
        if (anchorEl) {
            getUnreadNotifications(0, 10).then(setNotifications).catch(console.error);
        }
    }, [anchorEl]);

    // WebSocket pushing hook connection
    useWebSocket({
        onUnreadCountUpdate: (count) => setUnreadCount(count),
        onNewNotification: (noti) => {
            // Push incoming alert to the top if user keeps dropdown open
            setNotifications(prev => [noti, ...prev].slice(0, 10));
        }
    });

    const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
        setAnchorEl(event.currentTarget);
    };

    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleNotiClick = async (id: string) => {
        await markAsRead(id);
        setUnreadCount(prev => Math.max(0, prev - 1));
        setNotifications(prev => prev.filter(n => n.id !== id));
    };

    const open = Boolean(anchorEl);
    const id = open ? 'notification-popover' : undefined;

    return (
        <>
            <IconButton color="inherit" onClick={handleClick} aria-describedby={id}>
                <Badge badgeContent={unreadCount} color="error">
                    <NotificationsIcon />
                </Badge>
            </IconButton>

            <Popover
                id={id}
                open={open}
                anchorEl={anchorEl}
                onClose={handleClose}
                anchorOrigin={{
                    vertical: 'bottom',
                    horizontal: 'right',
                }}
                transformOrigin={{
                    vertical: 'top',
                    horizontal: 'right',
                }}
            >
                <Box sx={{ p: 2, pb: 1, minWidth: 300, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="h6" sx={{ fontSize: '1.2rem', fontWeight: 600 }}>推播中心</Typography>
                    <Box>
                        {/* 臨時的體驗測試鈕，可點擊測試 websocket 流路徑是否打通 */}
                        <Button size="small" onClick={() => testPush()} color="secondary">
                            [測試推播]
                        </Button>
                        {unreadCount > 0 && (
                            <Button size="small" onClick={async () => { await markAllAsRead(); setUnreadCount(0); setNotifications([]); handleClose(); }}>
                                全部標為已讀
                            </Button>
                        )}
                    </Box>
                </Box>
                <List sx={{ width: '100%', minWidth: 320, bgcolor: 'background.paper', maxHeight: 400, overflow: 'auto' }}>
                    {notifications.length === 0 ? (
                        <ListItem>
                            <ListItemText primary="目前沒有新通知" secondary="當系統有新的簽核或異動將會即時發送" />
                        </ListItem>
                    ) : (
                        notifications.map((noti) => (
                            <ListItem key={noti.id} component="div" sx={{
                                cursor: 'pointer',
                                borderBottom: '1px solid #f0f0f0',
                                '&:hover': { bgcolor: 'action.hover' }
                            }} onClick={() => handleNotiClick(noti.id)}>
                                <ListItemText
                                    primary={noti.title}
                                    secondary={noti.content}
                                    primaryTypographyProps={{ fontWeight: 600, fontSize: '0.95rem' }}
                                    secondaryTypographyProps={{
                                        color: 'text.secondary',
                                        fontSize: '0.85rem'
                                    }}
                                />
                            </ListItem>
                        ))
                    )}
                </List>
            </Popover>
        </>
    );
};
