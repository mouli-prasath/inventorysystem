import { useState } from 'react';
import { ScanLine, Search } from 'lucide-react';
import { productApi } from '../../api/catalog';
import { getErrorMessage } from '../../api/client';
import { PageHeader, Input, Button, Card, StatusBadge } from '../../components/ui';

export default function BarcodeScanner() {
  const [code, setCode] = useState('');
  const [result, setResult] = useState(null);
  const [notFound, setNotFound] = useState(false);
  const [error, setError] = useState('');
  const [searching, setSearching] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    setSearching(true);
    setError('');
    setNotFound(false);
    setResult(null);
    try {
      const { data } = await productApi.search({ keyword: code, size: 1 });
      if (data.content?.length) {
        setResult(data.content[0]);
      } else {
        setNotFound(true);
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSearching(false);
    }
  };

  return (
    <div>
      <PageHeader
        title="Barcode Scanner"
        subtitle="Look up a product by scanning or typing its barcode/SKU."
      />

      <Card className="max-w-lg p-6">
        <div className="mb-4 flex justify-center text-brand-600">
          <ScanLine size={48} />
        </div>
        <p className="mb-4 text-center text-xs text-text-muted">
          Camera-based scanning isn't wired in yet — type or paste a barcode/SKU below.
        </p>
        <form onSubmit={handleSearch} className="flex gap-2">
          <Input value={code} onChange={(e) => setCode(e.target.value)} placeholder="Scan or type barcode…" autoFocus className="flex-1" />
          <Button type="submit" icon={Search} disabled={searching}>{searching ? '…' : 'Look up'}</Button>
        </form>

        {error && <p className="mt-4 text-sm text-status-danger">{error}</p>}
        {notFound && <p className="mt-4 text-sm text-text-secondary">No product matched that code.</p>}

        {result && (
          <div className="mt-4 rounded-lg border border-border p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="font-semibold text-text-primary">{result.name}</p>
                <p className="text-xs text-text-muted">SKU: {result.sku}</p>
              </div>
              <StatusBadge status={result.stockQuantity <= 0 ? 'OUT_OF_STOCK' : result.lowStock ? 'LOW_STOCK' : 'IN_STOCK'} />
            </div>
            <div className="mt-2 flex justify-between text-sm">
              <span className="text-text-secondary">Price</span>
              <span className="font-medium text-text-primary">${Number(result.effectivePrice).toFixed(2)}</span>
            </div>
            <div className="mt-1 flex justify-between text-sm">
              <span className="text-text-secondary">Stock on hand</span>
              <span className="font-medium text-text-primary">{result.stockQuantity}</span>
            </div>
          </div>
        )}
      </Card>
    </div>
  );
}
