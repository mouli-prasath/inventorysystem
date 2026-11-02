import apiClient from './client';

export const supplierApi = {
  list: (activeOnly = true) => apiClient.get('/suppliers', { params: { activeOnly } }),
  search: (name) => apiClient.get('/suppliers/search', { params: { name } }),
  getById: (id) => apiClient.get(`/suppliers/${id}`),
  create: (data) => apiClient.post('/suppliers', data),
  update: (id, data) => apiClient.put(`/suppliers/${id}`, data),
  deactivate: (id) => apiClient.delete(`/suppliers/${id}`),
};

export const purchaseOrderApi = {
  list: (params) => apiClient.get('/purchase-orders', { params }),
  getById: (id) => apiClient.get(`/purchase-orders/${id}`),
  create: (data) => apiClient.post('/purchase-orders', data),
  autoGenerate: () => apiClient.post('/purchase-orders/auto-generate'),
  approve: (id) => apiClient.put(`/purchase-orders/${id}/approve`),
  markOrdered: (id) => apiClient.put(`/purchase-orders/${id}/order`),
  receive: (id, receivedItems) => apiClient.put(`/purchase-orders/${id}/receive`, { receivedItems }),
  cancel: (id) => apiClient.put(`/purchase-orders/${id}/cancel`),
};
