import { useEffect, useState } from 'react';
import { Search } from 'lucide-react';
import { productApi, categoryApi } from '../api/catalog';
import { Input } from '../components/ui';
import ProductCard from '../components/ProductCard';

export default function ProductCatalog() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    categoryApi.list().then((res) => setCategories(res.data)).catch(() => {});
  }, []);

  useEffect(() => {
    setLoading(true);
    const params = { size: 24 };
    if (keyword) params.keyword = keyword;
    if (categoryId) params.categoryId = categoryId;

    const timeout = setTimeout(() => {
      productApi
        .search(params)
        .then((res) => setProducts(res.data.content || []))
        .catch(() => setProducts([]))
        .finally(() => setLoading(false));
    }, 300); // debounce keyword typing

    return () => clearTimeout(timeout);
  }, [keyword, categoryId]);

  return (
    <div className="mx-auto max-w-7xl px-6 py-10">
      <h1 className="text-2xl font-bold text-text-primary">Shop</h1>
      <p className="mt-1 text-sm text-text-secondary">Browse our full electronics catalog.</p>

      <div className="mt-6 flex flex-col gap-3 sm:flex-row">
        <div className="relative flex-1">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="Search products…"
            className="w-full rounded-lg border border-border bg-card py-2 pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>
        <select
          value={categoryId}
          onChange={(e) => setCategoryId(e.target.value)}
          className="rounded-lg border border-border bg-card px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        >
          <option value="">All categories</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.categoryName}</option>
          ))}
        </select>
      </div>

      <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>

      {!loading && products.length === 0 && (
        <p className="mt-10 text-center text-sm text-text-secondary">No products found.</p>
      )}
    </div>
  );
}
