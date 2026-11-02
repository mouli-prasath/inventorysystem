package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.request.CycleCountRequest;
import com.electronicstore.inventory.dto.request.StockAdjustmentRequest;
import com.electronicstore.inventory.dto.response.InventoryResponse;
import com.electronicstore.inventory.entity.Inventory;
import com.electronicstore.inventory.entity.Product;
import com.electronicstore.inventory.entity.StockMovement;
import com.electronicstore.inventory.entity.User;
import com.electronicstore.inventory.enums.MovementType;
import com.electronicstore.inventory.exception.InsufficientStockException;
import com.electronicstore.inventory.exception.ResourceNotFoundException;
import com.electronicstore.inventory.repository.InventoryRepository;
import com.electronicstore.inventory.repository.ProductRepository;
import com.electronicstore.inventory.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * FR5: Real-Time Inventory Tracking and Control System.
 * Tracks stock per store location and records every movement for audit purposes.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {
    private static final Set<MovementType> INBOUND = Set.of(
            MovementType.STOCK_IN, MovementType.RETURN, MovementType.TRANSFER_IN);
    private static final Set<MovementType> OUTBOUND = Set.of(
            MovementType.STOCK_OUT, MovementType.SALE, MovementType.DAMAGE,
            MovementType.THEFT, MovementType.TRANSFER_OUT);
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NotificationService notificationService;
    @Transactional
    public InventoryResponse adjustStock(StockAdjustmentRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        int delta = resolveDelta(request.getMovementType(), request.getQuantity());
        boolean allowNegative = resolveManagerOverride(request.getManagerOverride());
        Inventory saved = applyDelta(product, request.getStoreLocation(), delta, product.getSku(), allowNegative);
        recordMovement(product, request.getMovementType(), request.getQuantity(),
                request.getStoreLocation(), request.getReference(), request.getNotes());
        return toResponse(saved);
    }
    private boolean resolveManagerOverride(Boolean requested) {
        if (requested == null || !requested) {
            return false;
        }
        if (!currentUserIsManagerOrAdmin()) {
            throw new IllegalArgumentException(
                    "Manager override requires a Store Manager or Admin role.");
        }
        return true;
    }
    private boolean currentUserIsManagerOrAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return false;
        }
        return user.getRole() == com.electronicstore.inventory.enums.Role.STORE_MANAGER
                || user.getRole() == com.electronicstore.inventory.enums.Role.ADMIN;
    }
    @Transactional
    public InventoryResponse recordCycleCount(CycleCountRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Inventory existing = inventoryRepository
                .findByProductIdAndStoreLocation(request.getProductId(), request.getStoreLocation())
                .orElse(null);
        int previousQuantity = existing != null ? existing.getQuantityOnHand() : 0;
        int delta = request.getCountedQuantity() - previousQuantity;

        Inventory saved = applyDelta(product, request.getStoreLocation(), delta, product.getSku(), false);

        String notes = "Cycle count: counted=" + request.getCountedQuantity()
                + ", previous=" + previousQuantity
                + (request.getNotes() != null && !request.getNotes().isBlank() ? " | " + request.getNotes() : "");

        recordMovement(product, MovementType.CYCLE_COUNT, Math.abs(delta),
                request.getStoreLocation(), "Cycle count", notes);

        return toResponse(saved);
    }

    public List<InventoryResponse> getByProduct(Long productId) {
        return inventoryRepository.findByProductId(productId).stream().map(this::toResponse).toList();
    }

    public List<InventoryResponse> getByLocation(String storeLocation) {
        return inventoryRepository.findByStoreLocation(storeLocation).stream().map(this::toResponse).toList();
    }

    /** Appendix H: GET /inventory/stock - current stock levels across all products/locations. */
    public Page<InventoryResponse> getAll(Pageable pageable) {
        return inventoryRepository.findAll(pageable).map(this::toResponse);
    }

    // ---- shared internals ----

    /** Applies a signed quantity change to a location's inventory row and keeps the product's
     *  aggregate stock figure and low-stock notification in sync. Creates the inventory row
     *  if this is the first time stock is tracked at that location. */
    private Inventory applyDelta(Product product, String storeLocation, int delta, String skuForError, boolean allowNegative) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndStoreLocation(product.getId(), storeLocation)
                .orElseGet(() -> Inventory.builder()
                        .product(product)
                        .storeLocation(storeLocation)
                        .quantityOnHand(0)
                        .quantityReserved(0)
                        .build());

        int newQuantity = inventory.getQuantityOnHand() + delta;
        if (newQuantity < 0 && !allowNegative) {
            throw new InsufficientStockException(
                    "Insufficient stock: cannot reduce quantity below zero for product " + skuForError
                            + ". A Store Manager or Admin can resubmit this adjustment with managerOverride=true.");
        }

        inventory.setQuantityOnHand(newQuantity);
        Inventory saved = inventoryRepository.save(inventory);

        // Keep the product's aggregate stock figure in sync (simple single-location model
        // can be extended to sum across locations for true multi-location totals)
        product.setStockQuantity(sumAcrossLocations(product.getId()));
        productRepository.save(product);

        if (product.getStockQuantity() <= product.getReorderPoint()) {
            notificationService.notifyLowStock(product);
        }

        return saved;
    }

    private void recordMovement(Product product, MovementType type, int quantity, String storeLocation,
                                 String reference, String notes) {
        StockMovement movement = StockMovement.builder()
                .product(product)
                .movementType(type)
                .quantity(quantity)
                .reference(reference)
                .storeLocation(storeLocation)
                .notes(notes)
                .performedBy(currentUser())
                .build();
        stockMovementRepository.save(movement);
    }

    private int sumAcrossLocations(Long productId) {
        return inventoryRepository.findByProductId(productId).stream()
                .mapToInt(Inventory::getQuantityOnHand)
                .sum();
    }

    private int resolveDelta(MovementType type, int quantity) {
        if (INBOUND.contains(type)) return quantity;
        if (OUTBOUND.contains(type)) return -quantity;
        // ADJUSTMENT / CYCLE_COUNT: quantity is a signed correction (can be negative
        // if the caller passes a negative value for a downward correction)
        return quantity;
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private InventoryResponse toResponse(Inventory inv) {
        return InventoryResponse.builder()
                .id(inv.getId())
                .productId(inv.getProduct().getId())
                .productName(inv.getProduct().getName())
                .sku(inv.getProduct().getSku())
                .storeLocation(inv.getStoreLocation())
                .quantityOnHand(inv.getQuantityOnHand())
                .quantityReserved(inv.getQuantityReserved())
                .quantityAvailable(inv.getAvailableQuantity())
                .lastUpdated(inv.getLastUpdated())
                .build();
    }
}
