package com.allan.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.allan.exceptions.ProductException;
import com.allan.model.Product;
import com.allan.model.Seller;
import com.allan.request.CreateProductRequest;

public interface ProductService {

    public Product creatProduct(CreateProductRequest req, Seller seller);

    public void deleteProduct(Long productId) throws ProductException;

    public Product updateProduct(Long productId, Product product) throws ProductException;

    Product findProductById(Long productId) throws ProductException;

    List<Product> searchProduct(String query);

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
            Integer pageNumber

    );

    List<Product> getProductBySellerId(Long sellerId);
}
