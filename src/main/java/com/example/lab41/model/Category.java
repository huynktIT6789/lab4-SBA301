package com.example.lab41.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Category la collection rieng, Product tham chieu bang categoryId (manual reference).
 * MongoDB khong enforce foreign key -> Service chiu trach nhiem kiem tra ton tai.
 */
@Document(collection = "categories")
public class Category {

    @Id
    private String id;
    private String name;

    public Category() {
    }

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
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
}
