import { Package } from 'lucide-react';

const COLUMNS = [
  { title: 'Shop', links: ['All Products', 'Smartphones', 'Laptops', 'Accessories'] },
  { title: 'Company', links: ['About', 'Contact', 'Careers'] },
  { title: 'Legal', links: ['Privacy', 'Terms'] },
];

export default function StorefrontFooter() {
  return (
    <footer className="border-t border-border bg-card">
      <div className="mx-auto grid max-w-7xl gap-8 px-6 py-10 sm:grid-cols-2 md:grid-cols-4">
        <div>
          <div className="flex items-center gap-2 font-bold text-text-primary">
            <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-brand-600 text-white">
              <Package size={16} />
            </span>
            ElectroHub
          </div>
          <p className="mt-2 text-sm text-text-secondary">
            Modern electronics store &amp; inventory platform.
          </p>
        </div>

        {COLUMNS.map((col) => (
          <div key={col.title}>
            <h4 className="text-sm font-semibold text-text-primary">{col.title}</h4>
            <ul className="mt-3 space-y-2 text-sm text-text-secondary">
              {col.links.map((link) => (
                <li key={link}>{link}</li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </footer>
  );
}
