import { Link } from 'react-router-dom';
import { Package, ShoppingCart } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { Button } from './ui';

export default function StorefrontHeader() {
  const { isAuthenticated, user, logout } = useAuth();
  const { itemCount } = useCart();

  return (
    <header className="border-b border-border bg-card">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
        <Link to="/" className="flex items-center gap-2 text-lg font-bold text-text-primary">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-white">
            <Package size={18} />
          </span>
          ElectroHub
        </Link>

        <nav className="hidden items-center gap-6 text-sm text-text-secondary md:flex">
          <Link to="/" className="hover:text-text-primary">Home</Link>
          <Link to="/products" className="hover:text-text-primary">Shop</Link>
          <Link to="/about" className="hover:text-text-primary">About</Link>
          <Link to="/contact" className="hover:text-text-primary">Contact</Link>
        </nav>

        <div className="flex items-center gap-4">
          <Link to="/cart" className="relative text-text-secondary hover:text-text-primary">
            <ShoppingCart size={20} />
            {itemCount > 0 && (
              <span className="absolute -right-2 -top-2 flex h-4 w-4 items-center justify-center rounded-full bg-brand-600 text-[10px] font-semibold text-white">
                {itemCount}
              </span>
            )}
          </Link>

          {isAuthenticated ? (
            <>
              <span className="hidden text-sm text-text-secondary sm:inline">{user.firstName}</span>
              <Button variant="ghost" size="sm" onClick={logout}>Sign out</Button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-sm font-medium text-text-secondary hover:text-text-primary">
                Sign in
              </Link>
              <Link to="/register">
                <Button size="sm">Get Started</Button>
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
