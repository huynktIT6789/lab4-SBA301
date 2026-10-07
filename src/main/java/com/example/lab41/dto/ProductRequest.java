package com.example.lab41.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequest(
        @NotBlank String name,
        @NotNull @PositiveOrZero BigDecimal price,
        @NotNull @PositiveOrZero Integer stock,
        String categoryId,
        String sku
) {
    /** Constructor 4 tham so giu dung contract trong lab guide (sku = null). */
    public ProductRequest(String name, BigDecimal price, Integer stock, String categoryId) {
        this(name, price, stock, categoryId, null);
    }
}
