import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, ImageOff, ShoppingCart, Truck, ShieldCheck, CheckCircle2 } from 'lucide-react';
import { productApi } from '../api/catalog';
import { useCart } from '../context/CartContext';
import { Button, StatusBadge } from '../components/ui';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addItem } = useCart();
  const [product, setProduct] = useState(null);
  const [added, setAdded] = useState(false);

  useEffect(() => {
    productApi.getById(id).then((res) => setProduct(res.data)).catch(() => setProduct(null));
  }, [id]);

  if (!product) {
    return <div className="mx-auto max-w-7xl px-6 py-10 text-text-secondary">Loading…</div>;
  }

  const inStock = product.stockQuantity > 0;

  const handleAddToCart = () => {
    addItem(product, 1);
    setAdded(true);
    setTimeout(() => setAdded(false), 1500);
  };

  const handleBuyNow = () => {
    addItem(product, 1);
    navigate('/cart');
  };

  return (
    <div className="mx-auto max-w-5xl px-6 py-10">
      <Link to="/products" className="inline-flex items-center gap-1 text-sm text-text-secondary hover:text-text-primary">
        <ArrowLeft size={14} /> Back to shop
      </Link>

      <div className="mt-6 grid gap-10 md:grid-cols-2">
        <div className="flex aspect-square items-center justify-center rounded-card bg-card border border-border">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} className="h-full w-full rounded-card object-cover" />
          ) : (
            <ImageOff className="text-text-muted" size={48} />
          )}
        </div>

        <div>
          <p className="text-xs uppercase tracking-wide text-text-muted">{product.categoryName}</p>
          <h1 className="mt-1 text-3xl font-bold text-text-primary">{product.name}</h1>
          <div className="mt-3 flex items-center gap-3">
            <span className="text-2xl font-bold text-text-primary">
              ${Number(product.effectivePrice ?? product.price).toFixed(2)}
            </span>
            <StatusBadge status={inStock ? 'IN_STOCK' : 'OUT_OF_STOCK'} label={inStock ? 'In stock' : 'Out of stock'} />
          </div>

          {product.description && <p className="mt-4 text-sm text-text-secondary">{product.description}</p>}

          <ul className="mt-4 space-y-1.5 text-sm text-text-secondary">
            <li className="flex items-center gap-2">
              <CheckCircle2 size={14} className="text-status-success" /> SKU: {product.sku}
            </li>
            {product.supplierName && (
              <li className="flex items-center gap-2">
                <CheckCircle2 size={14} className="text-status-success" /> Sold by {product.supplierName}
              </li>
            )}
            {product.warrantyPeriod > 0 && (
              <li className="flex items-center gap-2">
                <CheckCircle2 size={14} className="text-status-success" /> {product.warrantyPeriod}-month manufacturer warranty
              </li>
            )}
          </ul>

          <div className="mt-6 flex gap-3">
            <Button icon={ShoppingCart} onClick={handleAddToCart} disabled={!inStock}>
              {added ? 'Added ✓' : 'Add to cart'}
            </Button>
            <Button variant="secondary" onClick={handleBuyNow} disabled={!inStock}>
              Buy now
            </Button>
          </div>

          <div className="mt-6 grid grid-cols-2 gap-3">
            <div className="flex items-center gap-2 rounded-lg border border-border p-3 text-xs text-text-secondary">
              <Truck size={16} /> Free 2-day shipping
            </div>
            <div className="flex items-center gap-2 rounded-lg border border-border p-3 text-xs text-text-secondary">
              <ShieldCheck size={16} /> Secure checkout
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
