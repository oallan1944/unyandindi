package com.allan.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.allan.model.Category;
import com.allan.repository.CategoryRepository;
import com.allan.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category createCategory(Category category) throws Exception {
        if (category.getCategoryId() == null || category.getCategoryId().isBlank()) {
            throw new Exception("categoryId is required.");
        }
        if (categoryRepository.existsByCategoryId(category.getCategoryId())) {
            throw new Exception("Category with categoryId '" + category.getCategoryId() + "' already exists.");
        }
        // never trust a client-supplied id — this is always a create
        category.setId(null);
        Category saved = categoryRepository.save(category);
        log.info("Category created: {}", saved.getCategoryId());
        return saved;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category category) throws Exception {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new Exception("Category not found with id: " + id));

        if (category.getName() != null) existing.setName(category.getName());
        if (category.getLevel() != null) existing.setLevel(category.getLevel());
        if (category.getParentCategory() != null) existing.setParentCategory(category.getParentCategory());
        // categoryId is intentionally NOT updatable here — changing it would
        // silently break the HomeCategory <-> Product matching in HomeServiceImpl.
        // Delete and recreate if it genuinely needs to change.

        Category updated = categoryRepository.save(existing);
        log.info("Category {} updated", id);
        return updated;
    }
}