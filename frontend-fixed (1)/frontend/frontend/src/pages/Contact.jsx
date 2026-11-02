import { Mail, Phone, MapPin } from 'lucide-react';

export default function Contact() {
  return (
    <div className="mx-auto max-w-2xl px-6 py-16">
      <h1 className="text-3xl font-bold text-text-primary">Contact us</h1>
      <p className="mt-4 text-text-secondary">
        Have a question about an order or your account? Reach out — we're happy to help.
      </p>

      <div className="mt-8 space-y-4 text-sm text-text-secondary">
        <div className="flex items-center gap-3">
          <Mail size={18} className="text-brand-600" />
          support@electrohub.example
        </div>
        <div className="flex items-center gap-3">
          <Phone size={18} className="text-brand-600" />
          +1 (555) 010-2020
        </div>
        <div className="flex items-center gap-3">
          <MapPin size={18} className="text-brand-600" />
          Downtown store — see our Shop page for stock at your nearest location
        </div>
      </div>
    </div>
  );
}
