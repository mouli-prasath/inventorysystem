import { useEffect, useState } from 'react';
import { customerApi } from '../../api/sales';
import { getErrorMessage } from '../../api/client';
import { Modal, Button, Input } from '../ui';

export default function CustomerDetailModal({ customerId, onClose, onChanged }) {
  const [customer, setCustomer] = useState(null);
  const [history, setHistory] = useState([]);
  const [form, setForm] = useState(null);
  const [loyaltyDelta, setLoyaltyDelta] = useState('');
  const [loyaltyReason, setLoyaltyReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const load = () => {
    customerApi.getById(customerId).then((res) => {
      setCustomer(res.data);
      setForm({
        firstName: res.data.firstName, lastName: res.data.lastName,
        email: res.data.email || '', phone: res.data.phone,
        address: res.data.address || '', city: res.data.city || '',
      });
    }).catch((err) => setError(getErrorMessage(err)));

    customerApi.getHistory(customerId, { size: 10, sort: 'transactionDate,desc' })
      .then((res) => setHistory(res.data.content || []))
      .catch(() => {});
  };

  useEffect(load, [customerId]);

  const set = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await customerApi.update(customerId, form);
      onChanged();
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const handleLoyaltyAdjust = async () => {
    if (!loyaltyDelta) return;
    setError('');
    try {
      await customerApi.adjustLoyalty(customerId, Number(loyaltyDelta), loyaltyReason);
      setLoyaltyDelta('');
      setLoyaltyReason('');
      load();
      onChanged();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleDeactivate = async () => {
    if (!confirm(`Deactivate ${customer.firstName} ${customer.lastName}?`)) return;
    setError('');
    try {
      await customerApi.deactivate(customerId);
      onChanged();
      onClose();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  if (!customer || !form) return null;

  return (
    <Modal title={`${customer.firstName} ${customer.lastName}`} onClose={onClose} maxWidth="max-w-2xl">
      <form onSubmit={handleSave} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <Input label="First name" value={form.firstName} onChange={set('firstName')} required />
          <Input label="Last name" value={form.lastName} onChange={set('lastName')} required />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <Input label="Phone" value={form.phone} onChange={set('phone')} required />
          <Input label="Email" type="email" value={form.email} onChange={set('email')} />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <Input label="Address" value={form.address} onChange={set('address')} />
          <Input label="City" value={form.city} onChange={set('city')} />
        </div>

        {error && <p className="text-sm text-status-danger">{error}</p>}

        <div className="flex justify-between">
          <Button type="button" variant="danger" size="sm" onClick={handleDeactivate} disabled={customer.active === false}>
            {customer.active === false ? 'Already inactive' : 'Deactivate customer'}
          </Button>
          <Button type="submit" disabled={saving} size="sm">{saving ? 'Saving…' : 'Save changes'}</Button>
        </div>
      </form>

      <div className="mt-6 rounded-lg border border-border p-4">
        <h3 className="text-sm font-semibold text-text-primary">Loyalty points — {customer.loyaltyPoints} current</h3>
        <div className="mt-2 flex gap-2">
          <input
            type="number" placeholder="+/- points"
            value={loyaltyDelta}
            onChange={(e) => setLoyaltyDelta(e.target.value)}
            className="w-28 rounded-lg border border-border px-2 py-1.5 text-sm"
          />
          <input
            placeholder="Reason (optional)"
            value={loyaltyReason}
            onChange={(e) => setLoyaltyReason(e.target.value)}
            className="flex-1 rounded-lg border border-border px-2 py-1.5 text-sm"
          />
          <Button type="button" size="sm" variant="secondary" onClick={handleLoyaltyAdjust}>Apply</Button>
        </div>
      </div>

      <div className="mt-6">
        <h3 className="text-sm font-semibold text-text-primary">Purchase history</h3>
        <div className="mt-2 max-h-48 overflow-y-auto">
          {history.length === 0 && <p className="text-sm text-text-secondary">No transactions yet.</p>}
          {history.map((t) => (
            <div key={t.id} className="flex justify-between border-b border-border py-2 text-sm last:border-0">
              <span className="text-text-secondary">{new Date(t.transactionDate).toLocaleDateString()} · {t.transactionNumber}</span>
              <span className="font-medium text-text-primary">${Number(t.totalAmount).toFixed(2)}</span>
            </div>
          ))}
        </div>
      </div>
    </Modal>
  );
}
