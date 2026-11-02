import { useEffect, useState } from 'react';
import { DollarSign, ShoppingBag, Package, AlertTriangle } from 'lucide-react';
import {
  LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import { reportApi, adminApi } from '../../api/admin';
import { productApi } from '../../api/catalog';
import { StatCard, Card } from '../../components/ui';

const DAYS_BACK = 14;

function lastNDays(n) {
  return Array.from({ length: n }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (n - 1 - i));
    return d.toISOString().slice(0, 10);
  });
}

const EMPTY_STATS = { totalSales: 0, totalOrders: 0, totalProducts: 0, lowStockItems: 0 };

export default function Dashboard() {
  const [stats, setStats] = useState(EMPTY_STATS);
  const [salesTrend, setSalesTrend] = useState([]);
  const [inventoryStatus, setInventoryStatus] = useState([]);
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);
  const [reportsUnavailable, setReportsUnavailable] = useState(false);

  useEffect(() => {
    let cancelled = false;

    adminApi.systemHealth().then((res) => setHealth(res.data)).catch(() => setHealth({ status: 'UNKNOWN' }));

    const days = lastNDays(DAYS_BACK);

    const salesTrendPromise = Promise.all(
      days.map((date) => reportApi.dailySales(date).then((res) => ({
        date: date.slice(5),
        revenue: Number(res.data.totalRevenue),
      })))
    );

    const totalsPromise = reportApi.salesSummary(
      `${days[0]}T00:00:00`,
      `${days[days.length - 1]}T23:59:59`
    );

    const lowStockPromise = productApi.lowStock();
    const productCountPromise = productApi.search({ size: 1 });

    const inventoryBucketsPromise = productApi.search({ size: 200 }).then((res) => {
      const buckets = { 'In Stock': 0, 'Low Stock': 0, 'Out of Stock': 0 };
      (res.data.content || []).forEach((p) => {
        if (p.stockQuantity <= 0) buckets['Out of Stock']++;
        else if (p.lowStock) buckets['Low Stock']++;
        else buckets['In Stock']++;
      });
      return buckets;
    });

    // Some of these calls are role-gated on the backend (e.g. sales reports
    // require SALES_ASSOCIATE+, inventory numbers require INVENTORY_MANAGER+).
    // Use allSettled so one role-based 403 (or any other single failure)
    // doesn't take down the whole dashboard - each widget degrades on its own.
    Promise.allSettled([
      salesTrendPromise,
      totalsPromise,
      lowStockPromise,
      productCountPromise,
      inventoryBucketsPromise,
    ]).then(([trendResult, totalsResult, lowStockResult, productCountResult, bucketsResult]) => {
      if (cancelled) return;

      if (trendResult.status === 'fulfilled') {
        setSalesTrend(trendResult.value);
      } else {
        setReportsUnavailable(true);
      }

      setStats({
        totalSales: totalsResult.status === 'fulfilled' ? Number(totalsResult.value.data.totalRevenue) : 0,
        totalOrders: totalsResult.status === 'fulfilled' ? totalsResult.value.data.transactionCount : 0,
        totalProducts: productCountResult.status === 'fulfilled' ? productCountResult.value.data.totalElements : 0,
        lowStockItems: lowStockResult.status === 'fulfilled' ? lowStockResult.value.data.length : 0,
      });

      if (totalsResult.status === 'rejected') setReportsUnavailable(true);

      if (bucketsResult.status === 'fulfilled') {
        const buckets = bucketsResult.value;
        setInventoryStatus([
          { name: 'In Stock', value: buckets['In Stock'], color: '#16a34a' },
          { name: 'Low Stock', value: buckets['Low Stock'], color: '#d97706' },
          { name: 'Out of Stock', value: buckets['Out of Stock'], color: '#dc2626' },
        ]);
      }

      setLoading(false);
    });

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Dashboard</h1>
          <p className="mt-1 text-sm text-text-secondary">Welcome back — here's what's happening today.</p>
        </div>
        {health && (
          <span className={`flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-medium ${
            health.status === 'UP' ? 'bg-status-success-bg text-status-success' : 'bg-status-warning-bg text-status-warning'
          }`}>
            <span className={`h-1.5 w-1.5 rounded-full ${health.status === 'UP' ? 'bg-status-success' : 'bg-status-warning'}`} />
            System {health.status === 'UP' ? 'operational' : health.status.toLowerCase()}
          </span>
        )}
      </div>

      <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          label="Total Sales"
          value={loading ? '…' : `$${stats.totalSales.toLocaleString(undefined, { maximumFractionDigits: 0 })}`}
          icon={DollarSign}
          tone="info"
        />
        <StatCard
          label="Total Orders"
          value={loading ? '…' : stats.totalOrders.toLocaleString()}
          icon={ShoppingBag}
          tone="info"
        />
        <StatCard
          label="Total Products"
          value={loading ? '…' : stats.totalProducts.toLocaleString()}
          icon={Package}
          tone="success"
        />
        <StatCard
          label="Low Stock Items"
          value={loading ? '…' : stats.lowStockItems.toLocaleString()}
          icon={AlertTriangle}
          tone="warning"
        />
      </div>

      <div className="mt-6 grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Card className="p-5 lg:col-span-2">
          <h2 className="font-semibold text-text-primary">Sales Overview</h2>
          <p className="text-xs text-text-secondary">Last {DAYS_BACK} days</p>
          {!loading && reportsUnavailable ? (
            <div className="mt-4 flex h-64 items-center justify-center text-sm text-text-secondary">
              Sales reports aren't available for your role.
            </div>
          ) : (
            <div className="mt-4 h-64">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={salesTrend}>
                  <XAxis dataKey="date" tick={{ fontSize: 11 }} stroke="#94a3b8" />
                  <YAxis tick={{ fontSize: 11 }} stroke="#94a3b8" />
                  <Tooltip formatter={(v) => [`$${v.toFixed(2)}`, 'Revenue']} />
                  <Line type="monotone" dataKey="revenue" stroke="#2563eb" strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          )}
        </Card>

        <Card className="p-5">
          <h2 className="font-semibold text-text-primary">Inventory Status</h2>
          <div className="mt-4 h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={inventoryStatus} dataKey="value" nameKey="name" innerRadius={55} outerRadius={80}>
                  {inventoryStatus.map((entry) => <Cell key={entry.name} fill={entry.color} />)}
                </Pie>
                <Legend verticalAlign="bottom" height={36} iconSize={8} wrapperStyle={{ fontSize: 12 }} />
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>
    </div>
  );
}