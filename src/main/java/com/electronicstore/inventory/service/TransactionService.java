package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.request.ReturnRequest;
import com.electronicstore.inventory.dto.request.TransactionItemRequest;
import com.electronicstore.inventory.dto.request.TransactionRequest;
import com.electronicstore.inventory.dto.response.ReturnResponse;
import com.electronicstore.inventory.dto.response.TransactionItemResponse;
import com.electronicstore.inventory.dto.response.TransactionResponse;
import com.electronicstore.inventory.entity.*;
import com.electronicstore.inventory.enums.MovementType;
import com.electronicstore.inventory.enums.TransactionStatus;
import com.electronicstore.inventory.exception.InsufficientStockException;
import com.electronicstore.inventory.exception.ResourceNotFoundException;
import com.electronicstore.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FR6: Point of Sale Integration and Transaction Processing System.
 * Handles sale processing (with inventory deduction), void, and return/refund flows.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ReturnRepository returnRepository;
    private final NotificationService notificationService;
    private final PromotionService promotionService;
    private final AuditService auditService;
    @Transactional
    public TransactionResponse processSale(TransactionRequest request) {
        User employee = currentUserOrThrow();
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
        }
        Transaction transaction = Transaction.builder()
                .transactionNumber(generateTransactionNumber())
                .customer(customer)
                .employee(employee)
                .storeLocation(request.getStoreLocation())
                .paymentMethod(request.getPaymentMethod())
                .discountAmount(request.getDiscountAmount() == null ? BigDecimal.ZERO : request.getDiscountAmount())
                .status(TransactionStatus.COMPLETED)
                .notes(request.getNotes())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (TransactionItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()));

            deductStock(product, request.getStoreLocation(), itemRequest.getQuantity(), employee,
                    "Transaction " + transaction.getTransactionNumber());

            BigDecimal unitPrice = promotionService.getEffectivePrice(product);
            BigDecimal itemDiscount = itemRequest.getDiscountAmount() == null ? BigDecimal.ZERO : itemRequest.getDiscountAmount();
            BigDecimal lineGross = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            // Appendix F: "Manager override required for large discounts (>15%)."
            requireManagerApprovalForLargeDiscount(itemDiscount, lineGross,
                    "line discount on " + product.getSku());

            TransactionItem item = TransactionItem.builder()
                    .transaction(transaction)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .discountAmount(itemDiscount)
                    .build();

            transaction.getItems().add(item);
            subtotal = subtotal.add(item.getLineTotal());
        }

        BigDecimal taxRate = request.getTaxRate() == null ? BigDecimal.ZERO : request.getTaxRate();
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = transaction.getDiscountAmount();

        // Appendix F: overall transaction-level discount is subject to the same 15% rule,
        // measured against the subtotal.
        requireManagerApprovalForLargeDiscount(discount, subtotal, "overall transaction discount");

        BigDecimal total = subtotal.add(taxAmount).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        transaction.setSubtotal(subtotal);
        transaction.setTaxAmount(taxAmount);
        transaction.setTotalAmount(total);

        // Appendix F: "Customer accounts automatically created for transactions over $100."
        if (customer == null && total.compareTo(BigDecimal.valueOf(100)) > 0) {
            customer = autoCreateWalkInCustomer(request);
            transaction.setCustomer(customer);
        }

        Transaction saved = transactionRepository.save(transaction);

        if (customer != null) {
            updateCustomerLoyalty(customer, total);
        }

        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse voidTransaction(Long id) {
        Transaction transaction = findEntity(id);

        if (transaction.getStatus() == TransactionStatus.CANCELLED) {
            throw new IllegalArgumentException("Transaction is already voided.");
        }
        if (transaction.getStatus() == TransactionStatus.REFUNDED) {
            throw new IllegalArgumentException("Cannot void a fully refunded transaction.");
        }

        User employee = currentUserOrThrow();

        // Reverse stock deduction for every line item
        for (TransactionItem item : transaction.getItems()) {
            restockItem(item.getProduct(), transaction.getStoreLocation(), item.getQuantity(), employee,
                    "Void of transaction " + transaction.getTransactionNumber());
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public ReturnResponse processReturn(Long transactionId, ReturnRequest request) {
        Transaction transaction = findEntity(transactionId);
        User employee = currentUserOrThrow();

        TransactionItem matchingItem = transaction.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product " + request.getProductId() + " was not part of this transaction."));

        if (request.getQuantity() > matchingItem.getQuantity()) {
            throw new IllegalArgumentException("Return quantity exceeds quantity originally purchased.");
        }

        Product product = matchingItem.getProduct();

        // Restock returned items
        restockItem(product, transaction.getStoreLocation(), request.getQuantity(), employee,
                "Return against transaction " + transaction.getTransactionNumber());

        BigDecimal unitTotal = matchingItem.getUnitPrice();
        BigDecimal refundAmount = unitTotal.multiply(BigDecimal.valueOf(request.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        Return returnRecord = Return.builder()
                .transaction(transaction)
                .product(product)
                .quantity(request.getQuantity())
                .reason(request.getReason())
                .refundAmount(refundAmount)
                .processedBy(employee)
                .build();
        Return saved = returnRepository.save(returnRecord);
        boolean fullyReturned = request.getQuantity().equals(matchingItem.getQuantity());
        transaction.setStatus(fullyReturned ? TransactionStatus.REFUNDED : TransactionStatus.PARTIALLY_REFUNDED);
        transactionRepository.save(transaction);
        notificationService.notifyReturnProcessed(transaction.getTransactionNumber(), product.getSku(), request.getQuantity());
        return ReturnResponse.builder()
                .id(saved.getId())
                .transactionId(transaction.getId())
                .transactionNumber(transaction.getTransactionNumber())
                .productId(product.getId())
                .productName(product.getName())
                .quantity(saved.getQuantity())
                .reason(saved.getReason())
                .returnDate(saved.getReturnDate())
                .refundAmount(saved.getRefundAmount())
                .build();
    }

    public TransactionResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    public TransactionResponse getByNumber(String transactionNumber) {
        Transaction transaction = transactionRepository.findByTransactionNumber(transactionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionNumber));
        return toResponse(transaction);
    }

    public Page<TransactionResponse> getByCustomer(Long customerId, Pageable pageable) {
        return transactionRepository.findByCustomerId(customerId, pageable).map(this::toResponse);
    }

    public Page<TransactionResponse> getByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return transactionRepository.findByTransactionDateBetween(start, end, pageable).map(this::toResponse);
    }

    // ---- internal helpers ----

    private void deductStock(Product product, String storeLocation, int quantity, User employee, String reference) {
        Inventory inventory = inventoryRepository.findByProductIdAndStoreLocation(product.getId(), storeLocation)
                .orElseGet(() -> Inventory.builder()
                        .product(product).storeLocation(storeLocation)
                        .quantityOnHand(0).quantityReserved(0).build());

        int available = inventory.getAvailableQuantity();
        if (available < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + product.getSku() + " at " + storeLocation
                            + " (available: " + available + ", requested: " + quantity + ")");
        }

        inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantity);
        inventoryRepository.save(inventory);

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        if (product.getStockQuantity() <= product.getReorderPoint()) {
            notificationService.notifyLowStock(product);
        }

        recordMovement(product, MovementType.SALE, quantity, storeLocation, employee, reference);
    }

    private void restockItem(Product product, String storeLocation, int quantity, User employee, String reference) {
        Inventory inventory = inventoryRepository.findByProductIdAndStoreLocation(product.getId(), storeLocation)
                .orElseGet(() -> Inventory.builder()
                        .product(product).storeLocation(storeLocation)
                        .quantityOnHand(0).quantityReserved(0).build());

        inventory.setQuantityOnHand(inventory.getQuantityOnHand() + quantity);
        inventoryRepository.save(inventory);

        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);

        recordMovement(product, MovementType.RETURN, quantity, storeLocation, employee, reference);
    }

    private void recordMovement(Product product, MovementType type, int quantity, String storeLocation,
                                User employee, String reference) {
        StockMovement movement = StockMovement.builder()
                .product(product)
                .movementType(type)
                .quantity(quantity)
                .storeLocation(storeLocation)
                .reference(reference)
                .performedBy(employee)
                .build();
        stockMovementRepository.save(movement);
    }

    /**
     * Appendix F: "Customer accounts automatically created for transactions over $100."
     * Requires at minimum first name, last name, and phone (Customer.phone is a required
     * column) - if that info wasn't supplied on the request, this is a courtesy feature, not
     * a hard requirement, so the sale simply proceeds as an anonymous walk-in instead of
     * failing.
     */
    private Customer autoCreateWalkInCustomer(TransactionRequest request) {
        if (request.getWalkInFirstName() == null || request.getWalkInFirstName().isBlank()
                || request.getWalkInLastName() == null || request.getWalkInLastName().isBlank()
                || request.getWalkInPhone() == null || request.getWalkInPhone().isBlank()) {
            return null;
        }

        Customer customer = Customer.builder()
                .firstName(request.getWalkInFirstName())
                .lastName(request.getWalkInLastName())
                .phone(request.getWalkInPhone())
                .email(request.getWalkInEmail())
                .loyaltyPoints(0)
                .totalPurchases(BigDecimal.ZERO)
                .isActive(true)
                .build();

        Customer saved = customerRepository.save(customer);
        auditService.logCurrentUser("CUSTOMER_AUTO_CREATED", "CUSTOMER", String.valueOf(saved.getId()),
                null, "Auto-created for transaction over $100");
        return saved;
    }

    private void updateCustomerLoyalty(Customer customer, BigDecimal total) {
        // 1 point per $1 spent (Appendix F)
        int pointsEarned = total.setScale(0, RoundingMode.DOWN).intValue();
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + pointsEarned);
        customer.setTotalPurchases(customer.getTotalPurchases().add(total));
        customer.setLastPurchaseDate(LocalDateTime.now());
        customerRepository.save(customer);
    }

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    /**
     * NOTE: the in-memory counter is per-process and resets on restart. It's fine for a
     * single-instance dev/demo setup, but for production/multi-instance deployments this
     * should be replaced with a DB sequence or a UUID-based scheme to guarantee uniqueness.
     */
    private String generateTransactionNumber() {
        String timestampPart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "TXN-" + timestampPart + "-" + String.format("%04d", SEQUENCE.getAndIncrement() % 10000);
    }

    private Transaction findEntity(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    private User currentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        throw new IllegalStateException("No authenticated employee found for this operation.");
    }

    private static final BigDecimal MAX_UNAPPROVED_DISCOUNT_PERCENT = BigDecimal.valueOf(15);

    /**
     * Appendix F: "Manager override required for large discounts (>15%)." Cashiers and Sales
     * Associates can apply ordinary discounts; anything exceeding 15% of the base amount is
     * rejected unless the transaction is being processed by a Store Manager or Admin.
     */
    private void requireManagerApprovalForLargeDiscount(BigDecimal discountAmount, BigDecimal baseAmount, String context) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0
                || baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal discountPercent = discountAmount.divide(baseAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (discountPercent.compareTo(MAX_UNAPPROVED_DISCOUNT_PERCENT) > 0 && !currentUserIsManagerOrAdmin()) {
            throw new IllegalArgumentException(
                    "The " + context + " is " + discountPercent.setScale(1, RoundingMode.HALF_UP)
                            + "%, which exceeds the 15% threshold that requires Store Manager or Admin approval.");
        }
    }

    private boolean currentUserIsManagerOrAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return false;
        }
        return user.getRole() == com.electronicstore.inventory.enums.Role.STORE_MANAGER
                || user.getRole() == com.electronicstore.inventory.enums.Role.ADMIN;
    }

    private TransactionResponse toResponse(Transaction t) {
        List<TransactionItemResponse> items = t.getItems().stream()
                .map(i -> TransactionItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProduct().getId())
                        .productName(i.getProduct().getName())
                        .sku(i.getProduct().getSku())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .discountAmount(i.getDiscountAmount())
                        .lineTotal(i.getLineTotal())
                        .build())
                .toList();

        return TransactionResponse.builder()
                .id(t.getId())
                .transactionNumber(t.getTransactionNumber())
                .customerId(t.getCustomer() != null ? t.getCustomer().getId() : null)
                .customerName(t.getCustomer() != null ? t.getCustomer().getFirstName() + " " + t.getCustomer().getLastName() : null)
                .employeeId(t.getEmployee().getId())
                .employeeName(t.getEmployee().getFirstName() + " " + t.getEmployee().getLastName())
                .storeLocation(t.getStoreLocation())
                .transactionDate(t.getTransactionDate())
                .subtotal(t.getSubtotal())
                .taxAmount(t.getTaxAmount())
                .discountAmount(t.getDiscountAmount())
                .totalAmount(t.getTotalAmount())
                .paymentMethod(t.getPaymentMethod())
                .status(t.getStatus())
                .notes(t.getNotes())
                .items(items)
                .build();
    }
}