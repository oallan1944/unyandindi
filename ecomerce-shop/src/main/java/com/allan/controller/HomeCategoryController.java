package com.allan.controller;

import org.springframework.web.bind.annotation.RestController;

import com.allan.model.Home;
import com.allan.model.HomeCategory;
import com.allan.service.HomeCategoryService;
import com.allan.service.HomeService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
public class HomeCategoryController {

    private final HomeCategoryService homeCategoryService;
    private final HomeService homeService;

    // ── Customer: fetch home page data ──────────────
    @GetMapping("/home")
    public ResponseEntity<Home> getHomePageData() {
        List<HomeCategory> categories = homeCategoryService.getAllHomeCategories();
        Home home = homeService.createHomePageData(categories);
        return new ResponseEntity<>(home, HttpStatus.ACCEPTED);
    }

    // ── Admin: seed/create home categories ──────────
    @PostMapping("/home/categories")
    public ResponseEntity<Home> createHomeCategories(
            @RequestBody List<HomeCategory> homeCategories) {
        List<HomeCategory> categories = homeCategoryService.createCategories(homeCategories);
        Home home = homeService.createHomePageData(categories);
        return new ResponseEntity<>(home, HttpStatus.ACCEPTED);
    }

    // ── Admin: create a single home category tile ───
    /**
     * Adds one new tile to a homepage section (GRID, SHOP_BY_CATEGORIES,
     * ELECTRIC_CATEGORIES, or DEALS — {@code section} is part of the request
     * body, so this one endpoint serves every section rather than needing
     * one per section).
     *
     * <p>Distinct from {@link #createHomeCategories}, which only seeds when
     * the table is empty — this is the ongoing "add one more tile" path an
     * admin needs after initial setup.
     */

     @PostMapping("/admin/home-category")
    public ResponseEntity<HomeCategory> createSingleHomeCategory(
            @RequestBody HomeCategory homeCategory) throws Exception {
        if (homeCategory.getName() == null || homeCategory.getName().isBlank()) {
            throw new Exception("Home category name is required.");
        }
        if (homeCategory.getSection() == null) {
            throw new Exception("Home category section is required.");
        }
        // id is never trusted from the request body — this is always a
        // create, never an accidental overwrite of an existing row.
        homeCategory.setId(null);
 
        HomeCategory saved = homeCategoryService.createHomeCategory(homeCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
 

    // ── Admin: get all home categories ──────────────
    @GetMapping("/admin/home-category")
    public ResponseEntity<List<HomeCategory>> getHomeCategory() {
        List<HomeCategory> categories = homeCategoryService.getAllHomeCategories();
        return ResponseEntity.ok(categories);
    }

    // ── Admin: update a home category ───────────────
    @PatchMapping("/admin/home-category/{id}")
    public ResponseEntity<HomeCategory> updateHomeCategory(
            @PathVariable Long id,
            @RequestBody HomeCategory homeCategory) throws Exception {
        HomeCategory updatedCategory = homeCategoryService.updateHomeCategory(homeCategory, id);
        return ResponseEntity.ok(updatedCategory);
    }
}