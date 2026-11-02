import { useEffect, useState } from 'react';
import { inventoryApi } from '../../api/inventory';
import { getErrorMessage } from '../../api/client';
import { Modal, Button, Input, StatusBadge } from '../ui';

export default function InventoryDetailModal({ productId, productName, onClose, onChanged }) {
  const [locations, setLocations] = useState([]);
  const [reorderPoint, setReorderPoint] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [savedMessage, setSavedMessage] = useState('');

  const load = () => {
    inventoryApi.getByProduct(productId)
      .then((res) => setLocations(res.data))
      .catch((err) => setError(getErrorMessage(err)));
  };

  useEffect(load, [productId]);

  const handleReorderSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSavedMessage('');
    try {
      await inventoryApi.updateReorderPoint(productId, Number(reorderPoint));
      setSavedMessage('Reorder point updated.');
      onChanged();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal title={productName} onClose={onClose}>
      <h3 className="text-sm font-semibold text-text-primary">Stock by location</h3>
      <div className="mt-2 space-y-2">
        {locations.map((loc) => (
          <div key={loc.id} className="flex items-center justify-between rounded-lg border border-border p-3 text-sm">
            <span className="font-medium text-text-primary">{loc.storeLocation}</span>
            <span className="text-text-secondary">
              {loc.quantityOnHand} on hand
              {loc.quantityReserved > 0 && ` (${loc.quantityReserved} reserved)`}
            </span>
          </div>
        ))}
        {locations.length === 0 && (
          <p className="text-sm text-text-secondary">No stock recorded at any location yet.</p>
        )}
      </div>

      <form onSubmit={handleReorderSave} className="mt-6 flex items-end gap-2 border-t border-border pt-4">
        <Input
          label="Reorder point"
          type="number" min="0"
          value={reorderPoint}
          onChange={(e) => setReorderPoint(e.target.value)}
          placeholder="e.g. 10"
          className="flex-1"
        />
        <Button type="submit" disabled={saving || !reorderPoint} size="sm">{saving ? 'Saving…' : 'Update'}</Button>
      </form>
      {error && <p className="mt-2 text-sm text-status-danger">{error}</p>}
      {savedMessage && <p className="mt-2 text-sm text-status-success">{savedMessage}</p>}
    </Modal>
  );
}
