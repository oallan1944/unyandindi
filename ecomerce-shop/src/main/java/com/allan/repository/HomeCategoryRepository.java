package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.HomeCategory;

public interface HomeCategoryRepository extends JpaRepository<HomeCategory, Long> {

}
