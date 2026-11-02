import { useEffect, useState } from 'react';
import { adminApi } from '../../api/admin';
import { getErrorMessage } from '../../api/client';
import { PageHeader, Table, TableHead, TableBody, Th, Td } from '../../components/ui';

export default function AuditLog() {
  const [logs, setLogs] = useState([]);
  const [actionFilter, setActionFilter] = useState('');
  const [userIdFilter, setUserIdFilter] = useState('');
  const [error, setError] = useState('');

  const load = () => {
    let call;
    if (userIdFilter) {
      call = adminApi.auditLogsByUser(userIdFilter, { size: 50, sort: 'timestamp,desc' });
    } else if (actionFilter) {
      call = adminApi.auditLogsByAction(actionFilter, { size: 50, sort: 'timestamp,desc' });
    } else {
      call = adminApi.auditLogs({ size: 50, sort: 'timestamp,desc' });
    }
    call.then((res) => setLogs(res.data.content || []))
      .catch((err) => setError(getErrorMessage(err)));
  };

  useEffect(load, [actionFilter, userIdFilter]);

  return (
    <div>
      <PageHeader title="Audit Log" subtitle="A record of security-relevant and business-sensitive actions." />

      <div className="mb-4 flex gap-3">
        <input
          value={actionFilter}
          onChange={(e) => { setActionFilter(e.target.value); setUserIdFilter(''); }}
          placeholder="Filter by action (e.g. PRICE_CHANGE_MAJOR)…"
          className="w-72 rounded-lg border border-border bg-card px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        />
        <input
          type="number"
          value={userIdFilter}
          onChange={(e) => { setUserIdFilter(e.target.value); setActionFilter(''); }}
          placeholder="Filter by user ID…"
          className="w-40 rounded-lg border border-border bg-card px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        />
      </div>

      {error && <p className="mb-4 text-sm text-status-danger">{error}</p>}

      <Table>
        <TableHead>
          <Th>Time</Th>
          <Th>User</Th>
          <Th>Action</Th>
          <Th>Entity</Th>
          <Th>Old → New</Th>
        </TableHead>
        <TableBody>
          {logs.map((log) => (
            <tr key={log.id}>
              <Td className="whitespace-nowrap text-xs">{new Date(log.timestamp).toLocaleString()}</Td>
              <Td>{log.username || 'system'}</Td>
              <Td className="font-mono text-xs">{log.action}</Td>
              <Td className="text-xs">{log.entityType}{log.entityId ? ` #${log.entityId}` : ''}</Td>
              <Td className="text-xs text-text-secondary">
                {log.oldValue || log.newValue
                  ? `${log.oldValue ?? '—'} → ${log.newValue ?? '—'}`
                  : '—'}
              </Td>
            </tr>
          ))}
        </TableBody>
      </Table>

      {logs.length === 0 && (
        <p className="mt-6 text-center text-sm text-text-secondary">No audit entries found.</p>
      )}
    </div>
  );
}
