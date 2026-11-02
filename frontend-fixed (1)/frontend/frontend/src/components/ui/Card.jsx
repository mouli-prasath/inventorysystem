export default function Card({ children, className = '', ...props }) {
  return (
    <div
      className={`rounded-card bg-card border border-border shadow-sm ${className}`}
      {...props}
    >
      {children}
    </div>
  );
}
