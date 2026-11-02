import { useEffect, useState } from 'react';
import { Plus, Pencil, Trash2, Search } from 'lucide-react';
import { supplierApi } from '../../api/procurement';
import { getErrorMessage } from '../../api/client';
import { PageHeader, Button, Table, TableHead, TableBody, Th, Td, StatusBadge } from '../../components/ui';
import SupplierFormModal from '../../components/admin/SupplierFormModal';

export default function Suppliers() {
  const [suppliers, setSuppliers] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState(null);
  const [error, setError] = useState('');

  const load = () => {
    const call = keyword ? supplierApi.search(keyword) : supplierApi.list(true);
    call.then((res) => setSuppliers(res.data))
      .catch((err) => setError(getErrorMessage(err)));
  };

  useEffect(() => {
    const timeout = setTimeout(load, 300);
    return () => clearTimeout(timeout);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword]);

  const handleDeactivate = async (supplier) => {
    if (!confirm(`Deactivate ${supplier.supplierName}?`)) return;
    try {
      await supplierApi.deactivate(supplier.id);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader
        title="Suppliers"
        subtitle="Vendor profiles and contact information."
        action={<Button icon={Plus} onClick={() => setShowCreateModal(true)}>Add Supplier</Button>}
      />

      {error && <p className="mb-4 text-sm text-status-danger">{error}</p>}

      <div className="relative mb-4 max-w-sm">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
        <input
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="Search suppliers…"
          className="w-full rounded-lg border border-border bg-card py-2 pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        />
      </div>

      <Table>
        <TableHead>
          <Th>Name</Th>
          <Th>Contact</Th>
          <Th>Email</Th>
          <Th>Phone</Th>
          <Th>Payment Terms</Th>
          <Th>Status</Th>
          <Th>Actions</Th>
        </TableHead>
        <TableBody>
          {suppliers.map((s) => (
            <tr key={s.id}>
              <Td className="font-medium">{s.supplierName}</Td>
              <Td>{s.contactPerson || '—'}</Td>
              <Td>{s.email || '—'}</Td>
              <Td>{s.phone || '—'}</Td>
              <Td>{s.paymentTerms || '—'}</Td>
              <Td><StatusBadge status={s.active ? 'ACTIVE' : 'INACTIVE'} /></Td>
              <Td>
                <div className="flex gap-2">
                  <button onClick={() => setEditingSupplier(s)} className="text-text-secondary hover:text-brand-600">
                    <Pencil size={15} />
                  </button>
                  <button onClick={() => handleDeactivate(s)} className="text-text-secondary hover:text-status-danger">
                    <Trash2 size={15} />
                  </button>
                </div>
              </Td>
            </tr>
          ))}
        </TableBody>
      </Table>

      {suppliers.length === 0 && (
        <p className="mt-6 text-center text-sm text-text-secondary">No suppliers yet.</p>
      )}

      {(showCreateModal || editingSupplier) && (
        <SupplierFormModal
          supplier={editingSupplier}
          onClose={() => { setShowCreateModal(false); setEditingSupplier(null); }}
          onSaved={() => { setShowCreateModal(false); setEditingSupplier(null); load(); }}
        />
      )}
    </div>
  );
}
