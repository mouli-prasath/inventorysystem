import { useEffect, useState } from 'react';
import { Boxes, PackageCheck, AlertTriangle, PackageX, Plus } from 'lucide-react';
import { inventoryApi } from '../../api/inventory';
import { getErrorMessage } from '../../api/client';
import { PageHeader, Button, StatCard, Table, TableHead, TableBody, Th, Td, StatusBadge } from '../../components/ui';
import StockAdjustmentModal from '../../components/admin/StockAdjustmentModal';
import InventoryDetailModal from '../../components/admin/InventoryDetailModal';

function stockStatus(item) {
  if (item.quantityOnHand <= 0) return 'OUT_OF_STOCK';
  return item.quantityAvailable <= 10 ? 'LOW_STOCK' : 'IN_STOCK'; // reorder point isn't on this DTO, so 10 is a reasonable client-side heuristic
}

export default function InventoryManagement() {
  const [allStock, setAllStock] = useState([]);
  const [stock, setStock] = useState([]);
  const [locationFilter, setLocationFilter] = useState('');
  const [lowStockAlerts, setLowStockAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showAdjustModal, setShowAdjustModal] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);

  const load = () => {
    setLoading(true);
    inventoryApi.getAllStock({ size: 100 })
      .then((res) => {
        setAllStock(res.data.content || []);
        if (!locationFilter) setStock(res.data.content || []);
      })
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false));

    inventoryApi.lowStock().then((res) => setLowStockAlerts(res.data || [])).catch(() => {});
  };

  useEffect(load, []);

  // Distinct locations, derived from whatever's already loaded.
  const locations = [...new Set(allStock.map((s) => s.storeLocation))].sort();

  const handleLocationChange = (loc) => {
    setLocationFilter(loc);
    if (!loc) {
      setStock(allStock);
      return;
    }
    inventoryApi.getByLocation(loc)
      .then((res) => setStock(res.data || []))
      .catch((err) => setError(getErrorMessage(err)));
  };

  const totals = stock.reduce(
    (acc, item) => {
      acc.total++;
      const status = stockStatus(item);
      if (status === 'IN_STOCK') acc.inStock++;
      else if (status === 'LOW_STOCK') acc.lowStock++;
      else acc.outOfStock++;
      return acc;
    },
    { total: 0, inStock: 0, lowStock: 0, outOfStock: 0 }
  );

  return (
    <div>
      <PageHeader
        title="Inventory Management"
        subtitle="Track stock across all locations."
        action={<Button icon={Plus} onClick={() => setShowAdjustModal(true)}>Adjust Stock</Button>}
      />

      <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Total Items" value={totals.total} icon={Boxes} tone="info" />
        <StatCard label="In Stock" value={totals.inStock} icon={PackageCheck} tone="success" />
        <StatCard label="Low Stock" value={totals.lowStock} icon={AlertTriangle} tone="warning" />
        <StatCard label="Out of Stock" value={totals.outOfStock} icon={PackageX} tone="danger" />
      </div>

      {error && <p className="mb-4 text-sm text-status-danger">{error}</p>}

      {lowStockAlerts.length > 0 && (
        <div className="mb-6 rounded-card border border-status-warning-bg bg-status-warning-bg/40 p-4">
          <h3 className="text-sm font-semibold text-status-warning">Reorder point alerts</h3>
          <div className="mt-2 flex flex-wrap gap-2">
            {lowStockAlerts.map((p) => (
              <span key={p.id} className="rounded-full bg-card px-3 py-1 text-xs text-text-primary shadow-sm">
                {p.name} — {p.stockQuantity}/{p.reorderPoint}
              </span>
            ))}
          </div>
        </div>
      )}

      <div className="mb-4 max-w-xs">
        <select
          value={locationFilter}
          onChange={(e) => handleLocationChange(e.target.value)}
          className="w-full rounded-lg border border-border bg-card px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        >
          <option value="">All locations</option>
          {locations.map((loc) => <option key={loc} value={loc}>{loc}</option>)}
        </select>
      </div>

      <Table>
        <TableHead>
          <Th>Product</Th>
          <Th>SKU</Th>
          <Th>Location</Th>
          <Th>Stock</Th>
          <Th>Status</Th>
        </TableHead>
        <TableBody>
          {stock.map((item) => (
            <tr
              key={item.id}
              onClick={() => setSelectedProduct({ id: item.productId, name: item.productName })}
              className="cursor-pointer hover:bg-surface"
            >
              <Td className="font-medium">{item.productName}</Td>
              <Td>{item.sku}</Td>
              <Td>{item.storeLocation}</Td>
              <Td>{item.quantityOnHand}</Td>
              <Td><StatusBadge status={stockStatus(item)} /></Td>
            </tr>
          ))}
        </TableBody>
      </Table>

      {!loading && stock.length === 0 && (
        <p className="mt-6 text-center text-sm text-text-secondary">No inventory records yet.</p>
      )}

      {showAdjustModal && (
        <StockAdjustmentModal onClose={() => setShowAdjustModal(false)} onSaved={() => { setShowAdjustModal(false); load(); }} />
      )}
      {selectedProduct && (
        <InventoryDetailModal
          productId={selectedProduct.id}
          productName={selectedProduct.name}
          onClose={() => setSelectedProduct(null)}
          onChanged={load}
        />
      )}
    </div>
  );
}
