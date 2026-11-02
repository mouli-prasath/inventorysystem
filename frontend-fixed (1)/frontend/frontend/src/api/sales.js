import apiClient from './client';

export const transactionApi = {
  processSale: (data) => apiClient.post('/transactions', data),
  getById: (id) => apiClient.get(`/transactions/${id}`),
  getByNumber: (transactionNumber) => apiClient.get(`/transactions/number/${transactionNumber}`),
  getByCustomer: (customerId, params) => apiClient.get(`/transactions/customer/${customerId}`, { params }),
  getDailyReport: (start, end, params) =>
    apiClient.get('/transactions/report/daily', { params: { start, end, ...params } }),
  void: (id) => apiClient.put(`/transactions/${id}/void`),
  processReturn: (id, data) => apiClient.post(`/transactions/${id}/return`, data),
};

export const customerApi = {
  search: (params) => apiClient.get('/customers', { params }),
  getById: (id) => apiClient.get(`/customers/${id}`),
  getHistory: (id, params) => apiClient.get(`/customers/${id}/history`, { params }),
  create: (data) => apiClient.post('/customers', data),
  update: (id, data) => apiClient.put(`/customers/${id}`, data),
  adjustLoyalty: (id, pointsDelta, reason) =>
    apiClient.post(`/customers/${id}/loyalty`, { pointsDelta, reason }),
  deactivate: (id) => apiClient.delete(`/customers/${id}`),
};
