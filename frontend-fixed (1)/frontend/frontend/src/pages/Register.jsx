import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import AuthLayout from '../layouts/AuthLayout';
import { Button, Input } from '../components/ui';

function splitName(fullName) {
  const trimmed = fullName.trim();
  const spaceIndex = trimmed.indexOf(' ');
  if (spaceIndex === -1) return { firstName: trimmed, lastName: '-' };
  return {
    firstName: trimmed.slice(0, spaceIndex),
    lastName: trimmed.slice(spaceIndex + 1),
  };
}

export default function Register() {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { register, loading } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const { firstName, lastName } = splitName(fullName);

    // Public storefront signup is always a CUSTOMER account - staff accounts
    // (Cashier, Inventory Manager, etc.) are created by an Admin, not self-registered.
    const result = await register({
      username: email.split('@')[0],
      email,
      password,
      firstName,
      lastName,
      role: 'CUSTOMER',
    });

    if (result.success) {
      navigate('/', { replace: true });
    } else {
      setError(result.message);
    }
  };

  return (
    <AuthLayout>
      <h1 className="text-2xl font-bold text-text-primary">Create your account</h1>
      <p className="mt-1 text-sm text-text-secondary">Sign up to start shopping</p>

      <form onSubmit={handleSubmit} className="mt-6 space-y-4">
        <Input
          id="fullName"
          label="Full name"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          placeholder="Jane Doe"
          required
          autoFocus
        />
        <Input
          id="email"
          label="Email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@example.com"
          required
        />
        <Input
          id="password"
          label="Password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="••••••••"
          required
          minLength={8}
        />

        {error && (
          <p role="alert" className="rounded-lg bg-status-danger-bg px-3 py-2 text-sm text-status-danger">
            {error}
          </p>
        )}

        <Button type="submit" disabled={loading} className="w-full">
          {loading ? 'Creating account…' : 'Create account'}
        </Button>
      </form>

      <p className="mt-4 text-center text-sm text-text-secondary">
        Already have an account? <Link to="/login" className="font-medium text-brand-600 hover:underline">Sign in</Link>
      </p>
    </AuthLayout>
  );
}
