package com.allan.repository;

import com.allan.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {  // ✅ keeps Specification support for getAllProducts

    // ─────────────────────────────────────────────
    // SELLER QUERIES
    // ─────────────────────────────────────────────

    // ✅ JOIN FETCH seller and category — prevents N+1 when listing seller products
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.seller " +
           "WHERE p.seller.id = :sellerId")
    List<Product> findBySellerId(@Param("sellerId") Long sellerId);

    long countBySellerId(Long sellerId);

    // ─────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────

    // ✅ searches title and category name
    // ✅ JOIN FETCH prevents N+1 on search results
    // ✅ DISTINCT prevents duplicate results from JOIN
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category c " +
           "LEFT JOIN FETCH p.seller s " +
           "WHERE (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "OR (:query IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProduct(@Param("query") String query);

    // ─────────────────────────────────────────────
    // SINGLE PRODUCT
    // ─────────────────────────────────────────────

    // ✅ JOIN FETCH on single product — avoids lazy load on category/seller
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.seller " +
           "WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    // ─────────────────────────────────────────────
    // FILTERED PAGE (used by getAllProducts Specification)
    // ─────────────────────────────────────────────

    // ✅ JOIN FETCH eliminates N+1 on category and seller
    // ✅ Dynamic null-safe filters — passing null skips the filter
    // ✅ Separate countQuery — JOIN FETCH in main query breaks count, must be separate
    @Query(value = """
            SELECT DISTINCT p FROM Product p
            LEFT JOIN FETCH p.category c
            LEFT JOIN FETCH p.seller s
            WHERE (:category IS NULL OR c.categoryId = :category)
            AND (:color IS NULL OR :color = '' OR LOWER(p.color) = LOWER(:color))
            AND (:minPrice IS NULL OR p.sellingPrice >= :minPrice)
            AND (:maxPrice IS NULL OR p.sellingPrice <= :maxPrice)
            AND (:minDiscount IS NULL OR p.discountPercent >= :minDiscount)
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p) FROM Product p
            LEFT JOIN p.category c
            WHERE (:category IS NULL OR c.categoryId = :category)
            AND (:color IS NULL OR :color = '' OR LOWER(p.color) = LOWER(:color))
            AND (:minPrice IS NULL OR p.sellingPrice >= :minPrice)
            AND (:maxPrice IS NULL OR p.sellingPrice <= :maxPrice)
            AND (:minDiscount IS NULL OR p.discountPercent >= :minDiscount)
            """)
    Page<Product> findWithFilters(
            @Param("category") String category,
            @Param("color") String color,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("minDiscount") Integer minDiscount,
            Pageable pageable);

       Optional<Product> findFirstByCategory_CategoryIdOrderByCreatedAtDesc(String categoryId);
}







// package com.allan.repository;

// import java.util.List;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import com.allan.model.Product;

// public interface ProductRepository extends JpaRepository<Product, Long>,
//                 JpaSpecificationExecutor<Product> {

//         List<Product> findBySellerId(Long id);

//         @Query("SELECT p FROM Product p where(:query is null or lower(p.title)" +
//                         "LIKE lower(concat('%', :query, '%') ) ) "+
//                         "or (:query is null or lower(p.category.name)"+
//                         "LIKE lower(concat('%', :query, '%') ) ) ")
//         List<Product> searchProduct(@Param("query") String query);
// }
