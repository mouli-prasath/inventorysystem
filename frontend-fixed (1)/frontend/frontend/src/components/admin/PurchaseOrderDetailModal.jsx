import { useEffect, useState } from 'react';
import { purchaseOrderApi } from '../../api/procurement';
import { getErrorMessage } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { Modal, Button, StatusBadge } from '../ui';

export default function PurchaseOrderDetailModal({ id, onClose, onChanged }) {
  const { hasRole } = useAuth();
  // Only approval is Store Manager/Admin-only. Mark-as-ordered, receive, and
  // cancel are all allowed for Inventory Manager too (see SecurityConfig).
  const canApprove = hasRole('STORE_MANAGER', 'ADMIN');
  const canManagePOs = hasRole('INVENTORY_MANAGER', 'STORE_MANAGER', 'ADMIN');
  const [po, setPo] = useState(null);
  const [receiveQuantities, setReceiveQuantities] = useState({});
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const load = () => {
    purchaseOrderApi.getById(id).then((res) => {
      setPo(res.data);
      const initial = {};
      res.data.items.forEach((item) => {
        initial[item.id] = item.quantityOrdered - item.quantityReceived;
      });
      setReceiveQuantities(initial);
    }).catch((err) => setError(getErrorMessage(err)));
  };

  useEffect(load, [id]);

  const runAction = async (action) => {
    setBusy(true);
    setError('');
    try {
      await action();
      load();
      onChanged();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const handleReceive = () => runAction(() =>
    purchaseOrderApi.receive(id, Object.entries(receiveQuantities)
      .filter(([, qty]) => Number(qty) > 0)
      .map(([purchaseOrderItemId, quantityReceived]) => ({
        purchaseOrderItemId: Number(purchaseOrderItemId),
        quantityReceived: Number(quantityReceived),
      }))
    )
  );

  if (!po) return null;

  const canReceive = po.status === 'APPROVED' || po.status === 'ORDERED' || po.status === 'PARTIALLY_RECEIVED';

  return (
    <Modal title={po.poNumber} onClose={onClose} maxWidth="max-w-2xl">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <p className="text-sm text-text-secondary">{po.supplierName} · {po.storeLocation}</p>
          <p className="text-xs text-text-muted">Ordered {new Date(po.orderDate).toLocaleDateString()}</p>
        </div>
        <StatusBadge status={po.status} />
      </div>

      <table className="w-full text-left text-sm">
        <thead className="border-b border-border text-xs uppercase text-text-secondary">
          <tr>
            <th className="py-2">Product</th>
            <th className="py-2">Ordered</th>
            <th className="py-2">Received</th>
            <th className="py-2">Unit Cost</th>
            {canReceive && canManagePOs && <th className="py-2">Receive now</th>}
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {po.items.map((item) => (
            <tr key={item.id}>
              <td className="py-2">{item.productName} <span className="text-text-muted">({item.sku})</span></td>
              <td className="py-2">{item.quantityOrdered}</td>
              <td className="py-2">{item.quantityReceived}</td>
              <td className="py-2">${Number(item.unitCost).toFixed(2)}</td>
              {canReceive && canManagePOs && (
                <td className="py-2">
                  <input
                    type="number" min="0" max={item.quantityOrdered - item.quantityReceived}
                    value={receiveQuantities[item.id] ?? 0}
                    onChange={(e) => setReceiveQuantities((prev) => ({ ...prev, [item.id]: e.target.value }))}
                    className="w-20 rounded border border-border px-2 py-1 text-sm"
                  />
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>

      <p className="mt-3 text-right text-sm font-bold text-text-primary">Total: ${Number(po.totalAmount).toFixed(2)}</p>

      {error && <p className="mt-3 text-sm text-status-danger">{error}</p>}

      {canManagePOs && (
        <div className="mt-4 flex justify-end gap-2 border-t border-border pt-4">
          {po.status === 'CANCELLED' || po.status === 'RECEIVED' ? null : (
            <Button variant="secondary" disabled={busy} onClick={() => runAction(() => purchaseOrderApi.cancel(id))}>
              Cancel Order
            </Button>
          )}
          {po.status === 'PENDING' && canApprove && (
            <Button disabled={busy} onClick={() => runAction(() => purchaseOrderApi.approve(id))}>Approve</Button>
          )}
          {po.status === 'APPROVED' && (
            <Button disabled={busy} onClick={() => runAction(() => purchaseOrderApi.markOrdered(id))}>Mark as Ordered</Button>
          )}
          {canReceive && (
            <Button disabled={busy} onClick={handleReceive}>Receive Delivery</Button>
          )}
        </div>
      )}
    </Modal>
  );
}