package com.expensetracker.service;

import com.expensetracker.model.Category;
import com.expensetracker.repository.CategoryRepository;

import java.util.List;

public class CategoryService {

    private final CategoryRepository repo = new CategoryRepository();

    public List<Category> listCategories(Long userId) {
        return repo.findAllByUserId(userId);
    }

    public Category addCategory(Long userId, String name) {
        name = ValidationService.validateCategoryName(name);

        if (repo.findByUserIdAndNameIgnoreCase(userId, name).isPresent())
            throw new IllegalArgumentException("Category already exists");

        Category cat = new Category(name, userId, false);
        return repo.save(cat);
    }

    public void renameCategory(Long userId, Integer categoryId, String newName) {
        newName = ValidationService.validateCategoryName(newName);

        Category cat = repo.findByUserIdAndNameIgnoreCase(userId, newName).orElse(null);
        if (cat != null && !cat.getId().equals(categoryId))
            throw new IllegalArgumentException("Category name already exists");

        // Load the existing category
        Category existing = repo.findAllByUserId(userId).stream()
            .filter(c -> c.getId().equals(categoryId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (existing.isDefaultCategory())
            throw new IllegalArgumentException("Cannot rename default category");

        existing.setName(newName);

        repo.update(existing); // <-- FIXED
    }


    public void deleteCategory(Long userId, Integer categoryId) {
        if (!repo.existsByIdAndUserId(categoryId, userId))
            throw new IllegalArgumentException("Category not found or not yours");

        // In real project: check if any expense uses it
        Category cat = new Category();
        cat.setId(categoryId);
        cat.setUserId(userId);
        repo.delete(cat);
    }
}