package com.example.lab41.config;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.lab41.model.Category;
import com.example.lab41.model.Product;
import com.example.lab41.repository.CategoryRepository;
import com.example.lab41.repository.ProductRepository;

@Configuration
public class DataSeeder {

    /** Seed Category truoc, Product sau (Product giu categoryId that). */
    @Bean
    @ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
    CommandLineRunner seed(CategoryRepository categories, ProductRepository products) {
        return args -> {
            if (categories.count() == 0) {
                Category accessories = categories.save(new Category(null, "Accessories"));
                Product keyboard = new Product(
                        null,
                        "Mechanical Keyboard",
                        new BigDecimal("120.00"),
                        20,
                        accessories.getId(),
                        new ArrayList<>());
                keyboard.setSku("KB-001");
                products.save(keyboard);
            }
        };
    }
}
