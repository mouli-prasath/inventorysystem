import { useEffect, useState } from 'react';
import { categoryApi } from '../../api/catalog';
import { getErrorMessage } from '../../api/client';
import { Modal, Button, Input } from '../ui';

export default function CategoryFormModal({ category, categories, onClose, onSaved }) {
  const [form, setForm] = useState({ categoryName: '', parentCategoryId: '', description: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (category) {
      setForm({
        categoryName: category.categoryName,
        parentCategoryId: category.parentCategoryId || '',
        description: category.description || '',
      });
    }
  }, [category]);

  const set = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    const payload = {
      categoryName: form.categoryName,
      description: form.description,
      parentCategoryId: form.parentCategoryId ? Number(form.parentCategoryId) : null,
    };
    try {
      if (category) {
        await categoryApi.update(category.id, payload);
      } else {
        await categoryApi.create(payload);
      }
      onSaved();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  // A category can't be its own parent.
  const parentOptions = categories.filter((c) => c.id !== category?.id);

  return (
    <Modal title={category ? 'Edit Category' : 'Add Category'} onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        <Input label="Category name" value={form.categoryName} onChange={set('categoryName')} required />
        <div>
          <label className="mb-1.5 block text-sm font-medium text-text-primary">Parent category</label>
          <select
            value={form.parentCategoryId}
            onChange={set('parentCategoryId')}
            className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">None (top-level)</option>
            {parentOptions.map((c) => <option key={c.id} value={c.id}>{c.categoryName}</option>)}
          </select>
        </div>
        <Input label="Description" value={form.description} onChange={set('description')} />

        {error && <p className="text-sm text-status-danger">{error}</p>}

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save Category'}</Button>
        </div>
      </form>
    </Modal>
  );
}
