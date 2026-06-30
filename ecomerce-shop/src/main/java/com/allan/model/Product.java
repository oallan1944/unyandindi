package com.allan.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product", indexes = {
        @Index(name = "idx_product_seller_id",   columnList = "seller_id"),
        @Index(name = "idx_product_category_id", columnList = "category_id"),
        @Index(name = "idx_product_created_at",  columnList = "created_at")
})
public class Product {

    // ── Views ────────────────────────────────────────
    // Controls which fields are serialized per context
    public static class ListView {}      // product listing — minimal fields
    public static class DetailView extends ListView {} // product detail — all fields

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView(ListView.class)
    private Long id;

    @JsonView(ListView.class)
    private String title;

    @JsonView(DetailView.class) // ✅ description only on detail page
    private String description;

    @JsonView(ListView.class)
    private int mrpPrice;

    @JsonView(ListView.class)
    private int sellingPrice;

    @JsonView(ListView.class)
    private int discountPercent;

    @JsonView(ListView.class)
    private int quantity;

    @JsonView(ListView.class)
    private String color;

    @ElementCollection(fetch = FetchType.EAGER)
    @BatchSize(size = 30)
    @JsonView(ListView.class)
    private List<String> images = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @BatchSize(size = 30)
    @JsonView(DetailView.class) // ✅ colors only needed on detail page
    private List<String> colors = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @BatchSize(size = 30)
    @JsonView(DetailView.class) // ✅ sizes only needed on detail page
    private List<String> sizes = new ArrayList<>();

    @JsonView(ListView.class)
    private int numRatings;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonView(ListView.class)
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonView(ListView.class)
    private Seller seller;

    @JsonView(ListView.class)
    private LocalDateTime createdAt;

    // ✅ @JsonIgnore — reviews never needed on listing page
    // ✅ fetched separately via GET /reviews/{productId}
    // ✅ @BatchSize — if ever fetched, loads in batches not N+1
    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 30)
    private List<Review> reviews = new ArrayList<>();
}



// package com.allan.model;

// import lombok.*;

// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;

// import jakarta.persistence.CascadeType;
// import jakarta.persistence.ElementCollection;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.OneToMany;
// import jakarta.persistence.Table;
// import jakarta.persistence.Index;
// import org.hibernate.annotations.BatchSize;
// import jakarta.persistence.FetchType;


// @Entity
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Table(name = "product", indexes = {
//        @Index(name = "idx_product_seller_id",   columnList = "seller_id"),
//         @Index(name = "idx_product_category_id", columnList = "category_id"),
//         @Index(name = "idx_product_created_at",  columnList = "createdAt")
// })
// public class Product {

//     @Id
//     @GeneratedValue(strategy = GenerationType.AUTO)
//     private Long id;

//     private String title;

//     private String description;

//     private int mrpPrice;

//     private int sellingPrice;

//     private int discountPercent;

//     private int quantity;

//     private String color;
//     // Element collection creates a table for images
//     @ElementCollection (fetch = FetchType.EAGER)
//     @BatchSize(size = 30)
//     private List<String> images = new ArrayList<>();

//     @ElementCollection(fetch = FetchType.EAGER )
//     @BatchSize(size = 30)
//     private List<String> colors = new ArrayList<>(); // updated from color

//     @ElementCollection(fetch = FetchType.EAGER)
//     @BatchSize(size = 30)
//     private List<String> sizes = new ArrayList<>(); // updated from sizes

//     private int numRatings;

//     // A category will have many products
//     // A product will only belong to one category
//     @ManyToOne (fetch = FetchType.EAGER)
//     private Category category;

//     // many sellers can have the same product
//     @ManyToOne(fetch = FetchType.EAGER)
//     private Seller seller;

//     private LocalDateTime createdAt;

//     //private String sizes;

//     @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//     @BatchSize(size = 30)
//     private List<Review> reviews = new ArrayList<>();

// }
