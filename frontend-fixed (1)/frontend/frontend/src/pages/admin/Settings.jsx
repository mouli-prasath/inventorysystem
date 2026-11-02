import { PageHeader, Card } from '../../components/ui';

// Placeholder - no settings resource exists on the backend yet.
export default function Settings() {
  return (
    <div>
      <PageHeader title="Settings" subtitle="Application preferences." />
      <Card className="p-6 text-sm text-text-secondary">
        Settings aren't backed by an API yet. To change your account details, use the Profile page.
      </Card>
    </div>
  );
}
