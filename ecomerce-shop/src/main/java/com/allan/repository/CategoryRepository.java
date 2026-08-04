package com.allan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.Category;


public interface CategoryRepository extends JpaRepository<Category, Long> {

    // unchanged — kept exactly as you already have it, so any existing
    // callers elsewhere in the codebase keep compiling without changes
    Category findByCategoryId(String categoryId);

    // added — CategoryServiceImpl.createCategory() uses this to check for
    // duplicates before insert, cheaper than fetching the full row just to
    // check if it exists
    boolean existsByCategoryId(String categoryId);

    // added — not used yet by CategoryServiceImpl as written, but useful
    // once you build a category tree/browse UI (top-level categories only)
    List<Category> findByParentCategoryIsNull();

}