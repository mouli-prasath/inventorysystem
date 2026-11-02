import { useEffect, useState } from 'react';
import { supplierApi } from '../../api/procurement';
import { getErrorMessage } from '../../api/client';
import { Modal, Button, Input } from '../ui';

export default function SupplierFormModal({ supplier, onClose, onSaved }) {
  const [form, setForm] = useState({ supplierName: '', contactPerson: '', email: '', phone: '', address: '', paymentTerms: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (supplier) {
      setForm({
        supplierName: supplier.supplierName, contactPerson: supplier.contactPerson || '',
        email: supplier.email || '', phone: supplier.phone || '',
        address: supplier.address || '', paymentTerms: supplier.paymentTerms || '',
      });
    }
  }, [supplier]);

  const set = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (supplier) {
        await supplierApi.update(supplier.id, form);
      } else {
        await supplierApi.create(form);
      }
      onSaved();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal title={supplier ? 'Edit Supplier' : 'Add Supplier'} onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        <Input label="Supplier name" value={form.supplierName} onChange={set('supplierName')} required />
        <Input label="Contact person" value={form.contactPerson} onChange={set('contactPerson')} />
        <div className="grid grid-cols-2 gap-4">
          <Input label="Email" type="email" value={form.email} onChange={set('email')} />
          <Input label="Phone" value={form.phone} onChange={set('phone')} />
        </div>
        <Input label="Address" value={form.address} onChange={set('address')} />
        <Input label="Payment terms" value={form.paymentTerms} onChange={set('paymentTerms')} placeholder="Net 30" />

        {error && <p className="text-sm text-status-danger">{error}</p>}

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save Supplier'}</Button>
        </div>
      </form>
    </Modal>
  );
}
