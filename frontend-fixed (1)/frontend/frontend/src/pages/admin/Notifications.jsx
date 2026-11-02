import { useEffect, useState } from 'react';
import { Bell, Send } from 'lucide-react';
import { notificationApi } from '../../api/admin';
import { getErrorMessage } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { PageHeader, Button, Card } from '../../components/ui';
import BroadcastNotificationModal from '../../components/admin/BroadcastNotificationModal';

function timeAgo(dateStr) {
  const seconds = Math.floor((Date.now() - new Date(dateStr)) / 1000);
  if (seconds < 60) return 'Just now';
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} hour${hours > 1 ? 's' : ''} ago`;
  return `${Math.floor(hours / 24)} day(s) ago`;
}

export default function Notifications() {
  const { hasRole } = useAuth();
  const canBroadcast = hasRole('STORE_MANAGER', 'ADMIN');
  const [notifications, setNotifications] = useState([]);
  const [error, setError] = useState('');
  const [showBroadcastModal, setShowBroadcastModal] = useState(false);
  const [sentMessage, setSentMessage] = useState('');

  const load = () => {
    notificationApi.list({ size: 30, sort: 'createdDate,desc' })
      .then((res) => setNotifications(res.data.content || []))
      .catch((err) => setError(getErrorMessage(err)));
  };

  useEffect(load, []);

  const markRead = async (id) => {
    try {
      await notificationApi.markRead(id);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const markAllRead = async () => {
    try {
      await notificationApi.markAllRead();
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader
        title="Notifications"
        subtitle="Stay on top of what's happening."
        action={
          <div className="flex gap-2">
            {canBroadcast && (
              <Button variant="secondary" icon={Send} onClick={() => setShowBroadcastModal(true)}>Broadcast</Button>
            )}
            <Button variant="secondary" onClick={markAllRead}>Mark all as read</Button>
          </div>
        }
      />

      {error && <p className="mb-4 text-sm text-status-danger">{error}</p>}
      {sentMessage && <p className="mb-4 text-sm text-status-success">{sentMessage}</p>}

      <Card>
        {notifications.map((n) => (
          <button
            key={n.id}
            onClick={() => !n.read && markRead(n.id)}
            className="flex w-full items-start gap-3 border-b border-border px-4 py-3 text-left last:border-0 hover:bg-surface"
          >
            <span className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-brand-100 text-brand-600">
              <Bell size={14} />
            </span>
            <div className="flex-1">
              <p className="text-sm text-text-primary">{n.title ? `${n.title} — ${n.message}` : n.message}</p>
              <p className="mt-0.5 text-xs text-text-muted">{timeAgo(n.createdDate)}</p>
            </div>
            {!n.read && <span className="mt-2 h-2 w-2 shrink-0 rounded-full bg-brand-600" />}
          </button>
        ))}
        {notifications.length === 0 && (
          <p className="px-4 py-8 text-center text-sm text-text-secondary">No notifications yet.</p>
        )}
      </Card>

      {showBroadcastModal && (
        <BroadcastNotificationModal
          onClose={() => setShowBroadcastModal(false)}
          onSent={(count) => {
            setShowBroadcastModal(false);
            setSentMessage(`Sent to ${count} recipient${count === 1 ? '' : 's'}.`);
            load();
          }}
        />
      )}
    </div>
  );
}
