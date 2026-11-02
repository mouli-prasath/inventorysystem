import { useEffect, useState } from 'react';
import { Search, Plus } from 'lucide-react';
import { customerApi } from '../../api/sales';
import { getErrorMessage } from '../../api/client';
import { PageHeader, Button, Table, TableHead, TableBody, Th, Td, StatusBadge } from '../../components/ui';
import CustomerFormModal from '../../components/admin/CustomerFormModal';
import CustomerDetailModal from '../../components/admin/CustomerDetailModal';

export default function CustomerManagement() {
  const [customers, setCustomers] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedId, setSelectedId] = useState(null);
  const [error, setError] = useState('');

  const load = () => {
    const params = { size: 50 };
    if (keyword) params.keyword = keyword;
    customerApi.search(params)
      .then((res) => setCustomers(res.data.content || []))
      .catch((err) => setError(getErrorMessage(err)));
  };

  useEffect(() => {
    const timeout = setTimeout(load, 300);
    return () => clearTimeout(timeout);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword]);

  return (
    <div>
      <PageHeader
        title="Customers"
        subtitle="All customer accounts and their activity."
        action={<Button icon={Plus} onClick={() => setShowCreateModal(true)}>Add Customer</Button>}
      />

      <div className="relative mb-4 max-w-sm">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
        <input
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="Search customers…"
          className="w-full rounded-lg border border-border bg-card py-2 pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        />
      </div>

      {error && <p className="mb-4 text-sm text-status-danger">{error}</p>}

      <Table>
        <TableHead>
          <Th>Name</Th>
          <Th>Email</Th>
          <Th>Phone</Th>
          <Th>Loyalty Points</Th>
          <Th>Total Spent</Th>
          <Th>Status</Th>
        </TableHead>
        <TableBody>
          {customers.map((c) => (
            <tr key={c.id} onClick={() => setSelectedId(c.id)} className="cursor-pointer hover:bg-surface">
              <Td className="font-medium">{c.firstName} {c.lastName}</Td>
              <Td>{c.email || '—'}</Td>
              <Td>{c.phone}</Td>
              <Td>{c.loyaltyPoints}</Td>
              <Td>${Number(c.totalPurchases).toFixed(2)}</Td>
              <Td><StatusBadge status={c.active ? 'ACTIVE' : 'INACTIVE'} /></Td>
            </tr>
          ))}
        </TableBody>
      </Table>

      {customers.length === 0 && (
        <p className="mt-6 text-center text-sm text-text-secondary">No customers found.</p>
      )}

      {showCreateModal && (
        <CustomerFormModal onClose={() => setShowCreateModal(false)} onSaved={() => { setShowCreateModal(false); load(); }} />
      )}
      {selectedId && (
        <CustomerDetailModal customerId={selectedId} onClose={() => setSelectedId(null)} onChanged={load} />
      )}
    </div>
  );
}
