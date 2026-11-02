import { Boxes, LineChart, ShieldCheck } from 'lucide-react';

export default function About() {
  return (
    <div className="mx-auto max-w-3xl px-6 py-16">
      <h1 className="text-3xl font-bold text-text-primary">About ElectroHub</h1>
      <p className="mt-4 text-text-secondary">
        ElectroHub is a modern electronic store inventory management system, built to simplify
        operations for retail teams while giving customers a straightforward place to browse and
        buy electronics — real-time stock across every location, sales and purchasing workflows,
        and reporting that reflects what's actually happening in the business.
      </p>

      <div className="mt-10 grid gap-6 sm:grid-cols-3">
        <div>
          <Boxes className="text-brand-600" size={24} />
          <h2 className="mt-3 font-semibold text-text-primary">Real-time inventory</h2>
          <p className="mt-1 text-sm text-text-secondary">Stock levels stay accurate across every store location, always.</p>
        </div>
        <div>
          <LineChart className="text-brand-600" size={24} />
          <h2 className="mt-3 font-semibold text-text-primary">Smart analytics</h2>
          <p className="mt-1 text-sm text-text-secondary">Sales trends, top sellers, and margins — decisions backed by data.</p>
        </div>
        <div>
          <ShieldCheck className="text-brand-600" size={24} />
          <h2 className="mt-3 font-semibold text-text-primary">Seamless operations</h2>
          <p className="mt-1 text-sm text-text-secondary">Purchases, sales, and returns, all in one connected system.</p>
        </div>
      </div>
    </div>
  );
}
