import axiosInstance from '../../../shared/api/axiosInstance';

export const getUnreadNotifications = async (page = 0, size = 10) => {
    const response = await axiosInstance.get('/v1/notifications', { params: { page, size } });
    return response.data;
};

export const getUnreadCount = async () => {
    const response = await axiosInstance.get('/v1/notifications/count');
    return response.data;
};

export const markAsRead = async (id: string) => {
    await axiosInstance.put(`/v1/notifications/${id}/read`);
};

export const markAllAsRead = async () => {
    await axiosInstance.put('/v1/notifications/read-all');
};

export const testPush = async () => {
    await axiosInstance.post('/v1/notifications/test-push');
};
