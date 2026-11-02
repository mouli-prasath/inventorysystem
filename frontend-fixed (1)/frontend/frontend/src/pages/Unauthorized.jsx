import { Link } from 'react-router-dom';
import { ShieldAlert } from 'lucide-react';
import { Button } from '../components/ui';

export default function Unauthorized() {
  return (
    <div className="flex min-h-[70vh] flex-col items-center justify-center px-6 text-center">
      <ShieldAlert size={48} className="text-status-danger" />
      <h1 className="mt-4 text-2xl font-bold text-text-primary">Access denied</h1>
      <p className="mt-2 text-sm text-text-secondary">
        Your account role doesn't have permission to view this page.
      </p>
      <Link to="/"><Button className="mt-6">Back to home</Button></Link>
    </div>
  );
}
