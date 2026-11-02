import { Link } from 'react-router-dom';
import { ImageOff } from 'lucide-react';

export default function ProductCard({ product }) {
  return (
    <Link
      to={`/products/${product.id}`}
      className="group block overflow-hidden rounded-card border border-border bg-card shadow-sm transition-shadow hover:shadow-md"
    >
      <div className="flex aspect-square items-center justify-center bg-surface">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} className="h-full w-full object-cover" />
        ) : (
          <ImageOff className="text-text-muted" size={32} />
        )}
      </div>
      <div className="p-4">
        <p className="text-xs uppercase tracking-wide text-text-muted">
          {product.categoryName || 'Uncategorized'}
        </p>
        <h3 className="mt-1 truncate text-sm font-semibold text-text-primary group-hover:text-brand-600">
          {product.name}
        </h3>
        <p className="mt-1 text-base font-bold text-text-primary">
          ${Number(product.effectivePrice ?? product.price).toFixed(2)}
        </p>
      </div>
    </Link>
  );
}
