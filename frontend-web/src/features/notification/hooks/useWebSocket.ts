import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuthStore } from '../../../shared/store/authStore';

interface UseWebSocketOptions {
    onUnreadCountUpdate?: (count: number) => void;
    onNewNotification?: (notification: any) => void;
}

export const useWebSocket = (options: UseWebSocketOptions) => {
    const { user, token } = useAuthStore();
    const clientRef = useRef<Client | null>(null);

    useEffect(() => {
        if (!user || !user.id || !token) return;

        // 連接後端配置的 ws 端點 (降級使用 SockJS 以提升相容性)
        const WS_URL = `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/ws/notifications`;

        const client = new Client({
            webSocketFactory: () => new SockJS(WS_URL),
            connectHeaders: {
                Authorization: `Bearer ${token}`
            },
            debug: () => {
                // 可在此打開 Debug 模式以追蹤心跳包
                // console.log('[STOMP]', str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = () => {
            console.log('Connected to WebSocket Notification Server!');

            // 訂閱專屬用戶的未讀數字
            client.subscribe(`/user/${user.id}/queue/notifications/count`, (message) => {
                if (options.onUnreadCountUpdate) {
                    options.onUnreadCountUpdate(Number(message.body));
                }
            });

            // 訂閱專屬用戶的新推播實體
            client.subscribe(`/user/${user.id}/queue/notifications/new`, (message) => {
                if (options.onNewNotification) {
                    options.onNewNotification(JSON.parse(message.body));
                }
            });
        };

        client.activate();
        clientRef.current = client;

        return () => {
            if (clientRef.current) {
                clientRef.current.deactivate();
            }
        };
    }, [user, token]);

    return clientRef.current;
};
