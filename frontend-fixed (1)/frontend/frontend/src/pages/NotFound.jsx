import { Link } from 'react-router-dom';
import { PackageX } from 'lucide-react';
import { Button } from '../components/ui';

export default function NotFound() {
  return (
    <div className="flex min-h-[70vh] flex-col items-center justify-center px-6 text-center">
      <PackageX size={48} className="text-text-muted" />
      <h1 className="mt-4 text-2xl font-bold text-text-primary">Page not found</h1>
      <p className="mt-2 text-sm text-text-secondary">The page you're looking for doesn't exist or has moved.</p>
      <Link to="/"><Button className="mt-6">Back to home</Button></Link>
    </div>
  );
}
