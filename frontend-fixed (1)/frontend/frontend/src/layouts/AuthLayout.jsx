import { Package } from 'lucide-react';

export default function AuthLayout({ children }) {
  return (
    <div className="flex min-h-screen">
      <div className="hidden w-1/2 flex-col justify-between bg-gradient-to-br from-navy-900 to-navy-800 p-12 text-white lg:flex">
        <div className="flex items-center gap-2 text-lg font-bold">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600">
            <Package size={18} />
          </span>
          ElectroHub
        </div>
        <div>
          <h2 className="text-3xl font-bold leading-tight">
            Manage smarter.
            <br />
            Sell faster.
          </h2>
          <p className="mt-3 max-w-sm text-sm text-white/70">
            Sign in to your workspace to manage products, inventory, sales, and customers — or
            shop the latest electronics.
          </p>
        </div>
        <div />
      </div>

      <div className="flex w-full items-center justify-center bg-surface p-6 lg:w-1/2">
        <div className="w-full max-w-sm">{children}</div>
      </div>
    </div>
  );
}
