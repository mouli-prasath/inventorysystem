// Maps backend status/enum values to a visual tone. Extend this map as new
// statuses appear rather than hardcoding colors at each call site.
const STATUS_TONE = {
  // Stock status (derived client-side from quantity vs reorder point)
  IN_STOCK: 'success',
  LOW_STOCK: 'warning',
  OUT_OF_STOCK: 'danger',

  // TransactionStatus
  COMPLETED: 'success',
  PENDING: 'warning',
  CANCELLED: 'danger',
  REFUNDED: 'danger',
  PARTIALLY_REFUNDED: 'warning',

  // PurchaseOrderStatus
  APPROVED: 'success',
  ORDERED: 'info',
  RECEIVED: 'success',
  PARTIALLY_RECEIVED: 'warning',

  // Generic
  ACTIVE: 'success',
  INACTIVE: 'danger',
};

const TONE_CLASSES = {
  success: 'bg-status-success-bg text-status-success',
  warning: 'bg-status-warning-bg text-status-warning',
  danger: 'bg-status-danger-bg text-status-danger',
  info: 'bg-status-info-bg text-status-info',
};

function formatLabel(status) {
  return status
    .toLowerCase()
    .split('_')
    .map((word) => word[0].toUpperCase() + word.slice(1))
    .join(' ');
}

export default function StatusBadge({ status, label }) {
  const tone = STATUS_TONE[status] || 'info';
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ${TONE_CLASSES[tone]}`}
    >
      {label || formatLabel(status)}
    </span>
  );
}
