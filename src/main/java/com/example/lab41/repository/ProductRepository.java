package com.example.lab41.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.lab41.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Product> findByCategoryId(String categoryId, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndCategoryId(
            String keyword, String categoryId, Pageable pageable);

    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<Product> findByStockGreaterThan(Integer stock);

    boolean existsByCategoryId(String categoryId);
}
