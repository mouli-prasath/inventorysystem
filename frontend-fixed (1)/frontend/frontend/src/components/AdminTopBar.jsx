import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { PanelLeft, Search, Bell } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { notificationApi } from '../api/admin';
import { Avatar } from './ui';

export default function AdminTopBar() {
  const { user } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    notificationApi.unreadCount()
      .then((res) => setUnreadCount(res.data.unreadCount))
      .catch(() => {});
  }, []);

  return (
    <header className="flex items-center justify-between border-b border-border bg-card px-6 py-3">
      <div className="flex items-center gap-4">
        <button className="text-text-secondary hover:text-text-primary lg:hidden">
          <PanelLeft size={20} />
        </button>
        <div className="relative hidden sm:block">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
          <input
            placeholder="Search anything…"
            className="w-72 rounded-lg border border-border bg-surface py-2 pl-9 pr-3 text-sm
              focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>
      </div>

      <div className="flex items-center gap-4">
        <Link to="/admin/notifications" className="relative text-text-secondary hover:text-text-primary">
          <Bell size={19} />
          {unreadCount > 0 && (
            <span className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-status-danger text-[10px] font-bold text-white">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          )}
        </Link>
        <div className="flex items-center gap-2">
          <Avatar firstName={user?.firstName} lastName={user?.lastName} size={32} />
          <div className="hidden text-right text-sm sm:block">
            <p className="font-medium text-text-primary">{user?.firstName} {user?.lastName}</p>
            <p className="text-xs text-text-secondary">{formatRole(user?.role)}</p>
          </div>
        </div>
      </div>
    </header>
  );
}

function formatRole(role) {
  if (!role) return '';
  return role.toLowerCase().split('_').map((w) => w[0].toUpperCase() + w.slice(1)).join(' ');
}
