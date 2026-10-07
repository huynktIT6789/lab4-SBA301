package com.example.lab41.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.lab41.model.Product;
import com.example.lab41.model.Review;

/**
 * Persistence integration test (thay cho @DataJpaTest).
 * Can Docker. Neu may khong co Docker, test tu bo qua (disabledWithoutDocker).
 * Chay: mvn test (tu bo qua neu khong co Docker)
 */
@DataMongoTest(properties = {
        "spring.data.mongodb.auto-index-creation=true",
        "app.seed.enabled=false"
})
@Testcontainers(disabledWithoutDocker = true)
class ProductRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:8.0"));

    @Autowired ProductRepository products;

    @BeforeEach
    void clean() {
        products.deleteAll();
    }

    private Product product(String name, String price, int stock, String categoryId) {
        return new Product(null, name, new BigDecimal(price), stock, categoryId, new ArrayList<>());
    }

    @Test
    void derivedQuery_nameContainingIgnoreCase() {
        products.save(product("Mechanical Keyboard", "120.00", 20, "c1"));
        products.save(product("Mouse", "30.00", 50, "c1"));

        Page<Product> page = products.findByNameContainingIgnoreCase("KEYBOARD", PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Product::getName).containsExactly("Mechanical Keyboard");
    }

    @Test
    void derivedQuery_categoryAndPagingSorting() {
        products.save(product("A", "30.00", 1, "c1"));
        products.save(product("B", "10.00", 1, "c1"));
        products.save(product("C", "20.00", 1, "c2"));

        Page<Product> page = products.findByCategoryId("c1",
                PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "price")));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Product::getName).containsExactly("B", "A");
    }

    @Test
    void derivedQuery_priceBetweenAndStock() {
        products.save(product("Cheap", "10.00", 0, "c1"));
        products.save(product("Mid", "100.00", 5, "c1"));
        products.save(product("Pricey", "500.00", 5, "c1"));

        assertThat(products.findByPriceBetween(new BigDecimal("50"), new BigDecimal("200")))
                .extracting(Product::getName).containsExactly("Mid");
        assertThat(products.findByStockGreaterThan(0))
                .extracting(Product::getName).containsExactlyInAnyOrder("Mid", "Pricey");
    }

    @Test
    void bsonMapping_roundTripsPriceAndEmbeddedReview() {
        Product p = product("Keyboard", "120.50", 3, "c1");
        p.getReviews().add(new Review("u1", 5, "Great", Instant.parse("2026-10-07T00:00:00Z")));
        Product saved = products.save(p);

        Product loaded = products.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getPrice()).isEqualByComparingTo("120.50");
        assertThat(loaded.getReviews()).hasSize(1);
        assertThat(loaded.getReviews().get(0).getUserId()).isEqualTo("u1");
        assertThat(loaded.getReviews().get(0).getCreatedAt()).isEqualTo(Instant.parse("2026-10-07T00:00:00Z"));
    }

    // #76 / Break-It #44: unique sku
    @Test
    void uniqueSkuIndex_shouldRejectDuplicate() {
        Product a = product("A", "1.00", 1, "c1");
        a.setSku("SKU-1");
        products.save(a);

        Product b = product("B", "2.00", 1, "c1");
        b.setSku("SKU-1");

        assertThatThrownBy(() -> products.save(b)).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void uniqueSkuIndex_isSparse_multipleNullSkuAllowed() {
        products.save(product("A", "1.00", 1, "c1"));
        products.save(product("B", "2.00", 1, "c1"));

        assertThat(products.count()).isEqualTo(2);
    }

    @Test
    void existsByCategoryId_shouldDetectReference() {
        products.save(product("A", "1.00", 1, "c1"));

        assertThat(products.existsByCategoryId("c1")).isTrue();
        assertThat(products.existsByCategoryId("zzz")).isFalse();
    }
}
