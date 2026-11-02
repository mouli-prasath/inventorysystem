import { NavLink } from 'react-router-dom';
import {
  LayoutGrid, Package, Tags, Percent, Warehouse, TrendingUp, ShoppingCart, ClipboardList,
  Users, Truck, BarChart3, UserCog, FileClock, ScanLine, Bell, User, Settings, Package as Logo,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Avatar } from './ui';
import { INVENTORY_ROLES, POS_ROLES, REPORTS_ROLES, ROLES, STAFF_ROLES } from '../constants/roles';

const WORKSPACE_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutGrid, roles: STAFF_ROLES },
  { to: '/admin/products', label: 'Products', icon: Package, roles: INVENTORY_ROLES },
  { to: '/admin/categories', label: 'Categories', icon: Tags, roles: INVENTORY_ROLES },
  { to: '/admin/promotions', label: 'Promotions', icon: Percent, roles: INVENTORY_ROLES },
  { to: '/admin/inventory', label: 'Inventory', icon: Warehouse, roles: INVENTORY_ROLES },
  { to: '/admin/sales', label: 'Sales', icon: TrendingUp, roles: STAFF_ROLES },
  { to: '/admin/pos', label: 'POS Billing', icon: ShoppingCart, roles: POS_ROLES },
  { to: '/admin/purchases', label: 'Purchases', icon: ClipboardList, roles: INVENTORY_ROLES },
  { to: '/admin/customers', label: 'Customers', icon: Users, roles: STAFF_ROLES },
  { to: '/admin/suppliers', label: 'Suppliers', icon: Truck, roles: INVENTORY_ROLES },
  { to: '/admin/reports', label: 'Reports', icon: BarChart3, roles: REPORTS_ROLES },
  { to: '/admin/users', label: 'Users', icon: UserCog, roles: [ROLES.ADMIN, ROLES.STORE_MANAGER] },
  { to: '/admin/audit-log', label: 'Audit Log', icon: FileClock, roles: [ROLES.ADMIN, ROLES.STORE_MANAGER] },
  { to: '/admin/barcode-scanner', label: 'Barcode Scanner', icon: ScanLine, roles: STAFF_ROLES },
  { to: '/admin/notifications', label: 'Notifications', icon: Bell },
];

const ACCOUNT_ITEMS = [
  { to: '/admin/profile', label: 'Profile', icon: User },
  { to: '/admin/settings', label: 'Settings', icon: Settings, roles: [ROLES.ADMIN] },
];

function NavItem({ to, label, icon: Icon }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors ${
          isActive
            ? 'bg-brand-600 text-white'
            : 'text-white/60 hover:bg-white/5 hover:text-white'
        }`
      }
    >
      <Icon size={17} />
      {label}
    </NavLink>
  );
}

export default function AdminSidebar() {
  const { user, hasRole } = useAuth();
  const visibleWorkspaceItems = WORKSPACE_ITEMS.filter((item) => !item.roles || hasRole(...item.roles));
  const visibleAccountItems = ACCOUNT_ITEMS.filter((item) => !item.roles || hasRole(...item.roles));

  return (
    <aside className="flex h-screen w-64 shrink-0 flex-col bg-navy-900 text-white">
      <div className="flex items-center gap-2 px-5 py-5 text-base font-bold">
        <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600">
          <Logo size={18} />
        </span>
        ElectroHub
      </div>

      <nav className="flex-1 overflow-y-auto px-3">
        <p className="px-3 pb-2 pt-3 text-[11px] font-semibold uppercase tracking-wide text-white/30">
          Workspace
        </p>
        <div className="space-y-1">
          {visibleWorkspaceItems.map((item) => <NavItem key={item.to} {...item} />)}
        </div>

        <p className="px-3 pb-2 pt-5 text-[11px] font-semibold uppercase tracking-wide text-white/30">
          Account
        </p>
        <div className="space-y-1">
          {visibleAccountItems.map((item) => <NavItem key={item.to} {...item} />)}
        </div>
      </nav>

      <div className="flex items-center gap-3 border-t border-white/10 px-4 py-4">
        <Avatar firstName={user?.firstName} lastName={user?.lastName} size={32} />
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-medium">{user?.firstName} {user?.lastName}</p>
          <p className="truncate text-xs text-white/50">{formatRole(user?.role)}</p>
        </div>
      </div>
    </aside>
  );
}

function formatRole(role) {
  if (!role) return '';
  return role.toLowerCase().split('_').map((w) => w[0].toUpperCase() + w.slice(1)).join(' ');
}