import apiClient from './client';

export const inventoryApi = {
  adjust: (data) => apiClient.post('/inventory/adjustment', data),
  cycleCount: (data) => apiClient.post('/inventory/count', data),
  getByProduct: (productId) => apiClient.get(`/inventory/product/${productId}`),
  getByLocation: (storeLocation) => apiClient.get(`/inventory/location/${storeLocation}`),
  getAllStock: (params) => apiClient.get('/inventory/stock', { params }),
  lowStock: () => apiClient.get('/inventory/low-stock'),
  updateReorderPoint: (productId, reorderPoint) =>
    apiClient.put('/inventory/reorder', { productId, reorderPoint }),
};
