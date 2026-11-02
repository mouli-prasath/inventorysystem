import { useEffect, useState } from 'react';
import { promotionApi, categoryApi, productApi } from '../../api/catalog';
import { getErrorMessage } from '../../api/client';
import { Modal, Button, Input } from '../ui';

function toDatetimeLocal(iso) {
  if (!iso) return '';
  return iso.slice(0, 16);
}

export default function PromotionFormModal({ promotion, onClose, onSaved }) {
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [scopeType, setScopeType] = useState('STORE');
  const [form, setForm] = useState({
    promotionName: '', discountType: 'PERCENTAGE', discountValue: '',
    categoryScopeId: '', productScopeId: '', startDate: '', endDate: '', description: '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    categoryApi.list().then((res) => setCategories(res.data)).catch(() => {});
    productApi.search({ size: 100 }).then((res) => setProducts(res.data.content || [])).catch(() => {});
  }, []);

  useEffect(() => {
    if (promotion) {
      setForm({
        promotionName: promotion.promotionName,
        discountType: promotion.discountType,
        discountValue: promotion.discountValue,
        categoryScopeId: promotion.categoryScopeId || '',
        productScopeId: promotion.productScopeId || '',
        startDate: toDatetimeLocal(promotion.startDate),
        endDate: toDatetimeLocal(promotion.endDate),
        description: promotion.description || '',
      });
      setScopeType(promotion.productScopeId ? 'PRODUCT' : promotion.categoryScopeId ? 'CATEGORY' : 'STORE');
    }
  }, [promotion]);

  const set = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    const payload = {
      promotionName: form.promotionName,
      discountType: form.discountType,
      discountValue: Number(form.discountValue),
      categoryScopeId: scopeType === 'CATEGORY' && form.categoryScopeId ? Number(form.categoryScopeId) : null,
      productScopeId: scopeType === 'PRODUCT' && form.productScopeId ? Number(form.productScopeId) : null,
      startDate: `${form.startDate}:00`,
      endDate: `${form.endDate}:00`,
      description: form.description,
    };
    try {
      if (promotion) {
        await promotionApi.update(promotion.id, payload);
      } else {
        await promotionApi.create(payload);
      }
      onSaved();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal title={promotion ? 'Edit Promotion' : 'Add Promotion'} onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        <Input label="Promotion name" value={form.promotionName} onChange={set('promotionName')} required />

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-text-primary">Discount type</label>
            <select
              value={form.discountType}
              onChange={set('discountType')}
              className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="PERCENTAGE">Percentage</option>
              <option value="FIXED_AMOUNT">Fixed amount</option>
            </select>
          </div>
          <Input
            label={form.discountType === 'PERCENTAGE' ? 'Discount %' : 'Discount $'}
            type="number" step="0.01" min="0"
            max={form.discountType === 'PERCENTAGE' ? 100 : undefined}
            value={form.discountValue} onChange={set('discountValue')} required
          />
        </div>

        <div>
          <label className="mb-1.5 block text-sm font-medium text-text-primary">Applies to</label>
          <select
            value={scopeType}
            onChange={(e) => setScopeType(e.target.value)}
            className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="STORE">Entire store</option>
            <option value="CATEGORY">A specific category</option>
            <option value="PRODUCT">A specific product</option>
          </select>
        </div>

        {scopeType === 'CATEGORY' && (
          <select
            value={form.categoryScopeId}
            onChange={set('categoryScopeId')}
            required
            className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">Select a category</option>
            {categories.map((c) => <option key={c.id} value={c.id}>{c.categoryName}</option>)}
          </select>
        )}
        {scopeType === 'PRODUCT' && (
          <select
            value={form.productScopeId}
            onChange={set('productScopeId')}
            required
            className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">Select a product</option>
            {products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        )}

        <div className="grid grid-cols-2 gap-4">
          <Input label="Start date" type="datetime-local" value={form.startDate} onChange={set('startDate')} required />
          <Input label="End date" type="datetime-local" value={form.endDate} onChange={set('endDate')} required />
        </div>
        <Input label="Description" value={form.description} onChange={set('description')} />

        {error && <p className="text-sm text-status-danger">{error}</p>}

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save Promotion'}</Button>
        </div>
      </form>
    </Modal>
  );
}
