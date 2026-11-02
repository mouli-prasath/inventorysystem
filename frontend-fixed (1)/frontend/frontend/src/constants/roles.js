// Mirrors com.electronicstore.inventory.enums.Role on the backend exactly -
// keep these two in sync if the backend enum ever changes.
export const ROLES = {
  CUSTOMER: 'CUSTOMER',
  CASHIER: 'CASHIER',
  SALES_ASSOCIATE: 'SALES_ASSOCIATE',
  INVENTORY_MANAGER: 'INVENTORY_MANAGER',
  STORE_MANAGER: 'STORE_MANAGER',
  ADMIN: 'ADMIN',
  SUPPLIER: 'SUPPLIER',
};

// Convenience groupings used by route guards, matching Appendix A's permission matrix.
export const STAFF_ROLES = [
  ROLES.CASHIER,
  ROLES.SALES_ASSOCIATE,
  ROLES.INVENTORY_MANAGER,
  ROLES.STORE_MANAGER,
  ROLES.ADMIN,
];

export const INVENTORY_ROLES = [ROLES.INVENTORY_MANAGER, ROLES.STORE_MANAGER, ROLES.ADMIN];
export const MANAGER_ROLES = [ROLES.STORE_MANAGER, ROLES.ADMIN];
export const POS_ROLES = [ROLES.CASHIER, ROLES.SALES_ASSOCIATE, ROLES.STORE_MANAGER, ROLES.ADMIN];
// Matches SecurityConfig's GET /api/reports/sales/** rule - Sales Associate and
// Inventory Manager get view access here too (Appendix A: "View Sales Reports
// (Limited)"), not just Store Manager/Admin.
export const REPORTS_ROLES = [ROLES.SALES_ASSOCIATE, ROLES.INVENTORY_MANAGER, ROLES.STORE_MANAGER, ROLES.ADMIN];