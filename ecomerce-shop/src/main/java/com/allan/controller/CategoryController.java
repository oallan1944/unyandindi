package com.allan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.allan.model.Category;
import com.allan.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Admin-only category management. Covered at the URL level by
 * SecurityConfig's "/api/admin/**" -> ROLE_ADMIN matcher; @PreAuthorize here
 * is a second, independent layer (requires @EnableMethodSecurity on
 * SecurityConfig — see the earlier security guide) so this stays protected
 * even if the URL prefix ever moves.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) throws Exception {
        Category created = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category) throws Exception {
        return ResponseEntity.ok(categoryService.updateCategory(id, category));
    }
}
