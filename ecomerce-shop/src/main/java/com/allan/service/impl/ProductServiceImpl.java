package com.allan.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.allan.exceptions.ProductException;
import com.allan.model.Category;
import com.allan.model.Product;
import com.allan.model.Seller;
import com.allan.repository.CategoryRepository;
import com.allan.repository.ProductRepository;
import com.allan.request.CreateProductRequest;
import com.allan.service.ProductService;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ─────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public Product creatProduct(CreateProductRequest req, Seller seller) {

        Category category1 = categoryRepository.findByCategoryId(req.getCategory());
        if (category1 == null) {
            Category category = new Category();
            category.setCategoryId(req.getCategory());
            category.setLevel(1);
            category1 = categoryRepository.save(category);
        }

        Category category2 = categoryRepository.findByCategoryId(req.getCategory2());
        if (category2 == null) {
            Category category = new Category();
            category.setCategoryId(req.getCategory2());
            category.setLevel(2);
            category.setParentCategory(category1);
            category2 = categoryRepository.save(category);
        }

        Category category3 = categoryRepository.findByCategoryId(req.getCategory3());
        if (category3 == null) {
            Category category = new Category();
            category.setCategoryId(req.getCategory3());
            category.setLevel(3);
            category.setParentCategory(category2);
            category3 = categoryRepository.save(category);
        }

        int discountPercentage = calculateDiscountPercentage(
                req.getMrpPrice(), req.getSellingPrice());

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category3);
        product.setDescription(req.getDescription());
        product.setCreatedAt(LocalDateTime.now());
        product.setTitle(req.getTitle());
        product.setColor(req.getColor());
        product.setSellingPrice(req.getSellingPrice());
        product.setImages(req.getImages());
        product.setMrpPrice(req.getMrpPrice());
        product.setSizes(req.getSizes());
        product.setDiscountPercent(discountPercentage);

        Product saved = productRepository.save(product);
        log.info("Product created: id={} title={} seller={}",
                saved.getId(), saved.getTitle(), seller.getId());
        return saved;
    }

    // ─────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Product findProductById(Long productId) throws ProductException {
        return productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new ProductException(
                        "Product not found with id: " + productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProduct(String query) {
        return productRepository.searchProduct(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(
            String category,
            String brand,
            String colors,
            String sizes,
            Integer minPrice,
            Integer maxPrice,
            Integer minDiscount,
            String sort,
            String stock,
            Integer pageNumber) {

        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ✅ JOIN FETCH on main select only — skip on count query
            // Long.class = count query, Product.class = main select
            if (query != null && !query.getResultType().equals(Long.class)) {
                root.fetch("category", JoinType.LEFT);
                root.fetch("seller", JoinType.LEFT);
                query.distinct(true); // ✅ prevent duplicate products from JOIN
            }

            // ✅ separate join for WHERE clause (fetch join can't be used in predicates)
            if (category != null && !category.isBlank()) {
                Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(
                        categoryJoin.get("categoryId"), category));
            }

            if (colors != null && !colors.isBlank()) {
                predicates.add(criteriaBuilder.isMember(colors, root.get("color")));
            }

            if (sizes != null && !sizes.isBlank()) {
                predicates.add(criteriaBuilder.isMember(sizes, root.get("sizes")));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("sellingPrice"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("sellingPrice"), maxPrice));
            }

            if (minDiscount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("discountPercent"), minDiscount));
            }

            if (stock != null && !stock.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("stock"), stock));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // ✅ safe sort with explicit default — prevents injection
        Pageable pageable = switch (sort != null ? sort.trim() : "") {
            case "price_low"  -> PageRequest.of(
                    pageNumber != null ? pageNumber : 0, 10,
                    Sort.by("sellingPrice").ascending());
            case "price_high" -> PageRequest.of(
                    pageNumber != null ? pageNumber : 0, 10,
                    Sort.by("sellingPrice").descending());
            case "discount"   -> PageRequest.of(
                    pageNumber != null ? pageNumber : 0, 10,
                    Sort.by("discountPercent").descending());
            case "newest"     -> PageRequest.of(
                    pageNumber != null ? pageNumber : 0, 10,
                    Sort.by("createdAt").descending());
            default           -> PageRequest.of(
                    pageNumber != null ? pageNumber : 0, 10,
                    Sort.by("createdAt").descending());
        };

        Page<Product> result = productRepository.findAll(spec, pageable);

        log.info("Products fetched: {} of {} total | category={} color={} page={}",
                result.getNumberOfElements(),
                result.getTotalElements(),
                category, colors, pageNumber);

        return result;
    }

    // ─────────────────────────────────────────────
    // UPDATE & DELETE
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteProduct(Long productId) throws ProductException {
        Product product = findProductById(productId);
        productRepository.delete(product);
        log.info("Product deleted: id={}", productId);
    }


    @Override
    @Transactional
    public Product updateProduct(Long productId, Product product) throws ProductException {
        findProductById(productId);
        product.setId(productId);
        Product updated = productRepository.save(product);
        log.info("Product updated: id={}", productId);
        return updated;
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private int calculateDiscountPercentage(int mrpPrice, int sellingPrice) {
        if (mrpPrice <= 0) {
            throw new IllegalArgumentException(
                    "MRP price must be greater than 0.");
        }
        if (sellingPrice > mrpPrice) {
            throw new IllegalArgumentException(
                    "Selling price cannot be greater than MRP price.");
        }
        double discount = mrpPrice - sellingPrice;
        double discountPercentage = (discount / mrpPrice) * 100;
        return (int) discountPercentage;
    }
}





