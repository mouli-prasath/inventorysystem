import { useState } from 'react';
import { notificationApi } from '../../api/admin';
import { getErrorMessage } from '../../api/client';
import { ROLES } from '../../constants/roles';
import { Modal, Button, Input } from '../ui';

const TYPES = ['SYSTEM_ALERT', 'GENERAL', 'ACCOUNT'];
const ALL_ROLES = Object.values(ROLES);

export default function BroadcastNotificationModal({ onClose, onSent }) {
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [type, setType] = useState('SYSTEM_ALERT');
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');

  const toggleRole = (role) => {
    setSelectedRoles((prev) => prev.includes(role) ? prev.filter((r) => r !== role) : [...prev, role]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSending(true);
    setError('');
    try {
      const { data } = await notificationApi.broadcast({
        title, message, type,
        roles: selectedRoles.length > 0 ? selectedRoles : undefined,
      });
      onSent(data.recipientCount);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSending(false);
    }
  };

  return (
    <Modal title="Broadcast Notification" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        <Input label="Title" value={title} onChange={(e) => setTitle(e.target.value)} required />
        <Input label="Message" value={message} onChange={(e) => setMessage(e.target.value)} required />

        <div>
          <label className="mb-1.5 block text-sm font-medium text-text-primary">Type</label>
          <select
            value={type}
            onChange={(e) => setType(e.target.value)}
            className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            {TYPES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
          </select>
        </div>

        <div>
          <label className="mb-1.5 block text-sm font-medium text-text-primary">
            Send to roles (leave empty for everyone)
          </label>
          <div className="flex flex-wrap gap-2">
            {ALL_ROLES.map((role) => (
              <button
                type="button"
                key={role}
                onClick={() => toggleRole(role)}
                className={`rounded-full border px-3 py-1 text-xs font-medium ${
                  selectedRoles.includes(role)
                    ? 'border-brand-600 bg-brand-600 text-white'
                    : 'border-border text-text-secondary'
                }`}
              >
                {role.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>

        {error && <p className="text-sm text-status-danger">{error}</p>}

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" disabled={sending}>{sending ? 'Sending…' : 'Send'}</Button>
        </div>
      </form>
    </Modal>
  );
}
