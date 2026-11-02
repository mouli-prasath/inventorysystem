import { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { Button, Input } from '../components/ui';

const TAX_RATE = 0.08;

// Demo/showcase checkout only, per project decision - our backend models sales
// as staff-operated POS transactions (POST /api/transactions is role-restricted
// to Cashier/Sales Associate/Store Manager/Admin), so a CUSTOMER-role self
// checkout has no real endpoint to submit to. This flow demonstrates the UI
// without pretending to place a real order.
export default function Checkout() {
  const { items, subtotal, clearCart } = useCart();
  const navigate = useNavigate();
  const [placing, setPlacing] = useState(false);
  const [placed, setPlaced] = useState(false);

  if (items.length === 0 && !placed) {
    return <Navigate to="/cart" replace />;
  }

  const tax = subtotal * TAX_RATE;
  const total = subtotal + tax;

  const handlePlaceOrder = (e) => {
    e.preventDefault();
    setPlacing(true);
    setTimeout(() => {
      setPlacing(false);
      setPlaced(true);
      clearCart();
    }, 800);
  };

  if (placed) {
    return (
      <div className="mx-auto max-w-md px-6 py-20 text-center">
        <h1 className="text-2xl font-bold text-text-primary">Order placed 🎉</h1>
        <p className="mt-2 text-sm text-text-secondary">
          This is a demo checkout — no real order was submitted. In-store or phone orders are
          processed by staff through POS Billing.
        </p>
        <Button className="mt-6" onClick={() => navigate('/products')}>Continue shopping</Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-10">
      <h1 className="text-2xl font-bold text-text-primary">Checkout</h1>

      <form onSubmit={handlePlaceOrder} className="mt-6 grid gap-8 md:grid-cols-2">
        <div className="rounded-card border border-border bg-card p-5">
          <h2 className="font-semibold text-text-primary">Payment</h2>
          <div className="mt-4 space-y-4">
            <Input label="Card number" placeholder="4242 4242 4242 4242" required maxLength={19} />
            <div className="grid grid-cols-2 gap-3">
              <Input label="Expiry" placeholder="MM/YY" required maxLength={5} />
              <Input label="CVC" placeholder="123" required maxLength={4} />
            </div>
          </div>
        </div>

        <div className="rounded-card border border-border bg-card p-5">
          <h2 className="font-semibold text-text-primary">Order Summary</h2>
          <ul className="mt-3 space-y-1.5 text-sm text-text-secondary">
            {items.map(({ product, quantity }) => (
              <li key={product.id} className="flex justify-between">
                <span>{product.name} × {quantity}</span>
                <span>${(product.effectivePrice * quantity).toFixed(2)}</span>
              </li>
            ))}
          </ul>
          <div className="mt-3 flex justify-between border-t border-border pt-2 text-base font-bold text-text-primary">
            <span>Total</span><span>${total.toFixed(2)}</span>
          </div>
          <Button type="submit" disabled={placing} className="mt-4 w-full">
            {placing ? 'Placing order…' : 'Place Order'}
          </Button>
        </div>
      </form>
    </div>
  );
}
