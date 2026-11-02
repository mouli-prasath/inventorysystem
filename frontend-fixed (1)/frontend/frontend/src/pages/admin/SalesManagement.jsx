import { useEffect, useState } from 'react';
import { XCircle, RotateCcw, Search } from 'lucide-react';
import { transactionApi } from '../../api/sales';
import { getErrorMessage } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { PageHeader, Table, TableHead, TableBody, Th, Td, StatusBadge } from '../../components/ui';
import ReturnModal from '../../components/admin/ReturnModal';
import TransactionDetailModal from '../../components/admin/TransactionDetailModal';
import { POS_ROLES } from '../../constants/roles';

export default function SalesManagement() {
  const { hasRole } = useAuth();
  const canVoid = hasRole('STORE_MANAGER', 'ADMIN');
  const canReturn = hasRole(...POS_ROLES);
  const [start, setStart] = useState(() => {
    const d = new Date(); d.setDate(d.getDate() - 30);
    return d.toISOString().slice(0, 10);
  });
  const [end, setEnd] = useState(() => new Date().toISOString().slice(0, 10));
  const [transactions, setTransactions] = useState([]);
  const [returningTransaction, setReturningTransaction] = useState(null);
  const [selectedId, setSelectedId] = useState(null);
  const [lookupNumber, setLookupNumber] = useState('');
  const [lookupError, setLookupError] = useState('');
  const [error, setError] = useState('');

  const load = () => {
    transactionApi.getDailyReport(`${start}T00:00:00`, `${end}T23:59:59`, { size: 50 })
      .then((res) => setTransactions(res.data.content || []))
      .catch((err) => setError(getErrorMessage(err)));
  };

  useEffect(load, [start, end]);

  const handleVoid = async (id) => {
    if (!confirm('Void this transaction? This restocks all items.')) return;
    try {
      await transactionApi.void(id);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleLookup = async (e) => {
    e.preventDefault();
    setLookupError('');
    try {
      const { data } = await transactionApi.getByNumber(lookupNumber.trim());
      setSelectedId(data.id);
      setLookupNumber('');
    } catch (err) {
      setLookupError(getErrorMessage(err));
    }
  };

  const showActions = canVoid || canReturn;

  return (
    <div>
      <PageHeader title="Sales" subtitle="Transaction history across all locations." />

      <div className="mb-4 flex flex-wrap items-end gap-3">
        <input type="date" value={start} onChange={(e) => setStart(e.target.value)} className="rounded-lg border border-border px-3 py-2 text-sm" />
        <input type="date" value={end} onChange={(e) => setEnd(e.target.value)} className="rounded-lg border border-border px-3 py-2 text-sm" />

        <form onSubmit={handleLookup} className="flex items-center gap-2">
          <div className="relative">
            <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
            <input
              value={lookupNumber}
              onChange={(e) => setLookupNumber(e.target.value)}
              placeholder="Look up by transaction #…"
              className="w-56 rounded-lg border border-border py-2 pl-8 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>
        </form>
        {lookupError && <span className="text-xs text-status-danger">{lookupError}</span>}
      </div>

      {error && <p className="mb-4 text-sm text-status-danger">{error}</p>}

      <Table>
        <TableHead>
          <Th>Transaction #</Th>
          <Th>Date</Th>
          <Th>Customer</Th>
          <Th>Employee</Th>
          <Th>Total</Th>
          <Th>Status</Th>
          {showActions && <Th>Actions</Th>}
        </TableHead>
        <TableBody>
          {transactions.map((t) => (
            <tr key={t.id} className="cursor-pointer hover:bg-surface">
              <Td className="font-mono text-xs" onClick={() => setSelectedId(t.id)}>{t.transactionNumber}</Td>
              <Td onClick={() => setSelectedId(t.id)}>{new Date(t.transactionDate).toLocaleString()}</Td>
              <Td onClick={() => setSelectedId(t.id)}>{t.customerName || 'Walk-in'}</Td>
              <Td onClick={() => setSelectedId(t.id)}>{t.employeeName}</Td>
              <Td className="font-medium" onClick={() => setSelectedId(t.id)}>${Number(t.totalAmount).toFixed(2)}</Td>
              <Td onClick={() => setSelectedId(t.id)}><StatusBadge status={t.status} /></Td>
              {showActions && (
                <Td>
                  <div className="flex gap-3">
                    {canReturn && (t.status === 'COMPLETED' || t.status === 'PARTIALLY_REFUNDED') && (
                      <button onClick={() => setReturningTransaction(t)} className="flex items-center gap-1 text-xs text-brand-600 hover:underline">
                        <RotateCcw size={13} /> Return
                      </button>
                    )}
                    {canVoid && t.status === 'COMPLETED' && (
                      <button onClick={() => handleVoid(t.id)} className="flex items-center gap-1 text-xs text-status-danger hover:underline">
                        <XCircle size={13} /> Void
                      </button>
                    )}
                  </div>
                </Td>
              )}
            </tr>
          ))}
        </TableBody>
      </Table>

      {transactions.length === 0 && (
        <p className="mt-6 text-center text-sm text-text-secondary">No transactions in this range.</p>
      )}

      {returningTransaction && (
        <ReturnModal
          transaction={returningTransaction}
          onClose={() => setReturningTransaction(null)}
          onSaved={() => { setReturningTransaction(null); load(); }}
        />
      )}
      {selectedId && (
        <TransactionDetailModal id={selectedId} onClose={() => setSelectedId(null)} />
      )}
    </div>
  );
}
