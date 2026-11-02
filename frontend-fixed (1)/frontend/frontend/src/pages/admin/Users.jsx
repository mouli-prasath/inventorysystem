import { useEffect, useState } from 'react';
import { Unlock, UserX, UserCheck } from 'lucide-react';
import { adminUserApi } from '../../api/auth';
import { getErrorMessage } from '../../api/client';
import { ROLES } from '../../constants/roles';
import { PageHeader, Table, TableHead, TableBody, Th, Td, StatusBadge } from '../../components/ui';

const ALL_ROLES = Object.values(ROLES);

export default function Users() {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');

  const load = () => {
    adminUserApi.list({ size: 50 })
      .then((res) => setUsers(res.data.content || []))
      .catch((err) => setError(getErrorMessage(err)));
  };

  useEffect(load, []);

  const runAction = async (action) => {
    try {
      await action();
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleRoleChange = (userId, role) => {
    if (!role) return;
    runAction(() => adminUserApi.changeRole(userId, role));
  };

  return (
    <div>
      <PageHeader title="Users" subtitle="Staff and customer accounts." />

      {error && <p className="mb-4 text-sm text-status-danger">{error}</p>}

      <Table>
        <TableHead>
          <Th>Name</Th>
          <Th>Username</Th>
          <Th>Email</Th>
          <Th>Role</Th>
          <Th>Status</Th>
          <Th>Actions</Th>
        </TableHead>
        <TableBody>
          {users.map((u) => (
            <tr key={u.id}>
              <Td className="font-medium">{u.firstName} {u.lastName}</Td>
              <Td>{u.username}</Td>
              <Td>{u.email}</Td>
              <Td>
                <select
                  defaultValue=""
                  onChange={(e) => { handleRoleChange(u.id, e.target.value); e.target.value = ''; }}
                  className="rounded border border-border bg-card px-2 py-1 text-xs"
                >
                  <option value="" disabled>{u.role}</option>
                  {ALL_ROLES.filter((r) => r !== u.role).map((r) => (
                    <option key={r} value={r}>Change to {r}</option>
                  ))}
                </select>
              </Td>
              <Td><StatusBadge status={u.active ? 'ACTIVE' : 'INACTIVE'} /></Td>
              <Td>
                <div className="flex gap-2">
                  {u.active ? (
                    <button onClick={() => runAction(() => adminUserApi.deactivate(u.id))} title="Deactivate" className="text-text-secondary hover:text-status-danger">
                      <UserX size={15} />
                    </button>
                  ) : (
                    <button onClick={() => runAction(() => adminUserApi.reactivate(u.id))} title="Reactivate" className="text-text-secondary hover:text-status-success">
                      <UserCheck size={15} />
                    </button>
                  )}
                  <button onClick={() => runAction(() => adminUserApi.unlock(u.id))} title="Unlock account" className="text-text-secondary hover:text-brand-600">
                    <Unlock size={15} />
                  </button>
                </div>
              </Td>
            </tr>
          ))}
        </TableBody>
      </Table>

      {users.length === 0 && (
        <p className="mt-6 text-center text-sm text-text-secondary">No users found.</p>
      )}
    </div>
  );
}
