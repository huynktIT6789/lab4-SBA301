package com.example.lab41.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.lab41.dto.CategoryRequest;
import com.example.lab41.dto.CategoryResponse;
import com.example.lab41.exception.ConflictException;
import com.example.lab41.exception.ResourceNotFoundException;
import com.example.lab41.model.Category;
import com.example.lab41.repository.CategoryRepository;
import com.example.lab41.repository.ProductRepository;

@Service
public class CategoryService {

    private final CategoryRepository categories;
    private final ProductRepository products;

    public CategoryService(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    public List<CategoryResponse> findAll() {
        return categories.findAll().stream().map(this::toResponse).toList();
    }

    public CategoryResponse create(CategoryRequest request) {
        return toResponse(categories.save(new Category(null, request.name())));
    }

    /**
     * Chan xoa Category dang duoc Product tham chieu de tranh dangling reference
     * (Break-It #43: bo check nay de quan sat dangling categoryId).
     */
    public void delete(String id) {
        if (!categories.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        if (products.existsByCategoryId(id)) {
            throw new ConflictException("Category is still referenced by products: " + id);
        }
        categories.deleteById(id);
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName());
    }
}
