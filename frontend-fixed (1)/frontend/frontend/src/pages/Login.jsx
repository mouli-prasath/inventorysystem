import { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import AuthLayout from '../layouts/AuthLayout';
import { Button, Input } from '../components/ui';

export default function Login() {
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const redirectTo = location.state?.from?.pathname || '/';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const result = await login(identifier, password);
    if (result.success) {
      navigate(redirectTo, { replace: true });
    } else {
      setError(result.message);
    }
  };

  return (
    <AuthLayout>
      <h1 className="text-2xl font-bold text-text-primary">Welcome Back</h1>
      <p className="mt-1 text-sm text-text-secondary">Sign in to continue to your account</p>

      <form onSubmit={handleSubmit} className="mt-6 space-y-4">
        <Input
          id="identifier"
          label="Username, email, or employee ID"
          value={identifier}
          onChange={(e) => setIdentifier(e.target.value)}
          placeholder="you@example.com"
          required
          autoFocus
        />
        <Input
          id="password"
          label="Password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="••••••••"
          required
        />

        {error && (
          <p role="alert" className="rounded-lg bg-status-danger-bg px-3 py-2 text-sm text-status-danger">
            {error}
          </p>
        )}

        <Button type="submit" disabled={loading} className="w-full">
          {loading ? 'Signing in…' : 'Sign in'}
        </Button>
      </form>

      <p className="mt-4 text-center text-sm text-text-secondary">
        Don't have an account? <Link to="/register" className="font-medium text-brand-600 hover:underline">Sign up</Link>
      </p>
      <p className="mt-2 text-center text-sm">
        <Link to="/forgot-password" className="text-brand-600 hover:underline">Forgot password?</Link>
      </p>
    </AuthLayout>
  );
}
