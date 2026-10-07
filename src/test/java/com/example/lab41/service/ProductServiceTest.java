package com.example.lab41.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.lab41.dto.PageResponse;
import com.example.lab41.dto.ProductRequest;
import com.example.lab41.dto.ProductResponse;
import com.example.lab41.dto.ReviewRequest;
import com.example.lab41.exception.ResourceNotFoundException;
import com.example.lab41.model.Product;
import com.example.lab41.repository.CategoryRepository;
import com.example.lab41.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository products;
    @Mock CategoryRepository categories;
    @InjectMocks ProductService service;

    private Product sample() {
        return new Product("p1", "Keyboard", new BigDecimal("120"), 10, "c1", new ArrayList<>());
    }

    // #65 Create valid product
    @Test
    void create_shouldSaveAndReturnResponse() {
        when(categories.existsById("c1")).thenReturn(true);
        when(products.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId("p1");
            return p;
        });

        ProductResponse res = service.create(
                new ProductRequest("Keyboard", new BigDecimal("120"), 10, "c1"));

        assertThat(res.id()).isEqualTo("p1");
        assertThat(res.name()).isEqualTo("Keyboard");
        assertThat(res.categoryId()).isEqualTo("c1");
        assertThat(res.reviews()).isEmpty();
    }

    // #67 Reject missing category
    @Test
    void create_shouldRejectMissingCategory() {
        when(categories.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.create(
                new ProductRequest("Keyboard", new BigDecimal("120"), 10, "missing")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
        verify(products, never()).save(any());
    }

    @Test
    void create_withoutCategory_shouldSkipCategoryCheck() {
        when(products.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(new ProductRequest("Mouse", new BigDecimal("10"), 1, null));

        verify(categories, never()).existsById(any());
    }

    // #68 Find existing product
    @Test
    void findById_shouldReturnProduct() {
        when(products.findById("p1")).thenReturn(Optional.of(sample()));

        assertThat(service.findById("p1").name()).isEqualTo("Keyboard");
    }

    // #69 404 for missing product
    @Test
    void findById_shouldThrowWhenMissing() {
        when(products.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("nope"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    // #70 Update existing product
    @Test
    void update_shouldChangeFields() {
        when(products.findById("p1")).thenReturn(Optional.of(sample()));
        when(categories.existsById("c2")).thenReturn(true);
        when(products.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse res = service.update("p1",
                new ProductRequest("Keyboard Pro", new BigDecimal("150"), 5, "c2", "SKU-9"));

        assertThat(res.name()).isEqualTo("Keyboard Pro");
        assertThat(res.price()).isEqualByComparingTo("150");
        assertThat(res.stock()).isEqualTo(5);
        assertThat(res.categoryId()).isEqualTo("c2");
        assertThat(res.sku()).isEqualTo("SKU-9");
    }

    @Test
    void update_shouldThrowWhenProductMissing() {
        when(products.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("nope",
                new ProductRequest("x", BigDecimal.ONE, 1, null)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(products, never()).save(any());
    }

    // #71 Delete existing product
    @Test
    void delete_shouldRemoveProduct() {
        Product p = sample();
        when(products.findById("p1")).thenReturn(Optional.of(p));

        service.delete("p1");

        verify(products).delete(p);
    }

    @Test
    void delete_shouldThrowWhenMissing() {
        when(products.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(products, never()).delete(any(Product.class));
    }

    // #72 Search by keyword
    @Test
    void search_byKeyword_shouldUseKeywordQuery() {
        when(products.findByNameContainingIgnoreCase(eq("key"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sample())));

        PageResponse<ProductResponse> res = service.search("key", null, 0, 10, "name", "asc");

        assertThat(res.content()).hasSize(1);
        verify(products).findByNameContainingIgnoreCase(eq("key"), any(Pageable.class));
    }

    // #73 Filter by category
    @Test
    void search_byCategory_shouldUseCategoryQuery() {
        when(products.findByCategoryId(eq("c1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sample())));

        PageResponse<ProductResponse> res = service.search("", "c1", 0, 10, "price", "desc");

        assertThat(res.totalElements()).isEqualTo(1);
        verify(products).findByCategoryId(eq("c1"), any(Pageable.class));
    }

    @Test
    void search_keywordAndCategory_shouldUseCombinedQuery() {
        when(products.findByNameContainingIgnoreCaseAndCategoryId(eq("key"), eq("c1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sample())));

        service.search("key", "c1", 0, 10, "name", "asc");

        verify(products).findByNameContainingIgnoreCaseAndCategoryId(eq("key"), eq("c1"), any(Pageable.class));
    }

    @Test
    void search_shouldClampPageSizeAndApplySort() {
        when(products.findAll(any(Pageable.class))).thenReturn(Page.empty());

        service.search("", null, -3, 9999, "price", "desc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(products).findAll(captor.capture());
        Pageable used = captor.getValue();
        assertThat(used.getPageNumber()).isZero();
        assertThat(used.getPageSize()).isEqualTo(ProductService.MAX_PAGE_SIZE);
        assertThat(used.getSort().getOrderFor("price").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    // Break-It #41: sort field ngoai whitelist
    @Test
    void search_shouldRejectUnsupportedSortField() {
        assertThatThrownBy(() -> service.search("", null, 0, 10, "password", "asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported sort field");
    }

    // #74 Add embedded review
    @Test
    void addReview_shouldAppendEmbeddedReview() {
        Product p = sample();
        when(products.findById("p1")).thenReturn(Optional.of(p));
        when(products.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse res = service.addReview("p1", new ReviewRequest("u1", 5, "Great"));

        assertThat(res.reviews()).hasSize(1);
        assertThat(res.reviews().get(0).userId()).isEqualTo("u1");
        assertThat(res.reviews().get(0).rating()).isEqualTo(5);
        assertThat(res.reviews().get(0).createdAt()).isNotNull();
    }

    @Test
    void addReview_shouldThrowWhenProductMissing() {
        when(products.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addReview("nope", new ReviewRequest("u1", 5, "x")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
