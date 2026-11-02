import { useEffect, useState } from 'react';
import { Search, Trash2, ShoppingCart } from 'lucide-react';
import { productApi } from '../../api/catalog';
import { transactionApi } from '../../api/sales';
import { getErrorMessage } from '../../api/client';
import { PageHeader, Button, Input, Card } from '../../components/ui';

const PAYMENT_METHODS = ['CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'MOBILE_PAYMENT', 'GIFT_CARD'];
const TAX_RATE = 0.08;

export default function POSBilling() {
  const [keyword, setKeyword] = useState('');
  const [results, setResults] = useState([]);
  const [lines, setLines] = useState([]); // [{ product, quantity, discountAmount }]
  const [storeLocation, setStoreLocation] = useState('Downtown');
  const [paymentMethod, setPaymentMethod] = useState('CASH');
  const [walkIn, setWalkIn] = useState({ firstName: '', lastName: '', phone: '' });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [completedSale, setCompletedSale] = useState(null);

  useEffect(() => {
    if (!keyword) { setResults([]); return; }
    const timeout = setTimeout(() => {
      productApi.search({ keyword, size: 6 }).then((res) => setResults(res.data.content || [])).catch(() => {});
    }, 250);
    return () => clearTimeout(timeout);
  }, [keyword]);

  const addLine = (product) => {
    setLines((prev) => {
      const existing = prev.find((l) => l.product.id === product.id);
      if (existing) {
        return prev.map((l) => l.product.id === product.id ? { ...l, quantity: l.quantity + 1 } : l);
      }
      return [...prev, { product, quantity: 1, discountAmount: 0 }];
    });
    setKeyword('');
    setResults([]);
  };

  const updateLine = (productId, field, value) => {
    setLines((prev) => prev.map((l) => l.product.id === productId ? { ...l, [field]: value } : l));
  };

  const removeLine = (productId) => setLines((prev) => prev.filter((l) => l.product.id !== productId));

  const subtotal = lines.reduce((sum, l) => sum + (l.product.effectivePrice * l.quantity - Number(l.discountAmount || 0)), 0);
  const tax = subtotal * TAX_RATE;
  const total = subtotal + tax;
  const hasOverStockLine = lines.some((l) => l.quantity > l.product.stockQuantity);

  const handleSubmit = async () => {
    setSubmitting(true);
    setError('');
    try {
      const payload = {
        storeLocation,
        paymentMethod,
        taxRate: TAX_RATE,
        items: lines.map((l) => ({
          productId: l.product.id,
          quantity: l.quantity,
          discountAmount: Number(l.discountAmount || 0),
        })),
      };
      if (walkIn.firstName && walkIn.lastName && walkIn.phone) {
        payload.walkInFirstName = walkIn.firstName;
        payload.walkInLastName = walkIn.lastName;
        payload.walkInPhone = walkIn.phone;
      }
      const { data } = await transactionApi.processSale(payload);
      setCompletedSale(data);
      setLines([]);
      setWalkIn({ firstName: '', lastName: '', phone: '' });
    } catch (err) {
      // Surfaces the backend's real error message directly - e.g. the >15%
      // discount-approval gate, or insufficient stock.
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <PageHeader title="POS Billing" subtitle="Process a new sale." />

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <Card className="p-4">
            <div className="relative">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Search products by name, SKU, or barcode…"
                className="w-full rounded-lg border border-border py-2 pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
              {results.length > 0 && (
                <div className="absolute z-10 mt-1 w-full rounded-lg border border-border bg-card shadow-lg">
                  {results.map((p) => (
                    <button
                      key={p.id}
                      onClick={() => addLine(p)}
                      disabled={p.stockQuantity <= 0}
                      className="flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-surface disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      <span>
                        {p.name} <span className="text-text-muted">({p.sku})</span>
                        {p.stockQuantity <= 0 ? (
                          <span className="ml-2 text-xs font-medium text-status-danger">Out of stock</span>
                        ) : (
                          <span className="ml-2 text-xs text-text-muted">{p.stockQuantity} in stock</span>
                        )}
                      </span>
                      <span className="font-medium">${Number(p.effectivePrice).toFixed(2)}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>

            <div className="mt-4 space-y-2">
              {lines.map((line) => {
                const overStock = line.quantity > line.product.stockQuantity;
                return (
                  <div key={line.product.id} className="rounded-lg border border-border p-3">
                    <div className="flex items-center gap-3">
                      <div className="flex-1">
                        <p className="text-sm font-medium text-text-primary">{line.product.name}</p>
                        <p className="text-xs text-text-muted">
                          ${Number(line.product.effectivePrice).toFixed(2)} each · {line.product.stockQuantity} available
                        </p>
                      </div>
                      <input
                        type="number"
                        min="1"
                        max={line.product.stockQuantity}
                        value={line.quantity}
                        onChange={(e) => updateLine(line.product.id, 'quantity', Number(e.target.value))}
                        className={`w-16 rounded border px-2 py-1 text-center text-sm ${overStock ? 'border-status-danger' : 'border-border'}`}
                      />
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        value={line.discountAmount}
                        onChange={(e) => updateLine(line.product.id, 'discountAmount', e.target.value)}
                        placeholder="Discount"
                        className="w-20 rounded border border-border px-2 py-1 text-sm"
                      />
                      <button onClick={() => removeLine(line.product.id)} className="text-text-secondary hover:text-status-danger">
                        <Trash2 size={16} />
                      </button>
                    </div>
                    {overStock && (
                      <p className="mt-1.5 text-xs text-status-danger">
                        Only {line.product.stockQuantity} in stock - reduce quantity to complete this sale.
                      </p>
                    )}
                  </div>
                );
              })}
              {lines.length === 0 && (
                <p className="py-8 text-center text-sm text-text-secondary">Search above to add items.</p>
              )}
            </div>
          </Card>

          <Card className="mt-4 p-4">
            <h3 className="text-sm font-semibold text-text-primary">Walk-in customer (optional)</h3>
            <p className="mt-1 text-xs text-text-secondary">
              If provided and the sale is over $100, a customer account is created automatically.
            </p>
            <div className="mt-3 grid grid-cols-3 gap-3">
              <Input placeholder="First name" value={walkIn.firstName} onChange={(e) => setWalkIn({ ...walkIn, firstName: e.target.value })} />
              <Input placeholder="Last name" value={walkIn.lastName} onChange={(e) => setWalkIn({ ...walkIn, lastName: e.target.value })} />
              <Input placeholder="Phone" value={walkIn.phone} onChange={(e) => setWalkIn({ ...walkIn, phone: e.target.value })} />
            </div>
          </Card>
        </div>

        <Card className="h-fit p-4">
          <h2 className="font-semibold text-text-primary">Order Summary</h2>

          <div className="mt-3 space-y-3">
            <Input label="Store location" value={storeLocation} onChange={(e) => setStoreLocation(e.target.value)} />
            <div>
              <label className="mb-1.5 block text-sm font-medium text-text-primary">Payment method</label>
              <select
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value)}
                className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              >
                {PAYMENT_METHODS.map((m) => <option key={m} value={m}>{m.replace('_', ' ')}</option>)}
              </select>
            </div>
          </div>

          <div className="mt-4 space-y-1.5 border-t border-border pt-3 text-sm">
            <div className="flex justify-between text-text-secondary"><span>Subtotal</span><span>${subtotal.toFixed(2)}</span></div>
            <div className="flex justify-between text-text-secondary"><span>Tax (8%)</span><span>${tax.toFixed(2)}</span></div>
            <div className="flex justify-between border-t border-border pt-2 text-base font-bold text-text-primary">
              <span>Total</span><span>${total.toFixed(2)}</span>
            </div>
          </div>

          {error && <p className="mt-3 rounded-lg bg-status-danger-bg p-2 text-xs text-status-danger">{error}</p>}
          {completedSale && (
            <p className="mt-3 rounded-lg bg-status-success-bg p-2 text-xs text-status-success">
              Sale {completedSale.transactionNumber} completed.
            </p>
          )}

          <Button
            icon={ShoppingCart}
            className="mt-4 w-full"
            disabled={lines.length === 0 || submitting || hasOverStockLine}
            onClick={handleSubmit}
          >
            {submitting ? 'Processing…' : hasOverStockLine ? 'Reduce quantity to continue' : 'Complete Sale'}
          </Button>
        </Card>
      </div>
    </div>
  );
}