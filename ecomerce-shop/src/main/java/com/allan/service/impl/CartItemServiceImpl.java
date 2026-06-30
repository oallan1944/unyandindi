package com.allan.service.impl;

import org.springframework.stereotype.Service;

import com.allan.model.CartItem;
import com.allan.model.User;
import com.allan.repository.CartItemRepository;
import com.allan.service.CartItemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;

    @Override
    public CartItem updateCartItem(Long userId, Long id, CartItem cartItem) throws Exception {

        CartItem item = finadCartItemById(id);

        User cartItemUser = item.getCart().getUser();

        if (cartItemUser.getId().equals(userId)) {
            item.setQuantity(cartItem.getQuantity());
            item.setMrpPrice(item.getQuantity() * item.getProduct().getMrpPrice());
            item.setSellingPrice(item.getQuantity() * item.getProduct().getSellingPrice());
            return cartItemRepository.save(item);
        }
        throw new Exception("You cant update these cart Items");
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId) throws Exception {

        CartItem item = finadCartItemById(cartItemId);

        User cartItemUser = item.getCart().getUser();

        if (cartItemUser.getId().equals(userId)) {
            cartItemRepository.delete(item);
        } else
            throw new Exception("You cant delete this item");
    }

    @Override
    public CartItem finadCartItemById(Long id) throws Exception {
        return cartItemRepository.findById(id).orElseThrow(() -> new Exception("Cart item not found with id " + id));
    }

}
