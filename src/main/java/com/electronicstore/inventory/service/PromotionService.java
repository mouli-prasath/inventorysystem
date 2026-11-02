package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.request.PromotionRequest;
import com.electronicstore.inventory.dto.response.PromotionResponse;
import com.electronicstore.inventory.entity.Category;
import com.electronicstore.inventory.entity.Product;
import com.electronicstore.inventory.entity.Promotion;
import com.electronicstore.inventory.enums.DiscountType;
import com.electronicstore.inventory.exception.ResourceNotFoundException;
import com.electronicstore.inventory.repository.CategoryRepository;
import com.electronicstore.inventory.repository.ProductRepository;
import com.electronicstore.inventory.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FR4/scope: "Price management and promotional system".
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        validate(request);

        Product productScope = request.getProductScopeId() != null
                ? productRepository.findById(request.getProductScopeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductScopeId()))
                : null;

        Category categoryScope = request.getCategoryScopeId() != null
                ? categoryRepository.findById(request.getCategoryScopeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryScopeId()))
                : null;

        Promotion promotion = Promotion.builder()
                .promotionName(request.getPromotionName())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .productScope(productScope)
                .categoryScope(categoryScope)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .isActive(true)
                .build();

        return toResponse(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionResponse update(Long id, PromotionRequest request) {
        validate(request);
        Promotion promotion = findEntity(id);

        Product productScope = request.getProductScopeId() != null
                ? productRepository.findById(request.getProductScopeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductScopeId()))
                : null;

        Category categoryScope = request.getCategoryScopeId() != null
                ? categoryRepository.findById(request.getCategoryScopeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryScopeId()))
                : null;

        promotion.setPromotionName(request.getPromotionName());
        promotion.setDiscountType(request.getDiscountType());
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setProductScope(productScope);
        promotion.setCategoryScope(categoryScope);
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setDescription(request.getDescription());

        return toResponse(promotionRepository.save(promotion));
    }

    public PromotionResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    public List<PromotionResponse> getAll() {
        return promotionRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<PromotionResponse> getCurrentlyRunning() {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findByIsActiveTrue().stream()
                .filter(p -> p.isCurrentlyValid(now))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deactivate(Long id) {
        Promotion promotion = findEntity(id);
        promotion.setActive(false);
        promotionRepository.save(promotion);
    }

    /**
     * Resolves the best applicable price for a product right now: if multiple active
     * promotions apply (e.g. a product-specific one and a category-wide one), the one giving
     * the deepest discount wins.
     */
    public BigDecimal getEffectivePrice(Product product) {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal basePrice = product.getPrice();

        Optional<BigDecimal> best = promotionRepository.findByIsActiveTrue().stream()
                .filter(p -> p.isCurrentlyValid(now) && p.appliesToProduct(product))
                .map(p -> p.applyTo(basePrice))
                .min(BigDecimal::compareTo);

        return best.orElse(basePrice);
    }

    private void validate(PromotionRequest request) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date.");
        }
        if (request.getDiscountType() == DiscountType.PERCENTAGE
                && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100.");
        }
        if (request.getProductScopeId() != null && request.getCategoryScopeId() != null) {
            throw new IllegalArgumentException("A promotion can target a product or a category, not both.");
        }
    }

    private Promotion findEntity(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));
    }

    private PromotionResponse toResponse(Promotion p) {
        LocalDateTime now = LocalDateTime.now();
        return PromotionResponse.builder()
                .id(p.getId())
                .promotionName(p.getPromotionName())
                .discountType(p.getDiscountType())
                .discountValue(p.getDiscountValue())
                .productScopeId(p.getProductScope() != null ? p.getProductScope().getId() : null)
                .productScopeName(p.getProductScope() != null ? p.getProductScope().getName() : null)
                .categoryScopeId(p.getCategoryScope() != null ? p.getCategoryScope().getId() : null)
                .categoryScopeName(p.getCategoryScope() != null ? p.getCategoryScope().getCategoryName() : null)
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .isActive(p.isActive())
                .currentlyRunning(p.isCurrentlyValid(now))
                .description(p.getDescription())
                .build();
    }
}
