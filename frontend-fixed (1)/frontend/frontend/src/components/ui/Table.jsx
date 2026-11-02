export function Table({ children }) {
  return (
    <div className="overflow-x-auto rounded-card border border-border bg-card shadow-sm">
      <table className="w-full text-left text-sm">{children}</table>
    </div>
  );
}

export function TableHead({ children }) {
  return (
    <thead className="border-b border-border bg-surface text-xs uppercase text-text-secondary">
      <tr>{children}</tr>
    </thead>
  );
}

export function TableBody({ children }) {
  return <tbody className="divide-y divide-border">{children}</tbody>;
}

export function Th({ children }) {
  return <th className="px-4 py-3 font-medium">{children}</th>;
}

export function Td({ children, className = '' }) {
  return <td className={`px-4 py-3 text-text-primary ${className}`}>{children}</td>;
}
