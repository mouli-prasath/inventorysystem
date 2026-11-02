import { useEffect, useState } from 'react';
import { productApi, categoryApi } from '../../api/catalog';
import { supplierApi } from '../../api/procurement';
import { getErrorMessage } from '../../api/client';
import { Modal, Button, Input } from '../ui';

const EMPTY_FORM = {
  sku: '', name: '', description: '', categoryId: '', brand: '', price: '',
  costPrice: '', stockQuantity: '', reorderPoint: '10', imageUrl: '', supplierId: '',
};

export default function ProductFormModal({ product, onClose, onSaved }) {
  const [categories, setCategories] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    categoryApi.list().then((res) => setCategories(res.data)).catch(() => {});
    supplierApi.list().then((res) => setSuppliers(res.data)).catch(() => {});
  }, []);

  useEffect(() => {
    if (product) {
      setForm({
        sku: product.sku, name: product.name, description: product.description || '',
        categoryId: product.categoryId || '', brand: product.brand || '',
        price: product.price, costPrice: product.costPrice,
        stockQuantity: product.stockQuantity, reorderPoint: product.reorderPoint,
        imageUrl: product.imageUrl || '', supplierId: product.supplierId || '',
      });
    }
  }, [product]);

  const set = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    const payload = {
      ...form,
      categoryId: Number(form.categoryId),
      price: Number(form.price),
      costPrice: Number(form.costPrice),
      stockQuantity: Number(form.stockQuantity),
      reorderPoint: Number(form.reorderPoint),
      supplierId: form.supplierId ? Number(form.supplierId) : null,
    };
    try {
      if (product) {
        await productApi.update(product.id, payload);
      } else {
        await productApi.create(payload);
      }
      onSaved();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal title={product ? 'Edit Product' : 'Add Product'} onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <Input label="SKU" value={form.sku} onChange={set('sku')} required disabled={!!product} />
          <Input label="Brand" value={form.brand} onChange={set('brand')} />
        </div>
        <Input label="Product name" value={form.name} onChange={set('name')} required />
        <Input label="Description" value={form.description} onChange={set('description')} />

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-text-primary">Category</label>
            <select
              value={form.categoryId}
              onChange={set('categoryId')}
              required
              className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="">Select a category</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.categoryName}</option>)}
            </select>
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-text-primary">Supplier</label>
            <select
              value={form.supplierId}
              onChange={set('supplierId')}
              className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="">No supplier assigned</option>
              {suppliers.map((s) => <option key={s.id} value={s.id}>{s.supplierName}</option>)}
            </select>
            <p className="mt-1 text-xs text-text-muted">
              Required for this product to be included in auto-generated restock orders.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Input label="Price" type="number" step="0.01" value={form.price} onChange={set('price')} required />
          <Input label="Cost price" type="number" step="0.01" value={form.costPrice} onChange={set('costPrice')} required />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <Input label="Stock quantity" type="number" value={form.stockQuantity} onChange={set('stockQuantity')} required />
          <Input label="Reorder point" type="number" value={form.reorderPoint} onChange={set('reorderPoint')} />
        </div>
        <Input label="Image URL" value={form.imageUrl} onChange={set('imageUrl')} placeholder="https://…" />

        {error && <p className="text-sm text-status-danger">{error}</p>}

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save Product'}</Button>
        </div>
      </form>
    </Modal>
  );
}