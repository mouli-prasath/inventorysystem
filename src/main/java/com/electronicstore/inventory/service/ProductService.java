package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.request.ProductRequest;
import com.electronicstore.inventory.dto.response.ProductResponse;
import com.electronicstore.inventory.entity.Category;
import com.electronicstore.inventory.entity.Product;
import com.electronicstore.inventory.entity.Supplier;
import com.electronicstore.inventory.exception.DuplicateResourceException;
import com.electronicstore.inventory.exception.ResourceNotFoundException;
import com.electronicstore.inventory.repository.CategoryRepository;
import com.electronicstore.inventory.repository.ProductRepository;
import com.electronicstore.inventory.repository.SupplierRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final PromotionService promotionService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU must be unique.");
        }
        if (request.getBarcode() != null && !request.getBarcode().isBlank()
                && productRepository.existsByBarcode(request.getBarcode())) {
            throw new DuplicateResourceException("Barcode already exists.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid category selected."));

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .brand(request.getBrand())
                .model(request.getModel())
                .price(request.getPrice())
                .costPrice(request.getCostPrice())
                .stockQuantity(request.getStockQuantity())
                .reorderPoint(request.getReorderPoint() == null ? 10 : request.getReorderPoint())
                .supplier(supplier)
                .barcode(blankToNull(request.getBarcode()))
                .weight(request.getWeight())
                .dimensions(request.getDimensions())
                .warrantyPeriod(request.getWarrantyPeriod())
                .imageUrl(blankToNull(request.getImageUrl()))
                .specSheetUrl(blankToNull(request.getSpecSheetUrl()))
                .isActive(true)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntity(id);

        if (!product.getSku().equalsIgnoreCase(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU must be unique.");
        }
        if (request.getBarcode() != null && !request.getBarcode().isBlank()
                && !request.getBarcode().equals(product.getBarcode())
                && productRepository.existsByBarcode(request.getBarcode())) {
            throw new DuplicateResourceException("Barcode already exists.");
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid category selected."));
        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));
        }
        BigDecimal oldPrice = product.getPrice();
        boolean priceChanged = oldPrice != null && oldPrice.compareTo(request.getPrice()) != 0;

        if (priceChanged) {
            BigDecimal percentChange = calculatePercentChange(oldPrice, request.getPrice());
            if (percentChange.compareTo(MAJOR_PRICE_CHANGE_THRESHOLD) > 0 && !currentUserCanApproveLargeChanges()) {
                throw new IllegalArgumentException(
                        "This price change is " + percentChange.setScale(1, java.math.RoundingMode.HALF_UP)
                                + "%, which exceeds the 20% threshold that requires Store Manager or Admin approval.");
            }
        }
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBrand(request.getBrand());
        product.setModel(request.getModel());
        product.setPrice(request.getPrice());
        product.setCostPrice(request.getCostPrice());
        product.setStockQuantity(request.getStockQuantity());
        if (request.getReorderPoint() != null) {
            product.setReorderPoint(request.getReorderPoint());
        }
        product.setSupplier(supplier);
        product.setBarcode(blankToNull(request.getBarcode()));
        product.setWeight(request.getWeight());
        product.setDimensions(request.getDimensions());
        product.setWarrantyPeriod(request.getWarrantyPeriod());
        product.setImageUrl(blankToNull(request.getImageUrl()));
        product.setSpecSheetUrl(blankToNull(request.getSpecSheetUrl()));

        Product saved = productRepository.save(product);

        if (priceChanged) {
            auditPriceChange(saved, oldPrice, request.getPrice());
        }

        return toResponse(saved);
    }

    private static final BigDecimal MAJOR_PRICE_CHANGE_THRESHOLD = BigDecimal.valueOf(20);

    private BigDecimal calculatePercentChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue == null || oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        return newValue.subtract(oldValue).abs()
                .divide(oldValue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private boolean currentUserCanApproveLargeChanges() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof com.electronicstore.inventory.entity.User user)) {
            return false;
        }
        return user.getRole() == com.electronicstore.inventory.enums.Role.STORE_MANAGER
                || user.getRole() == com.electronicstore.inventory.enums.Role.ADMIN;
    }

    private void auditPriceChange(Product product, BigDecimal oldPrice, BigDecimal newPrice) {
        BigDecimal percentChange = calculatePercentChange(oldPrice, newPrice);
        boolean isMajorChange = percentChange.compareTo(MAJOR_PRICE_CHANGE_THRESHOLD) > 0;

        auditService.logCurrentUser(
                isMajorChange ? "PRICE_CHANGE_MAJOR" : "PRICE_CHANGE",
                "PRODUCT",
                String.valueOf(product.getId()),
                oldPrice.toPlainString(),
                newPrice.toPlainString());
    }
    public ProductResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    /**
     * FR4/FR5-supporting search & filter: by keyword (name/SKU/barcode), category, brand,
     * price range, and active status.
     */
    public Page<ProductResponse> search(String keyword, Long categoryId, String brand,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         Boolean activeOnly, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("sku")), like),
                        cb.like(cb.lower(root.get("barcode")), like)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), brand.toLowerCase()));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (activeOnly == null || activeOnly) {
                predicates.add(cb.isTrue(root.get("isActive")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public List<ProductResponse> getLowStock() {
        return productRepository.findProductsAtOrBelowReorderPoint().stream().map(this::toResponse).toList();
    }

    /** Appendix H: PUT /inventory/reorder - update a product's reorder point independently of a full product edit. */
    @Transactional
    public ProductResponse updateReorderPoint(Long productId, Integer reorderPoint) {
        Product product = findEntity(productId);
        product.setReorderPoint(reorderPoint);
        Product saved = productRepository.save(product);
        if (saved.getStockQuantity() != null && saved.getStockQuantity() <= reorderPoint) {
            notificationService.notifyLowStock(saved);
        }
        return toResponse(saved);
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = findEntity(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .description(p.getDescription())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getCategoryName() : null)
                .brand(p.getBrand())
                .model(p.getModel())
                .price(p.getPrice())
                .costPrice(p.getCostPrice())
                .stockQuantity(p.getStockQuantity())
                .reorderPoint(p.getReorderPoint())
                .supplierId(p.getSupplier() != null ? p.getSupplier().getId() : null)
                .supplierName(p.getSupplier() != null ? p.getSupplier().getSupplierName() : null)
                .barcode(p.getBarcode())
                .weight(p.getWeight())
                .dimensions(p.getDimensions())
                .warrantyPeriod(p.getWarrantyPeriod())
                .imageUrl(p.getImageUrl())
                .specSheetUrl(p.getSpecSheetUrl())
                .isActive(p.isActive())
                .lowStock(p.getStockQuantity() != null && p.getStockQuantity() <= p.getReorderPoint())
                .effectivePrice(promotionService.getEffectivePrice(p))
                .build();
    }
}
