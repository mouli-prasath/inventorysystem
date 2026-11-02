# Electronic Store Inventory Management System — Backend

Spring Boot 3.4 / Java 25 / MySQL / JWT backend, built from the SRS. This first slice covers:

- **FR1 & FR2** — Registration + JWT authentication (login with username, email, *or* employee ID)
- **FR3** — Role-based access control (7 roles, enforced in `SecurityConfig`)
- **FR4** — Product & Category catalog management, with search/filter/pagination
- **FR5** — Real-time inventory tracking per store location, stock adjustments, and a `StockMovement` audit trail
- **FR6** — POS transaction processing: sale → stock deduction → tax/discount/total calc, void, and returns/refunds (with automatic restock)
- **FR7** — Customer profiles, lookup/search, and loyalty points (1 point per $1 spent, per Appendix F)
- **FR8/FR9** — Supplier profile management, purchase orders (manual + auto-generated from low-stock alerts), receiving deliveries (partial or full) with automatic stock updates
- **FR12** — Notifications: in-app alerts auto-triggered by low/out-of-stock events, PO status changes, and returns; plus a manual/broadcast send endpoint for admins & store managers
- **FR10/FR11** — Reporting & analytics: sales summaries (period or daily), top-selling products, inventory valuation (cost vs. retail, by category), and per-product profit margins
- **Appendix B** — General `AuditLogs` trail: logins (success/failure), registrations, price changes (flagged if >20%, per Appendix F), and PO approvals
- **Scope: pricing/promotions** — store-wide, category-wide, or product-specific promotions with automatic best-discount pricing applied at sale time
- **FR17** — Centralized exception handling with clean, non-leaky error responses
- **FR14** — Self-service profile (`/api/users/profile`), admin user management (list/view/deactivate/reactivate/change role), and a lightweight system-health endpoint

## Prerequisites
- Java 25 (JDK)
- Maven 3.9+
- MySQL 8+ running locally (or update `application.yml`)

## Setup

1. Create the database (or let it auto-create):
   ```sql
   CREATE DATABASE electronic_store_db;
   ```
2. Set credentials via environment variables (recommended) or edit `application.yml` directly:
   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=yourpassword
   export JWT_SECRET=$(openssl rand -hex 32)
   ```
3. Run it:
   ```bash
   mvn spring-boot:run
   ```
   The API comes up on `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Try it

