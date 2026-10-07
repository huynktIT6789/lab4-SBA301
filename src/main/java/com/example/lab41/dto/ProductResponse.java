package com.example.lab41.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        String id,
        String name,
        BigDecimal price,
        Integer stock,
        String categoryId,
        String sku,
        List<ReviewResponse> reviews
) {
    /** Constructor 6 tham so giu dung contract trong lab guide (sku = null). */
    public ProductResponse(String id, String name, BigDecimal price, Integer stock,
                           String categoryId, List<ReviewResponse> reviews) {
        this(id, name, price, stock, categoryId, null, reviews);
    }
}
