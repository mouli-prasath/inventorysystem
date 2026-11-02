import { useEffect, useState } from 'react';
import { productApi } from '../../api/catalog';
import { inventoryApi } from '../../api/inventory';
import { getErrorMessage } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { Modal, Button, Input } from '../ui';

const MOVEMENT_TYPES = ['STOCK_IN', 'STOCK_OUT', 'ADJUSTMENT', 'DAMAGE', 'THEFT'];

export default function StockAdjustmentModal({ onClose, onSaved }) {
  const { hasRole } = useAuth();
  const canOverride = hasRole('STORE_MANAGER', 'ADMIN');

  const [mode, setMode] = useState('adjustment'); // 'adjustment' | 'cycleCount'
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState({
    productId: '', storeLocation: '', movementType: 'STOCK_IN',
    quantity: '', countedQuantity: '', reference: '', notes: '', managerOverride: false,
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    productApi.search({ size: 100 }).then((res) => setProducts(res.data.content || [])).catch(() => {});
  }, []);

  const set = (field) => (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm((f) => ({ ...f, [field]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (mode === 'cycleCount') {
        await inventoryApi.cycleCount({
          productId: Number(form.productId),
          storeLocation: form.storeLocation,
          countedQuantity: Number(form.countedQuantity),
          notes: form.notes,
        });
      } else {
        await inventoryApi.adjust({
          productId: Number(form.productId),
          storeLocation: form.storeLocation,
          movementType: form.movementType,
          quantity: Number(form.quantity),
          reference: form.reference,
          notes: form.notes,
          managerOverride: form.managerOverride,
        });
      }
      onSaved();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal title="Adjust Stock" onClose={onClose}>
      <div className="mb-4 flex gap-2 rounded-lg bg-surface p-1">
        <button
          type="button"
          onClick={() => setMode('adjustment')}
          className={`flex-1 rounded-md py-1.5 text-sm font-medium ${mode === 'adjustment' ? 'bg-card shadow-sm' : 'text-text-secondary'}`}
        >
          Adjustment
        </button>
        <button
          type="button"
          onClick={() => setMode('cycleCount')}
          className={`flex-1 rounded-md py-1.5 text-sm font-medium ${mode === 'cycleCount' ? 'bg-card shadow-sm' : 'text-text-secondary'}`}
        >
          Physical Cycle Count
        </button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-1.5 block text-sm font-medium text-text-primary">Product</label>
          <select
            value={form.productId}
            onChange={set('productId')}
            required
            className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">Select a product</option>
            {products.map((p) => <option key={p.id} value={p.id}>{p.name} ({p.sku})</option>)}
          </select>
        </div>

        <Input label="Store location" value={form.storeLocation} onChange={set('storeLocation')} required placeholder="Downtown" />

        {mode === 'cycleCount' ? (
          <>
            <Input
              label="Counted quantity"
              type="number"
              min="0"
              value={form.countedQuantity}
              onChange={set('countedQuantity')}
              required
            />
            <p className="-mt-2 text-xs text-text-secondary">
              Enter the actual physical count — the system computes the correction automatically.
            </p>
          </>
        ) : (
          <>
            <div>
              <label className="mb-1.5 block text-sm font-medium text-text-primary">Movement type</label>
              <select
                value={form.movementType}
                onChange={set('movementType')}
                className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              >
                {MOVEMENT_TYPES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
              </select>
            </div>
            <Input label="Quantity" type="number" min="1" value={form.quantity} onChange={set('quantity')} required />
            <Input label="Reference" value={form.reference} onChange={set('reference')} placeholder="e.g. PO-1001" />

            {canOverride && (
              <label className="flex items-center gap-2 text-sm text-text-secondary">
                <input type="checkbox" checked={form.managerOverride} onChange={set('managerOverride')} />
                Allow this to push stock below zero (manager override)
              </label>
            )}
          </>
        )}

        <Input label="Notes" value={form.notes} onChange={set('notes')} />

        {error && <p className="text-sm text-status-danger">{error}</p>}

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" disabled={saving}>{saving ? 'Saving…' : 'Submit'}</Button>
        </div>
      </form>
    </Modal>
  );
}
