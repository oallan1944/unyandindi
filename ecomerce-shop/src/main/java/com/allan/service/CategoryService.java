package com.allan.service;

import java.util.List;

import com.allan.model.Category;

public interface CategoryService {
    Category createCategory(Category category) throws Exception;
    List<Category> getAllCategories();
    Category updateCategory(Long id, Category category) throws Exception;
}