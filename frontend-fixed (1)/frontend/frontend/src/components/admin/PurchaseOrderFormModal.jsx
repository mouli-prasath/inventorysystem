import { useEffect, useState } from 'react';
import { Trash2, Plus } from 'lucide-react';
import { supplierApi } from '../../api/procurement';
import { productApi } from '../../api/catalog';
import { purchaseOrderApi } from '../../api/procurement';
import { getErrorMessage } from '../../api/client';
import { Modal, Button, Input } from '../ui';

export default function PurchaseOrderFormModal({ onClose, onSaved }) {
  const [suppliers, setSuppliers] = useState([]);
  const [products, setProducts] = useState([]);
  const [supplierId, setSupplierId] = useState('');
  const [storeLocation, setStoreLocation] = useState('Downtown');
  const [items, setItems] = useState([{ productId: '', quantityOrdered: '', unitCost: '' }]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    supplierApi.list(true).then((res) => setSuppliers(res.data)).catch(() => {});
    productApi.search({ size: 100 }).then((res) => setProducts(res.data.content || [])).catch(() => {});
  }, []);

  const updateItem = (index, field, value) => {
    setItems((prev) => prev.map((it, i) => i === index ? { ...it, [field]: value } : it));
  };

  const addItem = () => setItems((prev) => [...prev, { productId: '', quantityOrdered: '', unitCost: '' }]);
  const removeItem = (index) => setItems((prev) => prev.filter((_, i) => i !== index));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await purchaseOrderApi.create({
        supplierId: Number(supplierId),
        storeLocation,
        items: items.map((it) => ({
          productId: Number(it.productId),
          quantityOrdered: Number(it.quantityOrdered),
          unitCost: Number(it.unitCost),
        })),
      });
      onSaved();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal title="New Purchase Order" onClose={onClose} maxWidth="max-w-2xl">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-1.5 block text-sm font-medium text-text-primary">Supplier</label>
          <select
            value={supplierId}
            onChange={(e) => setSupplierId(e.target.value)}
            required
            className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">Select a supplier</option>
            {suppliers.map((s) => <option key={s.id} value={s.id}>{s.supplierName}</option>)}
          </select>
        </div>
        <Input label="Store location" value={storeLocation} onChange={(e) => setStoreLocation(e.target.value)} required />

        <div>
          <div className="mb-2 flex items-center justify-between">
            <label className="text-sm font-medium text-text-primary">Line items</label>
            <button type="button" onClick={addItem} className="flex items-center gap-1 text-xs text-brand-600 hover:underline">
              <Plus size={13} /> Add item
            </button>
          </div>
          <div className="space-y-2">
            {items.map((item, i) => (
              <div key={i} className="flex items-center gap-2">
                <select
                  value={item.productId}
                  onChange={(e) => updateItem(i, 'productId', e.target.value)}
                  required
                  className="flex-1 rounded-lg border border-border px-2 py-1.5 text-sm"
                >
                  <option value="">Product…</option>
                  {products.map((p) => <option key={p.id} value={p.id}>{p.name} ({p.sku})</option>)}
                </select>
                <input
                  type="number" min="1" placeholder="Qty" required
                  value={item.quantityOrdered}
                  onChange={(e) => updateItem(i, 'quantityOrdered', e.target.value)}
                  className="w-20 rounded-lg border border-border px-2 py-1.5 text-sm"
                />
                <input
                  type="number" min="0" step="0.01" placeholder="Unit cost" required
                  value={item.unitCost}
                  onChange={(e) => updateItem(i, 'unitCost', e.target.value)}
                  className="w-28 rounded-lg border border-border px-2 py-1.5 text-sm"
                />
                {items.length > 1 && (
                  <button type="button" onClick={() => removeItem(i)} className="text-text-secondary hover:text-status-danger">
                    <Trash2 size={15} />
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>

        {error && <p className="text-sm text-status-danger">{error}</p>}

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" disabled={saving}>{saving ? 'Creating…' : 'Create Purchase Order'}</Button>
        </div>
      </form>
    </Modal>
  );
}
