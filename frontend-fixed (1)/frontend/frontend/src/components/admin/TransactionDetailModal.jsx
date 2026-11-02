import { useEffect, useState } from 'react';
import { transactionApi } from '../../api/sales';
import { getErrorMessage } from '../../api/client';
import { Modal, StatusBadge } from '../ui';

export default function TransactionDetailModal({ id, onClose }) {
  const [transaction, setTransaction] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    transactionApi.getById(id).then((res) => setTransaction(res.data)).catch((err) => setError(getErrorMessage(err)));
  }, [id]);

  if (error) {
    return (
      <Modal title="Transaction" onClose={onClose}>
        <p className="text-sm text-status-danger">{error}</p>
      </Modal>
    );
  }
  if (!transaction) return null;

  return (
    <Modal title={transaction.transactionNumber} onClose={onClose}>
      <div className="mb-4 flex items-center justify-between">
        <div>
          <p className="text-sm text-text-secondary">{transaction.customerName || 'Walk-in'} · {transaction.storeLocation}</p>
          <p className="text-xs text-text-muted">
            {new Date(transaction.transactionDate).toLocaleString()} · Processed by {transaction.employeeName}
          </p>
        </div>
        <StatusBadge status={transaction.status} />
      </div>

      <table className="w-full text-left text-sm">
        <thead className="border-b border-border text-xs uppercase text-text-secondary">
          <tr><th className="py-2">Item</th><th className="py-2">Qty</th><th className="py-2">Unit Price</th><th className="py-2">Discount</th><th className="py-2">Total</th></tr>
        </thead>
        <tbody className="divide-y divide-border">
          {transaction.items.map((item) => (
            <tr key={item.id}>
              <td className="py-2">{item.productName} <span className="text-text-muted">({item.sku})</span></td>
              <td className="py-2">{item.quantity}</td>
              <td className="py-2">${Number(item.unitPrice).toFixed(2)}</td>
              <td className="py-2">${Number(item.discountAmount).toFixed(2)}</td>
              <td className="py-2 font-medium">${Number(item.lineTotal).toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="mt-3 space-y-1 border-t border-border pt-3 text-sm">
        <div className="flex justify-between text-text-secondary"><span>Subtotal</span><span>${Number(transaction.subtotal).toFixed(2)}</span></div>
        <div className="flex justify-between text-text-secondary"><span>Tax</span><span>${Number(transaction.taxAmount).toFixed(2)}</span></div>
        <div className="flex justify-between text-text-secondary"><span>Discount</span><span>-${Number(transaction.discountAmount).toFixed(2)}</span></div>
        <div className="flex justify-between border-t border-border pt-1 text-base font-bold text-text-primary">
          <span>Total</span><span>${Number(transaction.totalAmount).toFixed(2)}</span>
        </div>
      </div>
    </Modal>
  );
}
