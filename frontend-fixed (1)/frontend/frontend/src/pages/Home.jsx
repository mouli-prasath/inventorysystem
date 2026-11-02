import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Boxes, LineChart, CheckCircle2, ArrowRight } from 'lucide-react';
import { productApi } from '../api/catalog';
import { reportApi } from '../api/admin';
import { useAuth } from '../context/AuthContext';
import { STAFF_ROLES } from '../constants/roles';
import { Button } from '../components/ui';
import ProductCard from '../components/ProductCard';

const FEATURES = [
  { icon: Boxes, title: 'Real-time Inventory', subtitle: 'Stock across all locations' },
  { icon: LineChart, title: 'Smart Analytics', subtitle: 'Data-driven decisions' },
  { icon: CheckCircle2, title: 'Seamless Ops', subtitle: 'Purchases, sales, returns' },
];

export default function Home() {
  const { isAuthenticated, user } = useAuth();
  const isStaff = isAuthenticated && STAFF_ROLES.includes(user?.role);
  const [topProducts, setTopProducts] = useState([]);
  const [todaySales, setTodaySales] = useState(null);

  useEffect(() => {
    productApi.search({ size: 4, sort: 'name' }).then((res) => {
      setTopProducts(res.data.content || []);
    }).catch(() => setTopProducts([]));

    // Sales summary requires auth (Sales Associate+) - only fetch for staff;
    // a public visitor or logged-in customer simply won't see this card.
    if (isStaff) {
      const today = new Date().toISOString().slice(0, 10);
      reportApi.dailySales(today).then((res) => setTodaySales(res.data)).catch(() => {});
    }
  }, [isStaff]);

  return (
    <div>
      {/* Hero */}
      <section className="relative overflow-hidden bg-gradient-to-br from-navy-900 to-navy-800 text-white">
        <div className="mx-auto max-w-7xl px-6 py-16 lg:py-24">
          <div className="grid items-center gap-10 lg:grid-cols-2">
            <div>
              <span className="inline-flex items-center gap-1.5 rounded-full bg-white/10 px-3 py-1 text-xs font-medium">
                ⚡ Now with real-time inventory
              </span>
              <h1 className="mt-4 text-4xl font-extrabold leading-tight sm:text-5xl">
                Smart Inventory.
                <br />
                Stronger Business.
              </h1>
              <p className="mt-4 max-w-md text-white/70">
                ElectroHub is a modern Electronic Store Inventory Management System to simplify
                your operations, delight customers, and grow faster.
              </p>
              <div className="mt-6 flex gap-3">
                {(!isAuthenticated || isStaff) && (
                  <Link to={isStaff ? '/dashboard' : '/register'}>
                    <Button icon={ArrowRight} size="lg">Get Started</Button>
                  </Link>
                )}
                <Link to="/products">
                  <Button variant="secondary" size="lg" className="bg-white/10 text-white border-white/20 hover:bg-white/20">
                    Browse Products
                  </Button>
                </Link>
              </div>

              <div className="mt-10 grid grid-cols-3 gap-4">
                {FEATURES.map((f) => (
                  <div key={f.title} className="rounded-lg bg-white/5 p-3">
                    <f.icon size={18} className="text-brand-400" />
                    <p className="mt-2 text-xs font-semibold">{f.title}</p>
                    <p className="text-[11px] text-white/50">{f.subtitle}</p>
                  </div>
                ))}
              </div>
            </div>

            <div className="relative hidden lg:block">
              <div className="aspect-video rounded-2xl bg-navy-700/50" />
              {todaySales && (
                <div className="absolute -bottom-6 left-6 rounded-card bg-card p-4 text-text-primary shadow-lg">
                  <p className="text-xs text-text-secondary">Today's Sales</p>
                  <p className="text-xl font-bold">${Number(todaySales.totalRevenue).toFixed(2)}</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* Top selling products */}
      <section className="mx-auto max-w-7xl px-6 py-12">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold text-text-primary">Top selling products</h2>
            <p className="text-sm text-text-secondary">Handpicked electronics loved by our customers.</p>
          </div>
          <Link to="/products" className="flex items-center gap-1 text-sm font-medium text-brand-600 hover:underline">
            View all <ArrowRight size={14} />
          </Link>
        </div>

        <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
          {topProducts.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
          {topProducts.length === 0 && (
            <p className="col-span-full text-sm text-text-secondary">No products yet.</p>
          )}
        </div>
      </section>
    </div>
  );
}