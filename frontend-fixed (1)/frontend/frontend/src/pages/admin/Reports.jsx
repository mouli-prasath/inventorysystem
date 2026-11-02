import { useEffect, useState } from 'react';
import { Download, DollarSign, TrendingUp, ShoppingBag, BarChart3 } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import { reportApi } from '../../api/admin';
import { PageHeader, Button, StatCard, Card, Table, TableHead, TableBody, Th, Td } from '../../components/ui';

const DAYS_BACK = 14;

function lastNDays(n) {
  return Array.from({ length: n }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (n - 1 - i));
    return d.toISOString().slice(0, 10);
  });
}

const EMPTY_SUMMARY = { totalRevenue: 0, transactionCount: 0, averageOrderValue: 0 };
const EMPTY_VALUATION = { potentialProfit: 0 };

export default function Reports() {
  const [summary, setSummary] = useState(EMPTY_SUMMARY);
  const [valuation, setValuation] = useState(EMPTY_VALUATION);
  const [trend, setTrend] = useState([]);
  const [topProducts, setTopProducts] = useState([]);
  const [thinMargins, setThinMargins] = useState([]);
  const [loading, setLoading] = useState(true);
  const [valuationUnavailable, setValuationUnavailable] = useState(false);

  useEffect(() => {
    const days = lastNDays(DAYS_BACK);
    const start = `${days[0]}T00:00:00`;
    const end = `${days[days.length - 1]}T23:59:59`;

    // reportApi.inventoryValuation() is Inventory Manager+ only on the backend,
    // so a Sales Associate viewing this page (who can see sales reports but not
    // inventory valuation) would 403 on that one call. Use allSettled so that
    // single denial degrades just the "Potential Profit" card instead of
    // crashing the whole page (same fix as the Dashboard/Sales pages earlier).
    Promise.allSettled([
      reportApi.salesSummary(start, end),
      reportApi.inventoryValuation(),
      Promise.all(days.map((d) => reportApi.dailySales(d).then((r) => ({
        date: d.slice(5),
        orders: r.data.transactionCount,
      })))),
      reportApi.topProducts(start, end, 5),
      reportApi.profitMargins(5, true), // lowest margins first - candidates for a pricing review
    ])
      .then(([summaryRes, valuationRes, trendRes, topRes, marginsRes]) => {
        if (summaryRes.status === 'fulfilled') setSummary(summaryRes.value.data);
        if (valuationRes.status === 'fulfilled') {
          setValuation(valuationRes.value.data);
        } else {
          setValuationUnavailable(true);
        }
        if (trendRes.status === 'fulfilled') setTrend(trendRes.value);
        if (topRes.status === 'fulfilled') setTopProducts(topRes.value.data.content || []);
        if (marginsRes.status === 'fulfilled') setThinMargins(marginsRes.value.data || []);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <PageHeader
        title="Reports & Analytics"
        subtitle="Track performance across your business."
        action={<Button variant="secondary" icon={Download}>Export</Button>}
      />

      <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard
          label="Total Sales"
          value={loading ? '…' : `$${Number(summary.totalRevenue).toLocaleString(undefined, { maximumFractionDigits: 0 })}`}
          icon={DollarSign}
        />
        <StatCard
          label="Potential Profit"
          value={loading ? '…' : valuationUnavailable ? 'N/A' : `$${Number(valuation.potentialProfit).toLocaleString(undefined, { maximumFractionDigits: 0 })}`}
          icon={TrendingUp}
          tone="success"
        />
        <StatCard
          label="Total Orders"
          value={loading ? '…' : summary.transactionCount}
          icon={ShoppingBag}
        />
        <StatCard
          label="Avg Order Value"
          value={loading ? '…' : `$${Number(summary.averageOrderValue).toFixed(2)}`}
          icon={BarChart3}
        />
      </div>

      <Card className="p-5">
        <h2 className="font-semibold text-text-primary">Sales & Orders — Last {DAYS_BACK} days</h2>
        <div className="mt-4 h-72">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={trend}>
              <XAxis dataKey="date" tick={{ fontSize: 11 }} stroke="#94a3b8" />
              <YAxis tick={{ fontSize: 11 }} stroke="#94a3b8" />
              <Tooltip />
              <Bar dataKey="orders" fill="#2563eb" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </Card>

      <Card className="mt-6 p-5">
        <h2 className="font-semibold text-text-primary">Top Selling Products</h2>
        <div className="mt-4">
          <Table>
            <TableHead>
              <Th>Product</Th>
              <Th>SKU</Th>
              <Th>Units Sold</Th>
              <Th>Revenue</Th>
            </TableHead>
            <TableBody>
              {topProducts.map((p) => (
                <tr key={p.productId}>
                  <Td className="font-medium">{p.productName}</Td>
                  <Td>{p.sku}</Td>
                  <Td>{p.quantitySold}</Td>
                  <Td>${Number(p.revenue).toFixed(2)}</Td>
                </tr>
              ))}
            </TableBody>
          </Table>
        </div>
      </Card>

      <Card className="mt-6 p-5">
        <h2 className="font-semibold text-text-primary">Thinnest Profit Margins</h2>
        <p className="text-xs text-text-secondary">Candidates for a pricing review.</p>
        <div className="mt-4">
          <Table>
            <TableHead>
              <Th>Product</Th>
              <Th>SKU</Th>
              <Th>Price</Th>
              <Th>Cost</Th>
              <Th>Margin</Th>
            </TableHead>
            <TableBody>
              {thinMargins.map((m) => (
                <tr key={m.productId}>
                  <Td className="font-medium">{m.productName}</Td>
                  <Td>{m.sku}</Td>
                  <Td>${Number(m.price).toFixed(2)}</Td>
                  <Td>${Number(m.costPrice).toFixed(2)}</Td>
                  <Td className={Number(m.marginPercent) < 10 ? 'text-status-danger' : 'text-text-primary'}>
                    {Number(m.marginPercent).toFixed(1)}%
                  </Td>
                </tr>
              ))}
            </TableBody>
          </Table>
        </div>
      </Card>
    </div>
  );
}