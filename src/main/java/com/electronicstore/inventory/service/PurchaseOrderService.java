package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.request.PurchaseOrderItemRequest;
import com.electronicstore.inventory.dto.request.PurchaseOrderRequest;
import com.electronicstore.inventory.dto.request.ReceiveDeliveryRequest;
import com.electronicstore.inventory.dto.response.PurchaseOrderItemResponse;
import com.electronicstore.inventory.dto.response.PurchaseOrderResponse;
import com.electronicstore.inventory.entity.*;
import com.electronicstore.inventory.enums.MovementType;
import com.electronicstore.inventory.enums.PurchaseOrderStatus;
import com.electronicstore.inventory.exception.ResourceNotFoundException;
import com.electronicstore.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * FR8: Supplier Management and Purchase Order System (order automation portion).
 * FR9: Procurement and Order Management System (receiving, cost tracking).
 * FR5-adjacent: auto-generation is driven off each product's reorder point.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(generatePoNumber())
                .supplier(supplier)
                .expectedDelivery(request.getExpectedDelivery())
                .storeLocation(request.getStoreLocation())
                .status(PurchaseOrderStatus.PENDING)
                .createdBy(currentUser())
                .notes(request.getNotes())
                .build();
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()));

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .quantityOrdered(itemRequest.getQuantityOrdered())
                    .unitCost(itemRequest.getUnitCost())
                    .quantityReceived(0)
                    .build();

            po.getItems().add(item);
            total = total.add(item.getLineTotal());
        }
        po.setTotalAmount(total);

        return toResponse(purchaseOrderRepository.save(po));
    }

    /**
     * FR5/FR8: Automated reorder system - generates a draft purchase order per supplier
     * for every active product at or below its reorder point, using the product's cost
     * price and a simple "top up to 3x reorder point" quantity heuristic.
     */
    @Transactional
    public List<PurchaseOrderResponse> autoGenerateFromLowStock(String storeLocation) {
        List<Product> lowStockProducts = productRepository.findProductsAtOrBelowReorderPoint();

        Map<Long, List<Product>> productsBySupplier = lowStockProducts.stream()
                .filter(p -> p.getSupplier() != null)
                .collect(Collectors.groupingBy(p -> p.getSupplier().getId()));

        List<PurchaseOrderResponse> generated = new ArrayList<>();

        for (Map.Entry<Long, List<Product>> entry : productsBySupplier.entrySet()) {
            Supplier supplier = supplierRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + entry.getKey()));

            PurchaseOrder po = PurchaseOrder.builder()
                    .poNumber(generatePoNumber())
                    .supplier(supplier)
                    .storeLocation(storeLocation)
                    .status(PurchaseOrderStatus.PENDING)
                    .createdBy(currentUser())
                    .notes("Auto-generated from low-stock alert")
                    .build();

            BigDecimal total = BigDecimal.ZERO;
            for (Product product : entry.getValue()) {
                int suggestedQty = Math.max(product.getReorderPoint() * 3 - product.getStockQuantity(), product.getReorderPoint());

                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .purchaseOrder(po)
                        .product(product)
                        .quantityOrdered(suggestedQty)
                        .unitCost(product.getCostPrice())
                        .quantityReceived(0)
                        .build();

                po.getItems().add(item);
                total = total.add(item.getLineTotal());
            }
            po.setTotalAmount(total);

            generated.add(toResponse(purchaseOrderRepository.save(po)));
        }

        return generated;
    }

    @Transactional
    public PurchaseOrderResponse approve(Long id) {
        PurchaseOrder po = findEntity(id);
        if (po.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new IllegalArgumentException("Only pending purchase orders can be approved.");
        }
        po.setStatus(PurchaseOrderStatus.APPROVED);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        notificationService.notifyPurchaseOrderStatusChange(saved.getPoNumber(), saved.getStatus(), saved.getCreatedBy());
        auditService.logCurrentUser("PO_APPROVED", "PURCHASE_ORDER", saved.getPoNumber(), "PENDING", "APPROVED");
        return toResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse markOrdered(Long id) {
        PurchaseOrder po = findEntity(id);
        if (po.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved purchase orders can be marked as ordered.");
        }
        po.setStatus(PurchaseOrderStatus.ORDERED);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        notificationService.notifyPurchaseOrderStatusChange(saved.getPoNumber(), saved.getStatus(), saved.getCreatedBy());
        return toResponse(saved);
    }

    /**
     * FR9: Inventory receiving shall update stock levels upon delivery confirmation.
     * Supports partial deliveries; updates PO status to PARTIALLY_RECEIVED or RECEIVED.
     */
    @Transactional
    public PurchaseOrderResponse receiveDelivery(Long id, ReceiveDeliveryRequest request) {
        PurchaseOrder po = findEntity(id);
        if (po.getStatus() == PurchaseOrderStatus.CANCELLED || po.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new IllegalArgumentException("This purchase order cannot receive further deliveries.");
        }

        User employee = currentUser();

        Map<Long, PurchaseOrderItem> itemsById = po.getItems().stream()
                .collect(Collectors.toMap(PurchaseOrderItem::getId, i -> i));

        for (ReceiveDeliveryRequest.ReceivedLineItem line : request.getReceivedItems()) {
            PurchaseOrderItem item = itemsById.get(line.getPurchaseOrderItemId());
            if (item == null) {
                throw new ResourceNotFoundException(
                        "Purchase order item not found: " + line.getPurchaseOrderItemId());
            }

            int newReceivedTotal = item.getQuantityReceived() + line.getQuantityReceived();
            if (newReceivedTotal > item.getQuantityOrdered()) {
                throw new IllegalArgumentException(
                        "Received quantity exceeds ordered quantity for product " + item.getProduct().getSku());
            }
            item.setQuantityReceived(newReceivedTotal);
            purchaseOrderItemRepository.save(item);

            receiveStock(item.getProduct(), po.getStoreLocation(), line.getQuantityReceived(), employee, po.getPoNumber());
        }

        boolean allFullyReceived = po.getItems().stream()
                .allMatch(i -> i.getQuantityReceived().equals(i.getQuantityOrdered()));
        boolean anyReceived = po.getItems().stream().anyMatch(i -> i.getQuantityReceived() > 0);

        po.setStatus(allFullyReceived ? PurchaseOrderStatus.RECEIVED
                : anyReceived ? PurchaseOrderStatus.PARTIALLY_RECEIVED : po.getStatus());

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        notificationService.notifyPurchaseOrderStatusChange(saved.getPoNumber(), saved.getStatus(), saved.getCreatedBy());
        return toResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long id) {
        PurchaseOrder po = findEntity(id);
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new IllegalArgumentException("A fully received purchase order cannot be cancelled.");
        }
        po.setStatus(PurchaseOrderStatus.CANCELLED);
        return toResponse(purchaseOrderRepository.save(po));
    }

    public PurchaseOrderResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    public Page<PurchaseOrderResponse> getBySupplier(Long supplierId, Pageable pageable) {
        return purchaseOrderRepository.findBySupplierId(supplierId, pageable).map(this::toResponse);
    }

    public Page<PurchaseOrderResponse> getByStatus(PurchaseOrderStatus status, Pageable pageable) {
        return purchaseOrderRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    // ---- internal helpers ----

    private void receiveStock(Product product, String storeLocation, int quantity, User employee, String reference) {
        String location = (storeLocation == null || storeLocation.isBlank()) ? "MAIN" : storeLocation;

        Inventory inventory = inventoryRepository.findByProductIdAndStoreLocation(product.getId(), location)
                .orElseGet(() -> Inventory.builder()
                        .product(product).storeLocation(location)
                        .quantityOnHand(0).quantityReserved(0).build());

        inventory.setQuantityOnHand(inventory.getQuantityOnHand() + quantity);
        inventoryRepository.save(inventory);

        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .movementType(MovementType.STOCK_IN)
                .quantity(quantity)
                .storeLocation(location)
                .reference("PO " + reference)
                .performedBy(employee)
                .build();
        stockMovementRepository.save(movement);
    }

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    private String generatePoNumber() {
        String timestampPart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "PO-" + timestampPart + "-" + String.format("%04d", SEQUENCE.getAndIncrement() % 10000);
    }

    private PurchaseOrder findEntity(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
    public Page<PurchaseOrderResponse> getAll(Pageable pageable) {
        return purchaseOrderRepository.findAll(pageable).map(this::toResponse);
    }
    private PurchaseOrderResponse toResponse(PurchaseOrder po) {
        List<PurchaseOrderItemResponse> items = po.getItems().stream()
                .map(i -> PurchaseOrderItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProduct().getId())
                        .productName(i.getProduct().getName())
                        .sku(i.getProduct().getSku())
                        .quantityOrdered(i.getQuantityOrdered())
                        .quantityReceived(i.getQuantityReceived())
                        .unitCost(i.getUnitCost())
                        .lineTotal(i.getLineTotal())
                        .build())
                .toList();

        return PurchaseOrderResponse.builder()
                .id(po.getId())
                .poNumber(po.getPoNumber())
                .supplierId(po.getSupplier().getId())
                .supplierName(po.getSupplier().getSupplierName())
                .orderDate(po.getOrderDate())
                .expectedDelivery(po.getExpectedDelivery())
                .status(po.getStatus())
                .storeLocation(po.getStoreLocation())
                .totalAmount(po.getTotalAmount())
                .createdByName(po.getCreatedBy() != null
                        ? po.getCreatedBy().getFirstName() + " " + po.getCreatedBy().getLastName() : null)
                .notes(po.getNotes())
                .items(items)
                .build();
    }
}
