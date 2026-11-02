# Electronic Store Inventory Management System — Frontend

React + Vite frontend built against the tested Spring Boot backend and the project's
wireframes. Two experiences in one app:

- **Public storefront** — Home, Shop, Product Detail, Cart, Checkout (demo/showcase only —
  see note below), Login, Register, Forgot/Reset Password, About, Contact.
- **Admin/staff dashboard** — dark sidebar matching the wireframes: Dashboard, Product
  Management, Category Management, Promotions, Inventory Management, Sales, POS Billing,
  Purchases, Customers, Suppliers, Reports & Analytics, Users, Audit Log, Notifications,
  Profile, Settings, Barcode Scanner.

## Setup

```bash
npm install
```

Copy `.env.example` to `.env` if you need to point at a different backend:
```
VITE_API_BASE_URL=http://localhost:8080/api
```

Run it:
```bash
npm run dev
```

**Important — port is fixed at 8081.** The backend's CORS config (`application.yml`,
`app.cors.allowed-origins`) only allows requests from `http://localhost:8081`, matching the
SRS's Operating Environment spec. `vite.config.js` pins the dev server to that port. If you
change one, change the other — otherwise every API call fails with a generic network error,
not an obvious CORS message.

**Backend must be running first** (`mvn spring-boot:run`).

## API coverage
74 of 84 backend-connected API functions are wired into real UI, verified by an automated
audit (grep every `apiObject.method` call site against every defined function). The remaining
10 are deliberately unused, not gaps:
- `adminApi.analytics` / `adminApi.reports` — Appendix H aliases that duplicate data already
  shown via the purpose-built Dashboard and Reports pages.
- `authApi.refresh` — the token-refresh interceptor calls the endpoint directly via raw axios
  to avoid a circular import; this named export is an unused leftover, functionality works.
- `adminUserApi.getById`, `categoryApi.getById`, `promotionApi.getById`, `supplierApi.getById` —
  each object's `.list()`/`.getAll()` sibling already returns full objects with everything
  needed for display and editing; a separate single-item fetch would just re-fetch data
  already in hand.
- `categoryApi.topLevel`, `promotionApi.active` — subsets of what `.list()` already returns.
- `transactionApi.getByCustomer` — same underlying data as `customerApi.getHistory`, which is
  already wired into the customer detail view.

## Known architectural gap: checkout is demo-only
The backend models sales as **staff-operated POS transactions** — `POST /api/transactions` is
role-restricted to Cashier/Sales Associate/Store Manager/Admin. There's no endpoint for a
`CUSTOMER`-role user to submit their own order. Per project decision, the storefront's
Cart → Checkout → Place Order flow is a fully-built, real-looking UI that does **not** submit
to a real backend endpoint — "placing an order" just clears the client-side cart. Real sales
happen through the admin dashboard's POS Billing page, which does hit the real endpoint.

## Other known gaps, stated plainly
- **Profile photo upload** — UI present, not wired to anything (no file upload endpoint exists).
- **Bulk product import** — button present, disabled (no backend endpoint).
- **Barcode scanning** — manual code entry that calls a real product search; no camera integration.
- **Settings page** — placeholder; no settings resource exists on the backend.
- **"My Orders" / customer order history** — not built. A self-registered `CUSTOMER`-role
  `User` has no corresponding `Customer` record on the backend (they're separate entities,
  only linked when a `Customer` is created explicitly via `POST /customers` or auto-created
  during a POS sale over $100). There's no real data to show a logged-in customer their own
  order history without a backend change linking these two concepts.

## Backend dependency: one endpoint had to be added
The Purchases list page needs `GET /api/purchase-orders`, which didn't exist on the backend
when this frontend was built (only single-PO lookup, create, approve, receive, cancel existed).
If your backend copy doesn't have this yet: add `PurchaseOrderService.getAll(Pageable)` (one
line, `JpaRepository` gives `findAll(Pageable)` for free) and a matching `GET` mapping on
`PurchaseOrderController` — see the project conversation for the exact code.

## Structure
- `src/api/` — one module per backend controller group.
- `src/context/` — `AuthContext` (login state, role checks), `CartContext` (client-side cart).
- `src/layouts/` — `StorefrontLayout` (public nav+footer), `AdminLayout` (dark sidebar+topbar).
- `src/components/ui/` — shared design system primitives matching the wireframes' palette.
- `src/components/admin/` — modal forms and detail views used across admin pages.
- `src/pages/` — storefront pages at the top level, admin pages under `pages/admin/`.

## What to test
This is 70+ files, all built without ever running in a real browser during development —
compiling cleanly is a floor on confidence, not a ceiling. Priority order: auth flow
(register/login/logout/lockout/password reset), product browsing → cart → checkout (demo),
then the admin dashboard's real workflows — POS Billing sale (including the discount-approval
business rule), Inventory adjustment/cycle count, Purchase Order create→approve→receive,
Category/Promotion CRUD, Customer edit/loyalty/history, and Users role-change/deactivate/unlock.
