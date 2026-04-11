export interface NotificationDTO {
    id: string;
    title: string;
    content: string;
    status: 'UNREAD' | 'READ';
    sentAt: string;
    readAt?: string;
}
