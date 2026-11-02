import { useState } from 'react';
import { customerApi } from '../../api/sales';
import { getErrorMessage } from '../../api/client';
import { Modal, Button, Input } from '../ui';

export default function CustomerFormModal({ onClose, onSaved }) {
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', phone: '', address: '', city: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const set = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await customerApi.create(form);
      onSaved();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal title="Add Customer" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <Input label="First name" value={form.firstName} onChange={set('firstName')} required />
          <Input label="Last name" value={form.lastName} onChange={set('lastName')} required />
        </div>
        <Input label="Phone" value={form.phone} onChange={set('phone')} required placeholder="5551234567" />
        <Input label="Email" type="email" value={form.email} onChange={set('email')} />
        <Input label="Address" value={form.address} onChange={set('address')} />
        <Input label="City" value={form.city} onChange={set('city')} />

        {error && <p className="text-sm text-status-danger">{error}</p>}

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save Customer'}</Button>
        </div>
      </form>
    </Modal>
  );
}
