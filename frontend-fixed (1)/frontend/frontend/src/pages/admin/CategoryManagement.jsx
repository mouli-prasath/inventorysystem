import { useEffect, useState } from 'react';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import { categoryApi } from '../../api/catalog';
import { getErrorMessage } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { MANAGER_ROLES } from '../../constants/roles';
import { PageHeader, Button, Table, TableHead, TableBody, Th, Td, StatusBadge } from '../../components/ui';
import CategoryFormModal from '../../components/admin/CategoryFormModal';

export default function CategoryManagement() {
  const { hasRole } = useAuth();
  const canDelete = hasRole(...MANAGER_ROLES);
  const [categories, setCategories] = useState([]);
  const [editingCategory, setEditingCategory] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [error, setError] = useState('');

  const load = () => {
    categoryApi.list()
      .then((res) => setCategories(res.data))
      .catch((err) => setError(getErrorMessage(err)));
  };

  useEffect(load, []);

  const handleDeactivate = async (category) => {
    if (!confirm(`Deactivate "${category.categoryName}"? Products in it will keep their category link.`)) return;
    try {
      await categoryApi.deactivate(category.id);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader
        title="Category Management"
        subtitle="Organize your product catalog into categories."
        action={<Button icon={Plus} onClick={() => setShowCreateModal(true)}>Add Category</Button>}
      />

      {error && <p className="mb-4 text-sm text-status-danger">{error}</p>}

      <Table>
        <TableHead>
          <Th>Name</Th>
          <Th>Parent</Th>
          <Th>Description</Th>
          <Th>Status</Th>
          <Th>Actions</Th>
        </TableHead>
        <TableBody>
          {categories.map((c) => (
            <tr key={c.id}>
              <Td className="font-medium">{c.categoryName}</Td>
              <Td>{c.parentCategoryName || '—'}</Td>
              <Td className="max-w-xs truncate">{c.description || '—'}</Td>
              <Td><StatusBadge status={c.active ? 'ACTIVE' : 'INACTIVE'} /></Td>
              <Td>
                <div className="flex gap-2">
                  <button onClick={() => setEditingCategory(c)} className="text-text-secondary hover:text-brand-600">
                    <Pencil size={15} />
                  </button>
                  {canDelete && (
                    <button onClick={() => handleDeactivate(c)} className="text-text-secondary hover:text-status-danger">
                      <Trash2 size={15} />
                    </button>
                  )}
                </div>
              </Td>
            </tr>
          ))}
        </TableBody>
      </Table>

      {categories.length === 0 && (
        <p className="mt-6 text-center text-sm text-text-secondary">No categories yet.</p>
      )}

      {(showCreateModal || editingCategory) && (
        <CategoryFormModal
          category={editingCategory}
          categories={categories}
          onClose={() => { setShowCreateModal(false); setEditingCategory(null); }}
          onSaved={() => { setShowCreateModal(false); setEditingCategory(null); load(); }}
        />
      )}
    </div>
  );
}