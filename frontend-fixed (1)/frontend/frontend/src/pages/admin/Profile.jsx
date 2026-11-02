import { useEffect, useState } from 'react';
import { Camera } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { userApi } from '../../api/auth';
import { getErrorMessage } from '../../api/client';
import { Card, Avatar, Button, Input } from '../../components/ui';

function formatRole(role) {
  if (!role) return '';
  return role.toLowerCase().split('_').map((w) => w[0].toUpperCase() + w.slice(1)).join(' ');
}

export default function Profile() {
  const { user, updateStoredUser } = useAuth();
  const [firstName, setFirstName] = useState(user?.firstName || '');
  const [lastName, setLastName] = useState(user?.lastName || '');
  const [phoneNumber, setPhoneNumber] = useState(user?.phoneNumber || '');
  const [storeLocation, setStoreLocation] = useState(user?.storeLocation || '');
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [changingPassword, setChangingPassword] = useState(false);
  const [passwordMessage, setPasswordMessage] = useState(null);

  // Profile in AuthContext comes from the login/register response and could be
  // stale (e.g. if something else updated this account since). Fetch the real
  // current state on load rather than only trusting the cached copy.
  useEffect(() => {
    userApi.getProfile().then(({ data }) => {
      setFirstName(data.firstName);
      setLastName(data.lastName);
      setPhoneNumber(data.phoneNumber || '');
      setStoreLocation(data.storeLocation || '');
      updateStoredUser({ ...user, ...data });
    }).catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage(null);
    try {
      const { data } = await userApi.updateProfile({ firstName, lastName, phoneNumber, storeLocation });
      updateStoredUser({ ...user, ...data });
      setMessage({ type: 'success', text: 'Profile updated.' });
    } catch (err) {
      setMessage({ type: 'error', text: getErrorMessage(err) });
    } finally {
      setSaving(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setChangingPassword(true);
    setPasswordMessage(null);
    try {
      await userApi.changePassword({ currentPassword, newPassword });
      setPasswordMessage({ type: 'success', text: 'Password changed.' });
      setCurrentPassword('');
      setNewPassword('');
    } catch (err) {
      setPasswordMessage({ type: 'error', text: getErrorMessage(err) });
    } finally {
      setChangingPassword(false);
    }
  };

  return (
    <div className="max-w-2xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-text-primary">Profile</h1>
        <p className="mt-1 text-sm text-text-secondary">Manage your personal information.</p>
      </div>

      <Card className="p-6">
        <div className="flex flex-col items-center border-b border-border pb-6">
          <div className="relative">
            <Avatar firstName={user?.firstName} lastName={user?.lastName} size={72} />
            <button
              type="button"
              className="absolute -bottom-1 -right-1 flex h-7 w-7 items-center justify-center rounded-full border border-border bg-card text-text-secondary"
              title="Photo upload isn't wired to the backend yet"
            >
              <Camera size={14} />
            </button>
          </div>
          <p className="mt-3 font-semibold text-text-primary">{user?.firstName} {user?.lastName}</p>
          <p className="text-sm text-text-secondary">{formatRole(user?.role)}</p>
        </div>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input label="First name" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
            <Input label="Last name" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Input label="Email" value={user?.email || ''} disabled className="opacity-60" />
            <Input label="Role" value={formatRole(user?.role)} disabled className="opacity-60" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Input label="Phone" value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} />
            <Input label="Store location" value={storeLocation} onChange={(e) => setStoreLocation(e.target.value)} />
          </div>

          {message && (
            <p className={`text-sm ${message.type === 'success' ? 'text-status-success' : 'text-status-danger'}`}>
              {message.text}
            </p>
          )}

          <Button type="submit" disabled={saving}>{saving ? 'Saving…' : 'Update Profile'}</Button>
        </form>
      </Card>

      <Card className="p-6">
        <h2 className="font-semibold text-text-primary">Change password</h2>
        <form onSubmit={handleChangePassword} className="mt-4 space-y-4">
          <Input
            label="Current password" type="password"
            value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required
          />
          <Input
            label="New password" type="password" minLength={8}
            value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required
          />
          {passwordMessage && (
            <p className={`text-sm ${passwordMessage.type === 'success' ? 'text-status-success' : 'text-status-danger'}`}>
              {passwordMessage.text}
            </p>
          )}
          <Button type="submit" disabled={changingPassword}>{changingPassword ? 'Changing…' : 'Change Password'}</Button>
        </form>
      </Card>
    </div>
  );
}