// package com.allan.service.impl;

// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.allan.exceptions.ProductException;
// import com.allan.model.Category;
// import com.allan.model.Product;
// import com.allan.model.Seller;
// import com.allan.repository.CategoryRepository;
// import com.allan.repository.ProductRepository;
// import com.allan.request.CreateProductRequest;
// import com.allan.service.ProductService;


// import jakarta.persistence.criteria.Join;
// import jakarta.persistence.criteria.Predicate;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Service
// @RequiredArgsConstructor
// public class ProductServiceImpl implements ProductService {

//     private final ProductRepository productRepository;
//     private final CategoryRepository categoryRepository;

//     // ─────────────────────────────────────────────
//     // CREATE
//     // ─────────────────────────────────────────────

//     @Override
//     @Transactional
//     public Product creatProduct(CreateProductRequest req, Seller seller) {

//         Category category1 = categoryRepository.findByCategoryId(req.getCategory());
//         if (category1 == null) {
//             Category category = new Category();
//             category.setCategoryId(req.getCategory());
//             category.setLevel(1);
//             category1 = categoryRepository.save(category);
//         }

//         Category category2 = categoryRepository.findByCategoryId(req.getCategory2());
//         if (category2 == null) {
//             Category category = new Category();
//             category.setCategoryId(req.getCategory2());
//             category.setLevel(2);
//             category.setParentCategory(category1);
//             category2 = categoryRepository.save(category);
//         }

//         Category category3 = categoryRepository.findByCategoryId(req.getCategory3());
//         if (category3 == null) {
//             Category category = new Category();
//             category.setCategoryId(req.getCategory3());
//             category.setLevel(3);
//             category.setParentCategory(category2);
//             category3 = categoryRepository.save(category);
//         }

//         int discountPercentage = calculateDiscountPercentage(
//                 req.getMrpPrice(), req.getSellingPrice());

//         Product product = new Product();
//         product.setSeller(seller);
//         product.setCategory(category3);
//         product.setDescription(req.getDescription());
//         product.setCreatedAt(LocalDateTime.now());
//         product.setTitle(req.getTitle());
//         product.setColor(req.getColor());
//         product.setSellingPrice(req.getSellingPrice());
//         product.setImages(req.getImages());
//         product.setMrpPrice(req.getMrpPrice());
//         product.setSizes(req.getSizes());
//         product.setDiscountPercent(discountPercentage);

