export default function StatCard({ label, value, icon: Icon, trend, tone = 'info' }) {
  const iconToneClasses = {
    info: 'bg-brand-100 text-brand-600',
    success: 'bg-status-success-bg text-status-success',
    warning: 'bg-status-warning-bg text-status-warning',
    danger: 'bg-status-danger-bg text-status-danger',
  };

  return (
    <div className="rounded-card bg-card border border-border p-4 shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-text-secondary">{label}</p>
          <p className="mt-1 text-2xl font-bold text-text-primary">{value}</p>
        </div>
        {Icon && (
          <span className={`flex h-9 w-9 items-center justify-center rounded-full ${iconToneClasses[tone]}`}>
            <Icon size={18} />
          </span>
        )}
      </div>
      {trend && (
        <p className={`mt-2 text-xs ${trend.positive ? 'text-status-success' : 'text-status-danger'}`}>
          {trend.text}
        </p>
      )}
    </div>
  );
}
