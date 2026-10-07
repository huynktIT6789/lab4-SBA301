package com.example.lab41.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.lab41.dto.PageResponse;
import com.example.lab41.dto.ProductRequest;
import com.example.lab41.dto.ProductResponse;
import com.example.lab41.dto.ReviewRequest;
import com.example.lab41.dto.ReviewResponse;
import com.example.lab41.exception.ResourceNotFoundException;
import com.example.lab41.model.Product;
import com.example.lab41.model.Review;
import com.example.lab41.repository.CategoryRepository;
import com.example.lab41.repository.ProductRepository;

@Service
public class ProductService {

    /** Chi cho phep sort tren cac field nay (guardrail, xem muc 9.2). */
    static final Set<String> SORT_FIELDS = Set.of("name", "price", "stock");
    static final int MAX_PAGE_SIZE = 50;

    private final ProductRepository products;
    private final CategoryRepository categories;

    public ProductService(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    public ProductResponse findById(String id) {
        return toResponse(getRequired(id));
    }

    public ProductResponse create(ProductRequest request) {
        validateCategory(request.categoryId());
        Product p = new Product();
        apply(p, request);
        return toResponse(products.save(p));
    }

    public ProductResponse update(String id, ProductRequest request) {
        Product p = getRequired(id);
        validateCategory(request.categoryId());
        apply(p, request);
        return toResponse(products.save(p));
    }

    public void delete(String id) {
        Product p = getRequired(id);
        products.delete(p);
    }

    public PageResponse<ProductResponse> search(String keyword, String categoryId,
                                                int page, int size,
                                                String sortBy, String direction) {
        if (!SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Unsupported sort field: " + sortBy + " (allowed: " + SORT_FIELDS + ")");
        }
        String kw = keyword == null ? "" : keyword.trim();
        String cat = (categoryId == null || categoryId.isBlank()) ? null : categoryId;

        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Sort.Direction dir = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(dir, sortBy));

        Page<Product> result;
        if (!kw.isEmpty() && cat != null) {
            result = products.findByNameContainingIgnoreCaseAndCategoryId(kw, cat, pageable);
        } else if (!kw.isEmpty()) {
            result = products.findByNameContainingIgnoreCase(kw, pageable);
        } else if (cat != null) {
            result = products.findByCategoryId(cat, pageable);
        } else {
            result = products.findAll(pageable);
        }
        return PageResponse.from(result.map(this::toResponse));
    }

    /** Them review vao mang embedded cua Product (atomic o muc 1 document). */
    public ProductResponse addReview(String productId, ReviewRequest request) {
        Product p = getRequired(productId);
        Review review = new Review(
                request.userId(), request.rating(), request.comment(), Instant.now());
        p.getReviews().add(review);
        return toResponse(products.save(p));
    }

    // ---------- helpers ----------

    private Product getRequired(String id) {
        return products.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    /** MongoDB khong co FK -> service tu kiem tra reference. */
    private void validateCategory(String categoryId) {
        if (categoryId != null && !categories.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found: " + categoryId);
        }
    }

    private void apply(Product p, ProductRequest r) {
        p.setName(r.name());
        p.setPrice(r.price());
        p.setStock(r.stock());
        p.setCategoryId(r.categoryId());
        p.setSku(r.sku() == null || r.sku().isBlank() ? null : r.sku());
    }

    private ProductResponse toResponse(Product p) {
        List<ReviewResponse> reviews = p.getReviews().stream()
                .map(r -> new ReviewResponse(r.getUserId(), r.getRating(), r.getComment(), r.getCreatedAt()))
                .toList();
        return new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getStock(),
                p.getCategoryId(), p.getSku(), reviews);
    }
}
