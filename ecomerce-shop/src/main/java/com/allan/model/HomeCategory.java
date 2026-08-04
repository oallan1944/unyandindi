package com.allan.model;

import java.util.ArrayList;
import java.util.List;

import com.allan.domain.HomeCategorySection;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class HomeCategory {   // <-- must be "public class", not just "class"
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String image;

    private String categoryId;

    private HomeCategorySection section;

    // In-memory only — populated by HomeServiceImpl when building the
    // homepage response, never persisted to the home_category table.
    // Powers the "Buy now" button on the frontend.
    @Transient
    private List<Product> products = new ArrayList<>();
}