//         Product saved = productRepository.save(product);
//         log.info("Product created: id={} title={} seller={}",
//                 saved.getId(), saved.getTitle(), seller.getId());
//         return saved;
//     }

//     // ─────────────────────────────────────────────
//     // READ
//     // ─────────────────────────────────────────────

//     @Override
//     @Transactional(readOnly = true) // ✅ readOnly = true — skips dirty checking, improves read performance
//     public Product findProductById(Long productId) throws ProductException {
//         return productRepository.findByIdWithDetails(productId)
//                 .orElseThrow(() -> new ProductException(
//                         "Product not found with id: " + productId));
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public List<Product> searchProduct(String query) {
//         return productRepository.searchProduct(query);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public List<Product> getProductBySellerId(Long sellerId) {
//         return productRepository.findBySellerId(sellerId);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public Page<Product> getAllProducts(
//             String category,
//             String brand,
//             String colors,
//             String sizes,
//             Integer minPrice,
//             Integer maxPrice,
//             Integer minDiscount,
//             String sort,
//             String stock,
//             Integer pageNumber) {

//         // ✅ JPA Specification — your existing logic preserved exactly
//         Specification<Product> spec = (root, query, criteriaBuilder) -> {
//             List<Predicate> predicates = new ArrayList<>();

//             if (category != null && !category.isBlank()) {
//                 Join<Product, Category> categoryJoin = root.join("category");
//                 predicates.add(criteriaBuilder.equal(
//                         categoryJoin.get("categoryId"), category));
//             }

//             if (colors != null && !colors.isEmpty()) {
//                 predicates.add(criteriaBuilder.isMember(colors, root.get("color")));
//             }

//             if (sizes != null && !sizes.isEmpty()) {
//                 predicates.add(criteriaBuilder.isMember(sizes, root.get("sizes")));
//             }

//             if (minPrice != null) {
//                 predicates.add(criteriaBuilder.greaterThanOrEqualTo(
//                         root.get("sellingPrice"), minPrice));
//             }

//             if (maxPrice != null) {
//                 predicates.add(criteriaBuilder.lessThanOrEqualTo(
//                         root.get("sellingPrice"), maxPrice));
//             }

//             if (minDiscount != null) {
//                 predicates.add(criteriaBuilder.greaterThanOrEqualTo( // ✅ fixed: was lessThanOrEqualTo
//                         root.get("discountPercent"), minDiscount));
//             }

//             if (stock != null && !stock.isBlank()) {
//                 predicates.add(criteriaBuilder.equal(root.get("stock"), stock));
//             }

//             return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//         };

//         // ✅ sort safely with explicit default — prevents injection
//         Pageable pageable = switch (sort != null ? sort.trim() : "") {
//             case "price_low"  -> PageRequest.of(
//                     pageNumber != null ? pageNumber : 0, 10,
//                     Sort.by("sellingPrice").ascending());
//             case "price_high" -> PageRequest.of(
//                     pageNumber != null ? pageNumber : 0, 10,
//                     Sort.by("sellingPrice").descending());
//             case "discount"   -> PageRequest.of(
//                     pageNumber != null ? pageNumber : 0, 10,
//                     Sort.by("discountPercent").descending());
//             case "newest"     -> PageRequest.of(
//                     pageNumber != null ? pageNumber : 0, 10,
//                     Sort.by("createdAt").descending());
//             default           -> PageRequest.of(
//                     pageNumber != null ? pageNumber : 0, 10,
//                     Sort.by("createdAt").descending()); // ✅ sensible default: newest first
//         };

//         Page<Product> result = productRepository.findAll(spec, pageable);

//         log.info("Products fetched: {} of {} total | category={} color={} page={}",
//                 result.getNumberOfElements(),
//                 result.getTotalElements(),
//                 category, colors, pageNumber);

//         return result;
//     }

//     // ─────────────────────────────────────────────
//     // UPDATE & DELETE
//     // ─────────────────────────────────────────────

//     @Override
//     @Transactional
//     public void deleteProduct(Long productId) throws ProductException {
//         Product product = findProductById(productId);
//         productRepository.delete(product);
//         log.info("Product deleted: id={}", productId);
//     }

//     @Override
//     @Transactional
//     public Product updateProduct(Long productId, Product product) throws ProductException {
//         findProductById(productId); // ✅ confirms existence before update
//         product.setId(productId);
//         Product updated = productRepository.save(product);
//         log.info("Product updated: id={}", productId);
//         return updated;
//     }

//     // ─────────────────────────────────────────────
//     // HELPERS
//     // ─────────────────────────────────────────────

//     private int calculateDiscountPercentage(int mrpPrice, int sellingPrice) {
//         if (mrpPrice <= 0) {
//             throw new IllegalArgumentException(
//                     "MRP price must be greater than 0.");
//         }
//         if (sellingPrice > mrpPrice) {
//             throw new IllegalArgumentException(
//                     "Selling price cannot be greater than MRP price.");
//         }
//         double discount = mrpPrice - sellingPrice;
//         double discountPercentage = (discount / mrpPrice) * 100;
//         return (int) discountPercentage;
//     }
// }



// // package com.allan.service.impl;

// // import java.time.LocalDateTime;
// // import java.util.ArrayList;
// // import java.util.List;

// // import org.springframework.data.domain.Page;
// // import org.springframework.data.domain.PageRequest;
// // import org.springframework.data.domain.Pageable;
// // import org.springframework.data.domain.Sort;
// // import org.springframework.data.jpa.domain.Specification;
// // import org.springframework.stereotype.Service;

// // import com.allan.exceptions.ProductException;
// // import com.allan.model.Category;
// // import com.allan.model.Product;
// // import com.allan.model.Seller;
// // import com.allan.repository.CategoryRepository;
// // import com.allan.repository.ProductRepository;
// // import com.allan.request.CreateProductRequest;
// // import com.allan.service.ProductService;

// // import jakarta.persistence.criteria.Join;
// // import jakarta.persistence.criteria.Predicate;
// // import lombok.RequiredArgsConstructor;

// // @Service
// // @RequiredArgsConstructor
// // public class ProductServiceImpl implements ProductService {

// //     private final ProductRepository productRepository;
// //     private final CategoryRepository categoryRepository;

// //     @Override
// //     public Product creatProduct(CreateProductRequest req, Seller seller) {

// //         Category category1 = categoryRepository.findByCategoryId(req.getCategory());
// //         if (category1 == null) {
// //             Category category = new Category();
// //             category.setCategoryId(req.getCategory());
// //             category.setLevel(1);
// //             category1 = categoryRepository.save(category);

// //         }
// //         Category category2 = categoryRepository.findByCategoryId(req.getCategory2());

// //         if (category2 == null) {
// //             Category category = new Category();
// //             category.setCategoryId(req.getCategory2());
// //             category.setLevel(2);
// //             category.setParentCategory(category1);
// //             category2 = categoryRepository.save(category);
// //         }
// //         Category category3 = categoryRepository.findByCategoryId(req.getCategory3());

// //         if (category3 == null) {
// //             Category category = new Category();
// //             category.setCategoryId(req.getCategory3());
// //             category.setLevel(3);
// //             category.setParentCategory(category2);
// //             category3 = categoryRepository.save(category);
// //         }

// //         // Calculating Discount
// //         int discountPercentage = calculateDiscountPercentage(req.getMrpPrice(), req.getSellingPrice());

// //         Product product = new Product();
// //         product.setSeller(seller);
// //         product.setCategory(category3);
// //         product.setDescription(req.getDescription());
// //         product.setCreatedAt(LocalDateTime.now());
// //         product.setTitle(req.getTitle());
// //         product.setColor(req.getColor());
// //         product.setSellingPrice(req.getSellingPrice());
// //         product.setImages(req.getImages());
// //         product.setMrpPrice(req.getMrpPrice());
// //         product.setSizes(req.getSizes());
// //         product.setDiscountPercent(discountPercentage);
// //         return productRepository.save(product);
// //     }

// //     private int calculateDiscountPercentage(int mrpPrice, int sellingPrice) {
// //         if (mrpPrice <= 0) {
// //             throw new IllegalArgumentException("Actual price must be greater than 0..");
// //         }
// //         double discount = mrpPrice - sellingPrice;
// //         double discountPercentage = (discount / mrpPrice) * 100;

// //         return (int) discountPercentage;

// //     }

// //     @Override
// //     public void deleteProduct(Long productId) throws ProductException {
// //         Product product = findProductById(productId);
// //         productRepository.delete(product);
// //     }

// //     @Override
// //     public Product updateProduct(Long productId, Product product) throws ProductException {
// //         findProductById(productId);
// //         product.setId(productId);

// //         return productRepository.save(product);
// //     }

// //     @Override
// //     public Product findProductById(Long productId) throws ProductException {

// //         return productRepository.findById(productId)
// //                 .orElseThrow(() -> new ProductException("Product not found with id" + productId));
// //     }

// //     @Override
// //     public List<Product> searchProduct(String query) {
// //         return productRepository.searchProduct(query);
// //     }

// //     @Override
// //     public List<Product> getProductBySellerId(Long sellerId) {
// //         return productRepository.findBySellerId(sellerId);
// //     }

// //     @Override
// //     public Page<Product> getAllProducts(String category, String brand, String colors, String sizes, Integer minPrice,
// //             Integer maxPrice, Integer minDiscount, String sort, String stock, Integer pageNumber) {

// //         Specification<Product> spec = (root, query, criteriaBuilder) -> {
// //             List<Predicate> predicates = new ArrayList<>();
// //             if (category != null) {
// //                 Join<Product, Category> categoryJoin = root.join("category");
// //                 predicates.add(criteriaBuilder.equal(categoryJoin.get("categoryId"), category));

// //             }
// //             if (colors != null && !colors.isEmpty()) {
// //                 System.out.println(colors);
// //                 predicates.add(criteriaBuilder.isMember(colors, root.get("color")));
// //             }

// //             // filter by size(Single Value)

// //             if (sizes != null && !sizes.isEmpty()) {
// //                 predicates.add(criteriaBuilder.isMember(sizes, root.get("sizes")));
// //             }

// //             if (minPrice != null) {
// //                 predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
// //             }

// //             if (maxPrice != null) {
// //                 predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
// //             }

// //             if (minDiscount != null) {
// //                 predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("discountPercent"), minDiscount));
// //             }
// //             if (stock != null) {
// //                 predicates.add(criteriaBuilder.equal(root.get("stock"), stock));
// //             }
// //             return criteriaBuilder.and(predicates.toArray(new Predicate[0]));

// //         };
// //         Pageable pageable;
// //         if (sort != null && !sort.isEmpty()) {
// //             pageable = switch (sort) {
// //                 case "price_low" ->
// //                     PageRequest.of(pageNumber != null ? pageNumber : 0, 10,
// //                             Sort.by("sellingPrice").ascending());

// //                 case "price_high" ->
// //                     PageRequest.of(pageNumber != null ? pageNumber : 0, 10,
// //                             Sort.by("sellingPrice").descending());

// //                 default ->
// //                     PageRequest.of(pageNumber != null ? pageNumber : 0, 10,
// //                             Sort.unsorted());

// //             };
// //         } else {
// //             pageable = PageRequest.of(pageNumber != null ? pageNumber : 0, 10, Sort.unsorted());
// //         }
// //         return productRepository.findAll(spec, pageable);

// //     }

// // }
