import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import ProtectedRoute from './routes/ProtectedRoute';
import StorefrontLayout from './layouts/StorefrontLayout';
import AdminLayout from './layouts/AdminLayout';

// Public storefront
import Home from './pages/Home';
import ProductCatalog from './pages/ProductCatalog';
import ProductDetail from './pages/ProductDetail';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import About from './pages/About';
import Contact from './pages/Contact';

// Auth
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import Unauthorized from './pages/Unauthorized';
import NotFound from './pages/NotFound';

// Admin / staff dashboard
import Dashboard from './pages/admin/Dashboard';
import ProductManagement from './pages/admin/ProductManagement';
import CategoryManagement from './pages/admin/CategoryManagement';
import Promotions from './pages/admin/Promotions';
import InventoryManagement from './pages/admin/InventoryManagement';
import SalesManagement from './pages/admin/SalesManagement';
import POSBilling from './pages/admin/POSBilling';
import Purchases from './pages/admin/Purchases';
import CustomerManagement from './pages/admin/CustomerManagement';
import Suppliers from './pages/admin/Suppliers';
import Reports from './pages/admin/Reports';
import Users from './pages/admin/Users';
import AuditLog from './pages/admin/AuditLog';
import BarcodeScanner from './pages/admin/BarcodeScanner';
import Notifications from './pages/admin/Notifications';
import Profile from './pages/admin/Profile';
import Settings from './pages/admin/Settings';

import { INVENTORY_ROLES, POS_ROLES, REPORTS_ROLES, ROLES, STAFF_ROLES } from './constants/roles';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <Routes>
            {/* Public auth routes render outside any layout */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />

            {/* Public storefront - top nav + footer, customer-facing */}
            <Route element={<StorefrontLayout />}>
              <Route path="/" element={<Home />} />
              <Route path="/products" element={<ProductCatalog />} />
              <Route path="/products/:id" element={<ProductDetail />} />
              <Route path="/cart" element={<Cart />} />
              <Route path="/checkout" element={<Checkout />} />
              <Route path="/about" element={<About />} />
              <Route path="/contact" element={<Contact />} />
              <Route path="/unauthorized" element={<Unauthorized />} />
            </Route>

            {/* Internal staff/admin dashboard - dark sidebar, authenticated, role-gated
                to mirror the backend's RBAC. The route guard is UX only, same as the
                sidebar hiding links it can't use - the backend enforces the real
                boundary regardless of what this shows. */}
            <Route
              element={
                <ProtectedRoute>
                  <AdminLayout />
                </ProtectedRoute>
              }
            >
              <Route
                path="/dashboard"
                element={<ProtectedRoute allowedRoles={STAFF_ROLES}><Dashboard /></ProtectedRoute>}
              />
              <Route
                path="/admin/products"
                element={<ProtectedRoute allowedRoles={INVENTORY_ROLES}><ProductManagement /></ProtectedRoute>}
              />
              <Route
                path="/admin/categories"
                element={<ProtectedRoute allowedRoles={INVENTORY_ROLES}><CategoryManagement /></ProtectedRoute>}
              />
              <Route
                path="/admin/promotions"
                element={<ProtectedRoute allowedRoles={INVENTORY_ROLES}><Promotions /></ProtectedRoute>}
              />
              <Route
                path="/admin/inventory"
                element={<ProtectedRoute allowedRoles={INVENTORY_ROLES}><InventoryManagement /></ProtectedRoute>}
              />
              <Route
                path="/admin/sales"
                element={<ProtectedRoute allowedRoles={STAFF_ROLES}><SalesManagement /></ProtectedRoute>}
              />
              <Route
                path="/admin/pos"
                element={<ProtectedRoute allowedRoles={POS_ROLES}><POSBilling /></ProtectedRoute>}
              />
              <Route
                path="/admin/purchases"
                element={<ProtectedRoute allowedRoles={INVENTORY_ROLES}><Purchases /></ProtectedRoute>}
              />
              <Route
                path="/admin/customers"
                element={<ProtectedRoute allowedRoles={STAFF_ROLES}><CustomerManagement /></ProtectedRoute>}
              />
              <Route
                path="/admin/suppliers"
                element={<ProtectedRoute allowedRoles={INVENTORY_ROLES}><Suppliers /></ProtectedRoute>}
              />
              <Route
                path="/admin/reports"
                element={<ProtectedRoute allowedRoles={REPORTS_ROLES}><Reports /></ProtectedRoute>}
              />
              <Route
                path="/admin/users"
                element={<ProtectedRoute allowedRoles={[ROLES.ADMIN, ROLES.STORE_MANAGER]}><Users /></ProtectedRoute>}
              />
              <Route
                path="/admin/audit-log"
                element={<ProtectedRoute allowedRoles={[ROLES.ADMIN, ROLES.STORE_MANAGER]}><AuditLog /></ProtectedRoute>}
              />
              <Route
                path="/admin/barcode-scanner"
                element={<ProtectedRoute allowedRoles={STAFF_ROLES}><BarcodeScanner /></ProtectedRoute>}
              />
              <Route path="/admin/notifications" element={<Notifications />} />
              <Route path="/admin/profile" element={<Profile />} />
              <Route
                path="/admin/settings"
                element={<ProtectedRoute allowedRoles={[ROLES.ADMIN]}><Settings /></ProtectedRoute>}
              />
              <Route path="*" element={<NotFound />} />
            </Route>
          </Routes>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}