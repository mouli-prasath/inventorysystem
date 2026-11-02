import { Link } from 'react-router-dom';
import { Mail, Minus, Plus, Trash2 } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { Button } from '../components/ui';

const TAX_RATE = 0.08;

export default function Cart() {
  const { items, updateQuantity, removeItem, subtotal } = useCart();

  if (items.length === 0) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-16 text-center">
        <h1 className="text-2xl font-bold text-text-primary">Your Cart</h1>
        <div className="mt-10 flex flex-col items-center">
          <span className="flex h-12 w-12 items-center justify-center rounded-full bg-surface">
            <Mail size={20} className="text-text-muted" />
          </span>
          <p className="mt-4 text-sm text-text-secondary">Your cart is empty.</p>
          <Link to="/products" className="mt-4">
            <Button>Continue shopping</Button>
          </Link>
        </div>
      </div>
    );
  }

  const tax = subtotal * TAX_RATE;
  const total = subtotal + tax;

  return (
    <div className="mx-auto max-w-3xl px-6 py-10">
      <h1 className="text-2xl font-bold text-text-primary">Your Cart</h1>

      <div className="mt-6 space-y-3">
        {items.map(({ product, quantity }) => (
          <div key={product.id} className="flex items-center justify-between rounded-card border border-border bg-card p-4">
            <div className="flex items-center gap-4">
              <div className="h-14 w-14 shrink-0 rounded-lg bg-surface" />
              <div>
                <p className="text-xs uppercase tracking-wide text-text-muted">{product.categoryName}</p>
                <p className="text-sm font-semibold text-text-primary">{product.name}</p>
                <div className="mt-1 flex items-center gap-2">
                  <button
                    onClick={() => updateQuantity(product.id, quantity - 1)}
                    className="flex h-6 w-6 items-center justify-center rounded border border-border text-text-secondary hover:bg-surface"
                  >
                    <Minus size={12} />
                  </button>
                  <span className="w-6 text-center text-sm">{quantity}</span>
                  <button
                    onClick={() => updateQuantity(product.id, quantity + 1)}
                    className="flex h-6 w-6 items-center justify-center rounded border border-border text-text-secondary hover:bg-surface"
                  >
                    <Plus size={12} />
                  </button>
                </div>
              </div>
            </div>
            <div className="text-right">
              <p className="text-sm font-semibold text-text-primary">
                ${(product.effectivePrice * quantity).toFixed(2)}
              </p>
              <button
                onClick={() => removeItem(product.id)}
                className="mt-1 flex items-center gap-1 text-xs text-status-danger hover:underline"
              >
                <Trash2 size={12} /> Remove
              </button>
            </div>
          </div>
        ))}
      </div>

      <div className="mt-6 rounded-card border border-border bg-card p-5">
        <h2 className="font-semibold text-text-primary">Order Summary</h2>
        <div className="mt-3 space-y-1.5 text-sm">
          <div className="flex justify-between text-text-secondary">
            <span>Subtotal</span><span>${subtotal.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-text-secondary">
            <span>Tax (8%)</span><span>${tax.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-text-secondary">
            <span>Shipping</span><span className="text-status-success">Free</span>
          </div>
          <div className="mt-2 flex justify-between border-t border-border pt-2 text-base font-bold text-text-primary">
            <span>Total</span><span>${total.toFixed(2)}</span>
          </div>
        </div>
        <Link to="/checkout">
          <Button className="mt-4 w-full">Proceed to Checkout</Button>
        </Link>
      </div>
    </div>
  );
}
