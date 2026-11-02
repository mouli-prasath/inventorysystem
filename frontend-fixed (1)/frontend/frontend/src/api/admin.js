import apiClient from './client';

export const notificationApi = {
  list: (params) => apiClient.get('/notifications', { params }),
  unreadCount: () => apiClient.get('/notifications/unread-count'),
  markRead: (id) => apiClient.put(`/notifications/${id}/read`),
  markAllRead: () => apiClient.put('/notifications/read-all'),
  broadcast: (data) => apiClient.post('/admin/notifications', data),
};

export const reportApi = {
  salesSummary: (start, end) => apiClient.get('/reports/sales/summary', { params: { start, end } }),
  dailySales: (date) => apiClient.get('/reports/sales/daily', { params: { date } }),
  topProducts: (start, end, limit = 10) =>
    apiClient.get('/reports/sales/top-products', { params: { start, end, limit } }),
  inventoryValuation: () => apiClient.get('/reports/inventory/valuation'),
  profitMargins: (limit = 20, lowestFirst = false) =>
    apiClient.get('/reports/inventory/margins', { params: { limit, lowestFirst } }),
};

export const adminApi = {
  analytics: () => apiClient.get('/admin/analytics'),
  reports: () => apiClient.get('/admin/reports'),
  systemHealth: () => apiClient.get('/admin/system-health'),
  auditLogs: (params) => apiClient.get('/admin/audit-logs', { params }),
  auditLogsByUser: (userId, params) => apiClient.get(`/admin/audit-logs/user/${userId}`, { params }),
  auditLogsByAction: (action, params) => apiClient.get(`/admin/audit-logs/action/${action}`, { params }),
};
