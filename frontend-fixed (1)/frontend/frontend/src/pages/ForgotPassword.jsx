import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/auth';
import { getErrorMessage } from '../api/client';
import AuthLayout from '../layouts/AuthLayout';
import { Button, Input } from '../components/ui';

export default function ForgotPassword() {
  const [step, setStep] = useState('request'); // 'request' | 'reset'
  const [identifier, setIdentifier] = useState('');
  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleRequest = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const { data } = await authApi.forgotPassword(identifier);
      setMessage(data.message);
      // No email service is wired into this project - the reset token comes
      // back directly in the API response for local testing only. A real
      // deployment must email this instead and remove it from the response.
      if (data.devOnlyResetToken) {
        setToken(data.devOnlyResetToken);
      }
      setStep('reset');
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleReset = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await authApi.resetPassword(token, newPassword);
      navigate('/login', { replace: true });
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout>
      {step === 'request' ? (
        <>
          <h1 className="text-2xl font-bold text-text-primary">Reset your password</h1>
          <p className="mt-1 text-sm text-text-secondary">
            Enter your username, email, or employee ID.
          </p>
          <form onSubmit={handleRequest} className="mt-6 space-y-4">
            <Input
              label="Username, email, or employee ID"
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
              required
              autoFocus
            />
            {error && <p className="text-sm text-status-danger">{error}</p>}
            <Button type="submit" disabled={loading} className="w-full">
              {loading ? 'Sending…' : 'Send reset token'}
            </Button>
          </form>
        </>
      ) : (
        <>
          <h1 className="text-2xl font-bold text-text-primary">Choose a new password</h1>
          <p className="mt-1 text-sm text-text-secondary">{message}</p>
          {token && (
            <p className="mt-2 rounded-lg bg-status-warning-bg p-2 text-xs text-status-warning">
              Dev mode: no email is configured, so your reset token was pre-filled below.
              A real deployment would email this instead.
            </p>
          )}
          <form onSubmit={handleReset} className="mt-4 space-y-4">
            <Input label="Reset token" value={token} onChange={(e) => setToken(e.target.value)} required />
            <Input
              label="New password"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              minLength={8}
            />
            {error && <p className="text-sm text-status-danger">{error}</p>}
            <Button type="submit" disabled={loading} className="w-full">
              {loading ? 'Resetting…' : 'Reset password'}
            </Button>
          </form>
        </>
      )}

      <p className="mt-4 text-center text-sm">
        <Link to="/login" className="text-brand-600 hover:underline">Back to sign in</Link>
      </p>
    </AuthLayout>
  );
}
