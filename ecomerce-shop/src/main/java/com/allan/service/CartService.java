package com.allan.service;

import com.allan.model.Cart;
import com.allan.model.CartItem;
import com.allan.model.Product;
import com.allan.model.User;

public interface CartService {

    public CartItem addCartItem(
            User user,
            Product product,
            String size,
            int quantity);

    public Cart findUserCart(User user);

}