```bash
# Register a store admin (staff roles require employeeId)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username":"admin1","email":"admin1@store.com","password":"Admin@1234",
    "firstName":"Store","lastName":"Admin","role":"ADMIN","employeeId":"EMP001",
    "storeLocation":"Downtown"
  }'

# Login (accepts username, email, or employeeId as "identifier")
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"admin1","password":"Admin@1234"}'

# Create a category (use the accessToken from login/register)
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"categoryName":"Laptops"}'

# Create a product
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "sku":"LAP00123","name":"UltraBook 14","categoryId":1,"brand":"Acme",
    "price":999.99,"costPrice":650.00,"stockQuantity":50,"reorderPoint":10,
    "imageUrl":"https://cdn.example.com/products/lap00123.jpg"
  }'

# Search/filter products
curl "http://localhost:8080/api/products?keyword=ultrabook&minPrice=500&maxPrice=1500"

# Adjust stock for a store location
curl -X POST http://localhost:8080/api/inventory/adjustment \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"storeLocation":"Downtown","movementType":"STOCK_IN","quantity":25,"reference":"PO-1001"}'

# Create a customer
curl -X POST http://localhost:8080/api/customers \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jane@example.com","phone":"5551234567"}'

# Process a sale (deducts stock, calculates tax/discount/total, awards loyalty points)
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId":1,"storeLocation":"Downtown","paymentMethod":"CREDIT_CARD","taxRate":0.08,
    "items":[{"productId":1,"quantity":2}]
  }'

# Process a return against that transaction (restocks + refunds)
curl -X POST http://localhost:8080/api/transactions/1/return \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":1,"reason":"Customer changed mind"}'

# Create a supplier
curl -X POST http://localhost:8080/api/suppliers \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"supplierName":"Acme Distributors","email":"orders@acme.com","phone":"5559876543"}'

# Manually create a purchase order
curl -X POST http://localhost:8080/api/purchase-orders \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "supplierId":1,"storeLocation":"Downtown",
    "items":[{"productId":1,"quantityOrdered":50,"unitCost":650.00}]
  }'

# Auto-generate POs for every product at/below its reorder point, grouped by supplier
curl -X POST "http://localhost:8080/api/purchase-orders/auto-generate?storeLocation=Downtown" \
  -H "Authorization: Bearer <accessToken>"

# Approve, then mark ordered
curl -X PUT http://localhost:8080/api/purchase-orders/1/approve -H "Authorization: Bearer <accessToken>"
curl -X PUT http://localhost:8080/api/purchase-orders/1/mark-ordered -H "Authorization: Bearer <accessToken>"

# Receive a delivery (partial or full) - updates inventory automatically
curl -X PUT http://localhost:8080/api/purchase-orders/1/receive \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"receivedItems":[{"purchaseOrderItemId":1,"quantityReceived":50}]}'

# Get my notifications (unread only)
curl "http://localhost:8080/api/notifications?unreadOnly=true" -H "Authorization: Bearer <accessToken>"

# Mark one as read / mark all as read
curl -X PUT http://localhost:8080/api/notifications/1/read -H "Authorization: Bearer <accessToken>"
curl -X PUT http://localhost:8080/api/notifications/read-all -H "Authorization: Bearer <accessToken>"

# Broadcast a manual notification (store manager/admin only)
curl -X POST http://localhost:8080/api/admin/notifications \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"title":"System maintenance","message":"Downtime tonight 11pm-1am","type":"SYSTEM_ALERT","roles":["ADMIN","STORE_MANAGER"]}'

# Daily sales summary
curl "http://localhost:8080/api/reports/sales/daily?date=2026-07-16" -H "Authorization: Bearer <accessToken>"

# Sales summary for an arbitrary period
curl "http://localhost:8080/api/reports/sales/summary?start=2026-07-01T00:00:00&end=2026-07-16T23:59:59" \
  -H "Authorization: Bearer <accessToken>"

# Top 10 selling products this month
curl "http://localhost:8080/api/reports/sales/top-products?start=2026-07-01T00:00:00&end=2026-07-16T23:59:59&limit=10" \
  -H "Authorization: Bearer <accessToken>"

# Inventory valuation (cost vs retail, by category)
curl http://localhost:8080/api/reports/inventory/valuation -H "Authorization: Bearer <accessToken>"

# Weakest profit margins (candidates for a pricing review)
curl "http://localhost:8080/api/reports/inventory/margins?limit=10&lowestFirst=true" \
  -H "Authorization: Bearer <accessToken>"

# Create a store-wide promotion
curl -X POST http://localhost:8080/api/promotions \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "promotionName":"Summer Sale","discountType":"PERCENTAGE","discountValue":15,
    "startDate":"2026-07-01T00:00:00","endDate":"2026-08-31T23:59:59"
  }'

# See what's currently running
curl http://localhost:8080/api/promotions/active

# View the audit trail (store manager/admin only)
curl "http://localhost:8080/api/admin/audit-logs?sort=timestamp,desc" -H "Authorization: Bearer <accessToken>"
curl http://localhost:8080/api/admin/audit-logs/action/PRICE_CHANGE_MAJOR -H "Authorization: Bearer <accessToken>"

# View / update my own profile
curl http://localhost:8080/api/users/profile -H "Authorization: Bearer <accessToken>"
curl -X PUT http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" \
  -d '{"firstName":"Store","lastName":"Admin","phoneNumber":"5551234567","storeLocation":"Downtown"}'

# Admin: list/deactivate users, change a role
curl http://localhost:8080/api/admin/users -H "Authorization: Bearer <accessToken>"
curl -X PUT http://localhost:8080/api/admin/users/3/deactivate -H "Authorization: Bearer <accessToken>"
curl -X PUT http://localhost:8080/api/admin/users/3/role \
  -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" -d '{"role":"STORE_MANAGER"}'

# Manual loyalty point adjustment (redemption, goodwill credit)
curl -X POST http://localhost:8080/api/customers/1/loyalty \
  -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" \
  -d '{"pointsDelta":-100,"reason":"Redeemed for $1 discount"}'

# Update a product's reorder point without a full product edit
curl -X PUT http://localhost:8080/api/inventory/reorder \
  -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" \
  -d '{"productId":1,"reorderPoint":15}'

# System health
curl http://localhost:8080/api/admin/system-health -H "Authorization: Bearer <accessToken>"

# Physical cycle count (absolute recount, not a delta - system computes the correction)
curl -X POST http://localhost:8080/api/inventory/count \
  -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" \
  -d '{"productId":1,"storeLocation":"Downtown","countedQuantity":47,"notes":"Monthly cycle count"}'

# Appendix H path aliases
curl http://localhost:8080/api/inventory/stock -H "Authorization: Bearer <accessToken>"
curl http://localhost:8080/api/inventory/low-stock -H "Authorization: Bearer <accessToken>"
curl http://localhost:8080/api/sales/daily -H "Authorization: Bearer <accessToken>"
curl http://localhost:8080/api/customers/1/history -H "Authorization: Bearer <accessToken>"
curl http://localhost:8080/api/admin/analytics -H "Authorization: Bearer <accessToken>"
curl http://localhost:8080/api/admin/reports -H "Authorization: Bearer <accessToken>"
```

