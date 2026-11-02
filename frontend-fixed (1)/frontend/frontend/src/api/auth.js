import apiClient from './client';

export const authApi = {
  register: (data) => apiClient.post('/auth/register', data),
  login: (data) => apiClient.post('/auth/login', data),
  refresh: (refreshToken) => apiClient.post('/auth/refresh', { refreshToken }),
  logout: () => apiClient.post('/auth/logout'),
  forgotPassword: (identifier) => apiClient.post('/auth/forgot-password', { identifier }),
  resetPassword: (token, newPassword) => apiClient.post('/auth/reset-password', { token, newPassword }),
};

export const userApi = {
  getProfile: () => apiClient.get('/users/profile'),
  updateProfile: (data) => apiClient.put('/users/profile', data),
  changePassword: (data) => apiClient.post('/users/change-password', data),
};

export const adminUserApi = {
  list: (params) => apiClient.get('/admin/users', { params }),
  getById: (id) => apiClient.get(`/admin/users/${id}`),
  deactivate: (id) => apiClient.put(`/admin/users/${id}/deactivate`),
  reactivate: (id) => apiClient.put(`/admin/users/${id}/reactivate`),
  unlock: (id) => apiClient.put(`/admin/users/${id}/unlock`),
  changeRole: (id, role) => apiClient.put(`/admin/users/${id}/role`, { role }),
};
