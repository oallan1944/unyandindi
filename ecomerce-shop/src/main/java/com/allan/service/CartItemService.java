package com.allan.service;

import com.allan.model.CartItem;

public interface CartItemService {

    CartItem updateCartItem(Long userId, Long id, CartItem cartItem) throws Exception;

    void removeCartItem(Long userId, Long cartItemId) throws Exception;

    CartItem finadCartItemById(Long id) throws Exception;
}