## Status: backend complete against the SRS, verified path-by-path against Appendix H
Every functional requirement in the SRS (FR1-FR12, FR14, FR17) has a working slice, and every
endpoint literally named in Appendix H's API summary now exists at its specified path - including
a second pass that caught endpoints which existed under a *different* path than the spec named
(`/inventory/stock`, `/inventory/low-stock`, `/sales/daily`, `/customers/{id}/history`,
`/admin/analytics`, `/admin/reports`) plus one that was missing outright (`/inventory/count`,
which needed real logic: cycle counts are an absolute recount, not a delta, so it computes and
records the correction rather than reusing the delta-based `/inventory/adjustment` endpoint).

Two categories of SRS content are intentionally *not* built, and shouldn't be treated as gaps:
- **FR13-FR16, FR18-FR19** (workflow/BPM engine, generic system-integration framework beyond
  what POS/payment/accounting hooks would need, mobile app, and the AI/ML features) describe
  either infrastructure that has no concrete behavior to implement without a specific
  integration target, or Phase 2/3 roadmap items per Appendix G (demand forecasting, IoT
  replenishment, AR, blockchain). Building these now would mean guessing at requirements no one
  has specified yet.
- **Appendix D's finer security details** (token blacklist/denylist, concurrent session caps
  per role, SSO/HR-directory integration) are real gaps, not roadmap items - flag it if you want
  the JWT layer hardened to match Appendix D exactly.

Natural next step: the React frontend. Or, if you'd rather harden the backend further first,
good candidates are the Appendix D session/blacklist items above, or a proper DB-sequence-backed
transaction/PO numbering scheme instead of the in-process counters currently used.

## Notes on design decisions
- **Inventory model**: `Product.stockQuantity` holds an aggregate total; `Inventory` rows hold the authoritative per-location breakdown. Every adjustment updates both and writes a `StockMovement` record.
- **JWT**: stateless, HS256, with role embedded in the token. Token blacklisting/session limits from Appendix D (concurrent session caps, blacklist-on-logout) aren't wired up yet — flag it if you want that added, it needs a small persisted denylist.
- **Validation**: field-level rules from Appendix C (SKU format, barcode length, password complexity, etc.) are enforced via Bean Validation annotations on the request DTOs.
- **Notifications**: currently in-app only (persisted rows the client polls). Email/SMS/push fan-out (Appendix E multi-channel delivery) can be added as a listener on notification creation without changing `NotificationService`'s public surface.
- **Reporting**: aggregate queries run directly against the transactional tables (no separate reporting/warehouse tables). Fine at MVP scale; if reports get slow at high transaction volume, the next step is materialized summary tables refreshed on a schedule rather than changing the API surface.
- **Audit logging**: `AuditService.log(...)` runs in its own transaction (`REQUIRES_NEW`) specifically so a failed-login audit entry survives even though the login attempt itself throws and rolls back. Hooked in at registration, login (success/failure), product price changes (flagged `PRICE_CHANGE_MAJOR` above the 20% threshold from Appendix F), and PO approval - extend the same pattern (`auditService.logCurrentUser(...)`) to any other sensitive action you want tracked.
- **Promotions**: a product can be discounted by a product-specific, category-wide, or store-wide promotion; if more than one applies, `PromotionService.getEffectivePrice` picks whichever gives the deepest discount. `TransactionService` uses this automatically at sale time, so POS pricing always reflects active promotions without the client needing to compute it.
