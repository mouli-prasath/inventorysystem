import { useEffect, useState } from 'react';
import { Plus, Pencil, Trash2, Search } from 'lucide-react';
import { productApi } from '../../api/catalog';
import { getErrorMessage } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { MANAGER_ROLES } from '../../constants/roles';
import { PageHeader, Button, Table, TableHead, TableBody, Th, Td, StatusBadge } from '../../components/ui';
import ProductFormModal from '../../components/admin/ProductFormModal';

function stockStatus(product) {
  if (product.stockQuantity <= 0) return 'OUT_OF_STOCK';
  if (product.lowStock) return 'LOW_STOCK';
  return 'IN_STOCK';
}

export default function ProductManagement() {
  const { hasRole } = useAuth();
  const canDelete = hasRole(...MANAGER_ROLES);
  const [products, setProducts] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(true);
  const [editingProduct, setEditingProduct] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [error, setError] = useState('');

  const load = () => {
    setLoading(true);
    const params = { size: 50 };
    if (keyword) params.keyword = keyword;
    productApi.search(params)
      .then((res) => setProducts(res.data.content || []))
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    const timeout = setTimeout(load, 300);
    return () => clearTimeout(timeout);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword]);

  const handleDeactivate = async (product) => {
    if (!confirm(`Deactivate ${product.name}?`)) return;
    try {
      await productApi.deactivate(product.id);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader
        title="Product Management"
        subtitle="Manage your product catalog and pricing."
        action={
          <div className="flex gap-2">
            <Button variant="secondary" disabled title="Bulk import isn't available on the backend yet">Import</Button>
            <Button icon={Plus} onClick={() => setShowCreateModal(true)}>Add Product</Button>
          </div>
        }
      />

      <div className="relative mb-4 max-w-sm">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
        <input
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="Search products…"
          className="w-full rounded-lg border border-border bg-card py-2 pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        />
      </div>

      {error && <p className="mb-4 text-sm text-status-danger">{error}</p>}

      <Table>
        <TableHead>
          <Th>Product</Th>
          <Th>SKU</Th>
          <Th>Category</Th>
          <Th>Price</Th>
          <Th>Stock</Th>
          <Th>Status</Th>
          <Th>Actions</Th>
        </TableHead>
        <TableBody>
          {products.map((p) => (
            <tr key={p.id}>
              <Td className="font-medium">{p.name}</Td>
              <Td>{p.sku}</Td>
              <Td>{p.categoryName}</Td>
              <Td>${Number(p.price).toFixed(2)}</Td>
              <Td>{p.stockQuantity}</Td>
              <Td><StatusBadge status={stockStatus(p)} /></Td>
              <Td>
                <div className="flex gap-2">
                  <button onClick={() => setEditingProduct(p)} className="text-text-secondary hover:text-brand-600">
                    <Pencil size={15} />
                  </button>
                  {canDelete && (
                    <button onClick={() => handleDeactivate(p)} className="text-text-secondary hover:text-status-danger">
                      <Trash2 size={15} />
                    </button>
                  )}
                </div>
              </Td>
            </tr>
          ))}
        </TableBody>
      </Table>

      {!loading && products.length === 0 && (
        <p className="mt-6 text-center text-sm text-text-secondary">No products found.</p>
      )}

      {(showCreateModal || editingProduct) && (
        <ProductFormModal
          product={editingProduct}
          onClose={() => { setShowCreateModal(false); setEditingProduct(null); }}
          onSaved={() => { setShowCreateModal(false); setEditingProduct(null); load(); }}
        />
      )}
    </div>
  );
}