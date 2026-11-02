import { useState } from 'react';
import { transactionApi } from '../../api/sales';
import { getErrorMessage } from '../../api/client';
import { Modal, Button, Input } from '../ui';

export default function ReturnModal({ transaction, onClose, onSaved }) {
  const [productId, setProductId] = useState(transaction.items[0]?.productId || '');
  const [quantity, setQuantity] = useState(1);
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);

  const selectedItem = transaction.items.find((i) => i.productId === Number(productId));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const { data } = await transactionApi.processReturn(transaction.id, {
        productId: Number(productId),
        quantity: Number(quantity),
        reason,
      });
      setResult(data);
      onSaved();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  if (result) {
    return (
      <Modal title="Return processed" onClose={onClose}>
        <p className="text-sm text-text-primary">
          Refunded <strong>${Number(result.refundAmount).toFixed(2)}</strong> for {result.quantity}× {result.productName}.
        </p>
        <Button className="mt-4 w-full" onClick={onClose}>Done</Button>
      </Modal>
    );
  }

  return (
    <Modal title={`Return — ${transaction.transactionNumber}`} onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-1.5 block text-sm font-medium text-text-primary">Product</label>
          <select
            value={productId}
            onChange={(e) => setProductId(e.target.value)}
            required
            className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            {transaction.items.map((item) => (
              <option key={item.productId} value={item.productId}>
                {item.productName} (purchased {item.quantity})
              </option>
            ))}
          </select>
        </div>

        <Input
          label="Quantity to return"
          type="number" min="1" max={selectedItem?.quantity || 1}
          value={quantity} onChange={(e) => setQuantity(e.target.value)} required
        />
        <Input label="Reason" value={reason} onChange={(e) => setReason(e.target.value)} placeholder="e.g. Customer changed mind" />

        {error && <p className="text-sm text-status-danger">{error}</p>}

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" disabled={saving}>{saving ? 'Processing…' : 'Process Return'}</Button>
        </div>
      </form>
    </Modal>
  );
}
