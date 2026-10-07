package com.example.lab41.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String name;

    // Luu dang decimal128 de khong mat do chinh xac (mac dinh BigDecimal -> String)
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    private Integer stock;

    // Phuc vu endpoint filter theo category
    @Indexed
    private String categoryId;

    // Business uniqueness. sparse=true de nhieu product khong co sku van hop le.
    @Indexed(unique = true, sparse = true)
    private String sku;

    // Embedded, bounded
    private List<Review> reviews = new ArrayList<>();

    public Product() {
    }

    public Product(String id, String name, BigDecimal price, Integer stock,
                   String categoryId, List<Review> reviews) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.reviews = reviews != null ? reviews : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
    }
}
