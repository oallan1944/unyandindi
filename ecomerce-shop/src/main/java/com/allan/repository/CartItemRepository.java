package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.Cart;
import com.allan.model.CartItem;
import com.allan.model.Product;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartAndProductAndSize(Cart cart, Product product, String size);

}
