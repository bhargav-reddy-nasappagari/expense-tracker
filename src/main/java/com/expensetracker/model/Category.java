package com.expensetracker.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Category {

    private Integer id;                    // INT AUTO_INCREMENT PRIMARY KEY
    private String name;                   // VARCHAR(50) NOT NULL
    private Long userId;                   // BIGINT NOT NULL → references users.id
    private boolean defaultCategory;             // BOOLEAN NOT NULL DEFAULT FALSE
    private LocalDateTime createdAt;       // TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    private LocalDateTime updatedAt;       // TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

    // ====================== Constructors ======================
    public Category() {}

    public Category(String name, Long userId, boolean defaultCategory) {
        this.name = name;
        this.userId = userId;
        this.defaultCategory = defaultCategory;
    }

    // ====================== Getters & Setters ======================

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) name = name.trim();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isDefaultCategory() {
        return defaultCategory;
    }

    // Protected defaults cannot be changed to false once true
    public void setDefaultCategory(boolean defaultCategory) {
        if (!defaultCategory && this.defaultCategory) {
            throw new IllegalStateException("Default categories cannot be unmarked as default");
        }
        this.defaultCategory = defaultCategory;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ====================== toString, equals, hashCode ======================

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", userId=" + userId +
                ", isDefault=" + defaultCategory +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}