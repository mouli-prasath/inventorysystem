import { useEffect, useState } from 'react';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import { promotionApi } from '../../api/catalog';
import { getErrorMessage } from '../../api/client';
import { PageHeader, Button, Table, TableHead, TableBody, Th, Td, StatusBadge } from '../../components/ui';
import PromotionFormModal from '../../components/admin/PromotionFormModal';

function scopeLabel(p) {
  if (p.productScopeName) return `Product: ${p.productScopeName}`;
  if (p.categoryScopeName) return `Category: ${p.categoryScopeName}`;
  return 'Store-wide';
}

function discountLabel(p) {
  return p.discountType === 'PERCENTAGE' ? `${p.discountValue}% off` : `$${Number(p.discountValue).toFixed(2)} off`;
}

export default function Promotions() {
  const [promotions, setPromotions] = useState([]);
  const [editingPromotion, setEditingPromotion] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [error, setError] = useState('');

  const load = () => {
    promotionApi.list()
      .then((res) => setPromotions(res.data))
      .catch((err) => setError(getErrorMessage(err)));
  };

  useEffect(load, []);

  const handleDeactivate = async (promo) => {
    if (!confirm(`Deactivate "${promo.promotionName}"?`)) return;
    try {
      await promotionApi.deactivate(promo.id);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader
        title="Promotions"
        subtitle="Store-wide, category, or product discounts."
        action={<Button icon={Plus} onClick={() => setShowCreateModal(true)}>Add Promotion</Button>}
      />

      {error && <p className="mb-4 text-sm text-status-danger">{error}</p>}

      <Table>
        <TableHead>
          <Th>Name</Th>
          <Th>Scope</Th>
          <Th>Discount</Th>
          <Th>Valid dates</Th>
          <Th>Status</Th>
          <Th>Actions</Th>
        </TableHead>
        <TableBody>
          {promotions.map((p) => (
            <tr key={p.id}>
              <Td className="font-medium">{p.promotionName}</Td>
              <Td>{scopeLabel(p)}</Td>
              <Td>{discountLabel(p)}</Td>
              <Td className="text-xs">
                {new Date(p.startDate).toLocaleDateString()} – {new Date(p.endDate).toLocaleDateString()}
              </Td>
              <Td>
                <StatusBadge status={p.currentlyRunning ? 'ACTIVE' : 'INACTIVE'} label={p.currentlyRunning ? 'Running' : 'Not running'} />
              </Td>
              <Td>
                <div className="flex gap-2">
                  <button onClick={() => setEditingPromotion(p)} className="text-text-secondary hover:text-brand-600">
                    <Pencil size={15} />
                  </button>
                  <button onClick={() => handleDeactivate(p)} className="text-text-secondary hover:text-status-danger">
                    <Trash2 size={15} />
                  </button>
                </div>
              </Td>
            </tr>
          ))}
        </TableBody>
      </Table>

      {promotions.length === 0 && (
        <p className="mt-6 text-center text-sm text-text-secondary">No promotions yet.</p>
      )}

      {(showCreateModal || editingPromotion) && (
        <PromotionFormModal
          promotion={editingPromotion}
          onClose={() => { setShowCreateModal(false); setEditingPromotion(null); }}
          onSaved={() => { setShowCreateModal(false); setEditingPromotion(null); load(); }}
        />
      )}
    </div>
  );
}
