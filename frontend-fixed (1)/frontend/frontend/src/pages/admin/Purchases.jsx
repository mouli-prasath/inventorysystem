import { useEffect, useState } from 'react';
import { Plus, Sparkles } from 'lucide-react';
import { purchaseOrderApi } from '../../api/procurement';
import { getErrorMessage } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { PageHeader, Button, Table, TableHead, TableBody, Th, Td, StatusBadge } from '../../components/ui';
import PurchaseOrderFormModal from '../../components/admin/PurchaseOrderFormModal';
import PurchaseOrderDetailModal from '../../components/admin/PurchaseOrderDetailModal';

export default function Purchases() {
  const { hasRole } = useAuth();
  // Approval itself is Store Manager/Admin-only, but creating, auto-generating,
  // and otherwise managing POs is allowed for Inventory Manager too (see
  // SecurityConfig: POST /purchase-orders and /auto-generate allow INVENTORY_MANAGER).
  const canManagePOs = hasRole('INVENTORY_MANAGER', 'STORE_MANAGER', 'ADMIN');
  const [orders, setOrders] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedId, setSelectedId] = useState(null);
  const [error, setError] = useState('');
  const [autoGenerating, setAutoGenerating] = useState(false);

  const load = () => {
    purchaseOrderApi.list({ size: 50 })
      .then((res) => setOrders(res.data.content || []))
      .catch((err) => setError(getErrorMessage(err)));
  };

  useEffect(load, []);

  const handleAutoGenerate = async () => {
    setAutoGenerating(true);
    setError('');
    try {
      await purchaseOrderApi.autoGenerate();
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setAutoGenerating(false);
    }
  };

  return (
    <div>
      <PageHeader
        title="Purchases"
        subtitle="Purchase orders and supplier restocking."
        action={
          <div className="flex gap-2">
            {canManagePOs && (
              <Button variant="secondary" icon={Sparkles} onClick={handleAutoGenerate} disabled={autoGenerating}>
                {autoGenerating ? 'Generating…' : 'Auto-generate from low stock'}
              </Button>
            )}
            <Button icon={Plus} onClick={() => setShowCreateModal(true)}>New Purchase Order</Button>
          </div>
        }
      />

      {error && <p className="mb-4 text-sm text-status-danger">{error}</p>}

      <Table>
        <TableHead>
          <Th>PO Number</Th>
          <Th>Supplier</Th>
          <Th>Order Date</Th>
          <Th>Total</Th>
          <Th>Status</Th>
        </TableHead>
        <TableBody>
          {orders.map((po) => (
            <tr key={po.id} onClick={() => setSelectedId(po.id)} className="cursor-pointer hover:bg-surface">
              <Td className="font-mono text-xs">{po.poNumber}</Td>
              <Td>{po.supplierName}</Td>
              <Td>{new Date(po.orderDate).toLocaleDateString()}</Td>
              <Td className="font-medium">${Number(po.totalAmount).toFixed(2)}</Td>
              <Td><StatusBadge status={po.status} /></Td>
            </tr>
          ))}
        </TableBody>
      </Table>

      {orders.length === 0 && (
        <p className="mt-6 text-center text-sm text-text-secondary">No purchase orders yet.</p>
      )}

      {showCreateModal && (
        <PurchaseOrderFormModal onClose={() => setShowCreateModal(false)} onSaved={() => { setShowCreateModal(false); load(); }} />
      )}
      {selectedId && (
        <PurchaseOrderDetailModal id={selectedId} onClose={() => setSelectedId(null)} onChanged={load} />
      )}
    </div>
  );
}