import apiClient from './client';

export const categoryApi = {
  list: () => apiClient.get('/categories'),
  topLevel: () => apiClient.get('/categories/top-level'),
  getById: (id) => apiClient.get(`/categories/${id}`),
  create: (data) => apiClient.post('/categories', data),
  update: (id, data) => apiClient.put(`/categories/${id}`, data),
  deactivate: (id) => apiClient.delete(`/categories/${id}`),
};

export const productApi = {
  search: (params) => apiClient.get('/products', { params }),
  getById: (id) => apiClient.get(`/products/${id}`),
  lowStock: () => apiClient.get('/products/low-stock'),
  create: (data) => apiClient.post('/products', data),
  update: (id, data) => apiClient.put(`/products/${id}`, data),
  deactivate: (id) => apiClient.delete(`/products/${id}`),
};

export const promotionApi = {
  list: () => apiClient.get('/promotions'),
  active: () => apiClient.get('/promotions/active'),
  getById: (id) => apiClient.get(`/promotions/${id}`),
  create: (data) => apiClient.post('/promotions', data),
  update: (id, data) => apiClient.put(`/promotions/${id}`, data),
  deactivate: (id) => apiClient.delete(`/promotions/${id}`),
};
