package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByCategoryId(String categoryId);

}
