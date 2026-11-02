package com.electronicstore.inventory.config;

import com.electronicstore.inventory.entity.*;
import com.electronicstore.inventory.enums.*;
import com.electronicstore.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds realistic demo data (users for every role, suppliers, categories,
 * products, customers, sales history, and purchase orders) on first startup,
 * so the app is immediately usable end-to-end instead of an empty shell.
 *
 * Idempotent: only runs if the users table is empty, so it never duplicates
 * data on subsequent restarts. Disable entirely with app.seed-demo-data=false.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String STORE = "Downtown";

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Demo data already present - skipping seed.");
            return;
        }
        log.info("Seeding demo data...");

        // ---- Users (one per role, matching the app's own role-testing conventions) ----
        User admin = user("admin1", "admin1@electrohub.test", "Admin123!", Role.ADMIN, "Ada", "Min", "EMP-0001");
        User storeManager = user("storemgr1", "storemgr1@electrohub.test", "StoreMgr1!", Role.STORE_MANAGER, "Steve", "Manager", "EMP-1004");
        User invManager = user("invmgr1", "invmgr1@electrohub.test", "InvMgr1!", Role.INVENTORY_MANAGER, "Ivy", "Manager", "EMP-1003");
        User salesAssoc = user("sales1", "sales1@electrohub.test", "Sales1!", Role.SALES_ASSOCIATE, "Sam", "Sales", "EMP-1002");
        User cashier = user("cashier1", "cashier1@electrohub.test", "Cashier1!", Role.CASHIER, "Casey", "Cash", "EMP-1001");
        user("cust1", "cust1@test.com", "Customer1!", Role.CUSTOMER, "Cara", "Buyer", null);

        // ---- Categories ----
        Category laptops = categoryRepository.save(Category.builder().categoryName("Laptops").description("Notebooks and ultrabooks").isActive(true).displayOrder(1).build());
        Category accessories = categoryRepository.save(Category.builder().categoryName("Accessories").description("Peripherals and add-ons").isActive(true).displayOrder(2).build());
        Category audio = categoryRepository.save(Category.builder().categoryName("Audio").description("Headphones and speakers").isActive(true).displayOrder(3).build());
        Category mobile = categoryRepository.save(Category.builder().categoryName("Mobile").description("Phones and tablets").isActive(true).displayOrder(4).build());

        // ---- Suppliers ----
        Supplier techSource = supplierRepository.save(Supplier.builder()
                .supplierName("TechSource Distributors").contactPerson("Raj Kumar").email("raj@techsource.com")
                .phone("9876543210").address("12 Industrial Ave, Coimbatore").paymentTerms("Net 30")
                .isActive(true).createdDate(LocalDateTime.now().minusMonths(8)).build());
        Supplier globalElec = supplierRepository.save(Supplier.builder()
                .supplierName("Global Electronics Supply").contactPerson("Meera Nair").email("meera@globalelec.com")
                .phone("9123456780").address("45 Export Road, Chennai").paymentTerms("Net 45")
                .isActive(true).createdDate(LocalDateTime.now().minusMonths(6)).build());
        Supplier soundWave = supplierRepository.save(Supplier.builder()
                .supplierName("SoundWave Audio Co.").contactPerson("Arjun Iyer").email("arjun@soundwave.com")
                .phone("9988776655").address("8 Harbor St, Vizag").paymentTerms("Net 15")
                .isActive(true).createdDate(LocalDateTime.now().minusMonths(4)).build());

        // ---- Products (deliberately spanning in-stock / low-stock / out-of-stock) ----
        Product p1 = product("LAP00123", "UltraBook 14", "14\" ultrabook, 16GB RAM, 512GB SSD", laptops, "NovaTech", "UB14-2026",
                new BigDecimal("899.00"), new BigDecimal("650.00"), 18, 10, techSource, "1234567890123");
        Product p2 = product("LAP00456", "ProBook 15 Business", "15.6\" business laptop, 8GB RAM, 256GB SSD", laptops, "NovaTech", "PB15-2025",
                new BigDecimal("649.00"), new BigDecimal("470.00"), 4, 10, techSource, "1234567890124");
        Product p3 = product("ELEC001", "Wireless Mouse", "Ergonomic wireless mouse with USB receiver", accessories, "Logitech", "M185",
                new BigDecimal("25.99"), new BigDecimal("14.00"), 120, 20, globalElec, "1234567890125");
        Product p4 = product("ELEC002", "Mechanical Keyboard", "RGB backlit mechanical keyboard, blue switches", accessories, "Logitech", "K840",
                new BigDecimal("79.99"), new BigDecimal("48.00"), 35, 15, globalElec, "1234567890126");
        Product p5 = product("ELEC003", "USB-C Hub 7-in-1", "7-port USB-C hub with HDMI and PD", accessories, "Anker", "H7C",
                new BigDecimal("39.99"), new BigDecimal("21.00"), 0, 15, globalElec, "1234567890127");
        Product p6 = product("AUD00789", "Noise Cancelling Headphones", "Over-ear ANC headphones, 30hr battery", audio, "SoundCore", "Q45",
                new BigDecimal("199.00"), new BigDecimal("120.00"), 22, 12, soundWave, "1234567890128");
        Product p7 = product("AUD00790", "Bluetooth Speaker Mini", "Portable waterproof Bluetooth speaker", audio, "SoundCore", "S12",
                new BigDecimal("49.99"), new BigDecimal("28.00"), 8, 10, soundWave, "1234567890129");
        Product p8 = product("MOB00321", "SmartPhone X12", "6.5\" AMOLED, 128GB, 5G", mobile, "Nexora", "X12-2026",
                new BigDecimal("599.00"), new BigDecimal("430.00"), 15, 8, techSource, "1234567890130");
        Product p9 = product("MOB00322", "Tablet Air 10", "10.5\" tablet, 64GB, WiFi", mobile, "Nexora", "TA10",
                new BigDecimal("329.00"), new BigDecimal("230.00"), 3, 6, techSource, "1234567890131");
        Product p10 = product("ELEC004", "27\" 4K Monitor", "27-inch 4K UHD IPS monitor", accessories, "ViewSonic", "VX2780",
                new BigDecimal("329.00"), new BigDecimal("240.00"), 9, 8, globalElec, "1234567890132");

        for (Product p : List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10)) {
            inventoryRepository.save(Inventory.builder()
                    .product(p).storeLocation(STORE)
                    .quantityOnHand(p.getStockQuantity()).quantityReserved(0)
                    .lastUpdated(LocalDateTime.now()).build());
        }

        // ---- Customers ----
        Customer c1 = customerRepository.save(customer("Amit", "Sharma", "amit.sharma@example.com", "9000000001", 320, new BigDecimal("1240.50")));
        Customer c2 = customerRepository.save(customer("Priya", "Verma", "priya.verma@example.com", "9000000002", 85, new BigDecimal("340.00")));
        Customer c3 = customerRepository.save(customer("Rohit", "Singh", "rohit.singh@example.com", "9000000003", 610, new BigDecimal("2890.75")));
        customerRepository.save(customer("Ananya", "Gupta", "ananya.gupta@example.com", "9000000004", 45, new BigDecimal("120.00")));
        customerRepository.save(customer("Karthik", "Reddy", "karthik.reddy@example.com", "9000000005", 200, new BigDecimal("890.25")));

        // ---- Sales history (last 14 days, for dashboard charts / reports) ----
        seedSale("TXN-DEMO-0001", c1, cashier, 13, List.of(item(p3, 2, p3.getPrice()), item(p4, 1, p4.getPrice())), PaymentMethod.CREDIT_CARD, null);
        seedSale("TXN-DEMO-0002", c2, salesAssoc, 12, List.of(item(p6, 1, p6.getPrice())), PaymentMethod.CASH, null);
        seedSale("TXN-DEMO-0003", null, cashier, 11, List.of(item(p3, 1, p3.getPrice()), item(p7, 1, p7.getPrice())), PaymentMethod.CASH, null);
        seedSale("TXN-DEMO-0004", c3, salesAssoc, 9, List.of(item(p1, 1, p1.getPrice())), PaymentMethod.CREDIT_CARD, null);
        seedSale("TXN-DEMO-0005", c1, cashier, 7, List.of(item(p8, 1, p8.getPrice()), item(p3, 3, p3.getPrice())), PaymentMethod.DEBIT_CARD, null);
        seedSale("TXN-DEMO-0006", null, cashier, 5, List.of(item(p4, 2, p4.getPrice())), PaymentMethod.MOBILE_PAYMENT, null);
        seedSale("TXN-DEMO-0007", c2, salesAssoc, 4, List.of(item(p10, 1, p10.getPrice())), PaymentMethod.CREDIT_CARD, null);
        seedSale("TXN-DEMO-0008", c3, cashier, 2, List.of(item(p6, 1, p6.getPrice()), item(p9, 1, p9.getPrice())), PaymentMethod.CASH, null);
        seedSale("TXN-DEMO-0009", c1, salesAssoc, 1, List.of(item(p3, 1, p3.getPrice())), PaymentMethod.CASH, null);
        seedSale("TXN-DEMO-0010", null, cashier, 0, List.of(item(p7, 2, p7.getPrice())), PaymentMethod.MOBILE_PAYMENT, null);

        // ---- Purchase orders (spanning statuses, to demo the restock workflow) ----
        purchaseOrder("PO-DEMO-0001", techSource, storeManager, storeManager, PurchaseOrderStatus.RECEIVED,
                List.of(poItem(p2, 20, new BigDecimal("470.00"), 20)), 20);
        purchaseOrder("PO-DEMO-0002", globalElec, invManager, null, PurchaseOrderStatus.APPROVED,
                List.of(poItem(p5, 30, new BigDecimal("21.00"), 0)), 5);
        purchaseOrder("PO-DEMO-0003", soundWave, invManager, null, PurchaseOrderStatus.PENDING,
                List.of(poItem(p7, 25, new BigDecimal("28.00"), 0), poItem(p6, 15, new BigDecimal("120.00"), 0)), 2);

        log.info("Demo data seeded: 6 users, 4 categories, 3 suppliers, 10 products, 5 customers, 10 sales, 3 purchase orders.");
    }

    // ---- helpers ----

    private User user(String username, String email, String rawPassword, Role role, String first, String last, String employeeId) {
        return userRepository.save(User.builder()
                .username(username).email(email).password(passwordEncoder.encode(rawPassword))
                .role(role).firstName(first).lastName(last).employeeId(employeeId)
                .storeLocation(STORE).createdDate(LocalDateTime.now()).isActive(true).build());
    }

    private Product product(String sku, String name, String description, Category category, String brand, String model,
                            BigDecimal price, BigDecimal costPrice, int stock, int reorderPoint, Supplier supplier, String barcode) {
        return productRepository.save(Product.builder()
                .sku(sku).name(name).description(description).category(category).brand(brand).model(model)
                .price(price).costPrice(costPrice).stockQuantity(stock).reorderPoint(reorderPoint)
                .supplier(supplier).barcode(barcode).isActive(true).createdDate(LocalDateTime.now()).build());
    }

    private Customer customer(String first, String last, String email, String phone, int loyaltyPoints, BigDecimal totalPurchases) {
        return Customer.builder()
                .firstName(first).lastName(last).email(email).phone(phone)
                .loyaltyPoints(loyaltyPoints).totalPurchases(totalPurchases)
                .registrationDate(LocalDateTime.now().minusMonths(3)).lastPurchaseDate(LocalDateTime.now().minusDays(1))
                .isActive(true).build();
    }

    private record LineItem(Product product, int quantity, BigDecimal unitPrice) {}

    private LineItem item(Product product, int quantity, BigDecimal unitPrice) {
        return new LineItem(product, quantity, unitPrice);
    }

    /** Inserts a historical sale directly (bypassing TransactionService, since the
     *  product's seeded stockQuantity above already reflects the post-sale level -
     *  this just records the matching history for dashboards/reports to read). */
    private void seedSale(String txnNumber, Customer customer, User employee, int daysAgo, List<LineItem> items, PaymentMethod method, String notes) {
        BigDecimal subtotal = items.stream().map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.08"));
        BigDecimal total = subtotal.add(tax);

        Transaction txn = transactionRepository.save(Transaction.builder()
                .transactionNumber(txnNumber).customer(customer).employee(employee).storeLocation(STORE)
                .transactionDate(LocalDateTime.now().minusDays(daysAgo))
                .subtotal(subtotal).taxAmount(tax).discountAmount(BigDecimal.ZERO).totalAmount(total)
                .paymentMethod(method).status(TransactionStatus.COMPLETED).notes(notes).build());
        // Transaction has a @PrePersist hook that force-sets transactionDate to
        // now() on insert, overriding whatever we pass in - so the date above is
        // ignored on first save. Back-date it with a second save (an UPDATE,
        // which the hook doesn't run for) so demo sales actually spread across
        // the last two weeks instead of all landing on "today".
        txn.setTransactionDate(LocalDateTime.now().minusDays(daysAgo));
        txn = transactionRepository.save(txn);

        for (LineItem li : items) {
            transactionItemRepository.save(TransactionItem.builder()
                    .transaction(txn).product(li.product()).quantity(li.quantity())
                    .unitPrice(li.unitPrice()).discountAmount(BigDecimal.ZERO).build());
            stockMovementRepository.save(StockMovement.builder()
                    .product(li.product()).movementType(MovementType.SALE).quantity(li.quantity())
                    .storeLocation(STORE).reference("Transaction " + txnNumber)
                    .performedBy(employee).timestamp(LocalDateTime.now().minusDays(daysAgo)).build());
        }
    }

    private record POLineItem(Product product, int quantityOrdered, BigDecimal unitCost, int quantityReceived) {}

    private POLineItem poItem(Product product, int quantityOrdered, BigDecimal unitCost, int quantityReceived) {
        return new POLineItem(product, quantityOrdered, unitCost, quantityReceived);
    }

    private void purchaseOrder(String poNumber, Supplier supplier, User createdBy, User approvedBy,
                               PurchaseOrderStatus status, List<POLineItem> items, int daysAgo) {
        BigDecimal total = items.stream()
                .map(i -> i.unitCost().multiply(BigDecimal.valueOf(i.quantityOrdered())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PurchaseOrder po = purchaseOrderRepository.save(PurchaseOrder.builder()
                .poNumber(poNumber).supplier(supplier).storeLocation(STORE)
                .orderDate(LocalDateTime.now().minusDays(daysAgo)).expectedDelivery(LocalDate.now().plusDays(7 - daysAgo))
                .status(status).totalAmount(total).createdBy(createdBy).approvedBy(approvedBy).build());
        // Same @PrePersist-overwrite issue as Transaction - back-date with a second save.
        po.setOrderDate(LocalDateTime.now().minusDays(daysAgo));
        po = purchaseOrderRepository.save(po);

        for (POLineItem li : items) {
            purchaseOrderItemRepository.save(PurchaseOrderItem.builder()
                    .purchaseOrder(po).product(li.product()).quantityOrdered(li.quantityOrdered())
                    .quantityReceived(li.quantityReceived()).unitCost(li.unitCost()).build());
        }
    }
}