package com.allan.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.allan.model.Cart;
import com.allan.model.CartItem;
import com.allan.model.Product;
import com.allan.model.User;
import com.allan.repository.CartItemRepository;
import com.allan.repository.CartRepository;
import com.allan.service.CartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public CartItem addCartItem(User user, Product product, String size, int quantity) {
        Cart cart = findUserCart(user);

        CartItem isPresent = cartItemRepository.findByCartAndProductAndSize(cart, product, size);

        if (isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUserId(user.getId());
            cartItem.setSize(size);

            long totalPrice = (long) quantity * product.getSellingPrice();
            cartItem.setSellingPrice(totalPrice);
            cartItem.setMrpPrice((long) quantity * product.getMrpPrice());

            cart.getCartItems().add(cartItem);
            cartItem.setCart(cart);

            return cartItemRepository.save(cartItem);
        }

        return isPresent;
    }

    @Override
    @Transactional
    public Cart findUserCart(User user) {

        Cart cart = cartRepository.findByUserId(user.getId());

        long totalPrice = 0;
        long totalDiscountedPrice = 0;
        int totalItem = 0;

        for (CartItem cartItem : cart.getCartItems()) {
            totalPrice += cartItem.getMrpPrice();
            totalDiscountedPrice += cartItem.getSellingPrice();
            totalItem += cartItem.getQuantity();
        }

        cart.setTotalMrpPrice(totalPrice);
        cart.setTotalItem(totalItem);
        cart.setTotalSellingPrice(totalDiscountedPrice);
        cart.setDiscount(calculateDiscountPercentage(totalPrice, totalDiscountedPrice));
        cart.setTotalItem(totalItem);

        return cart;
    }

    private long calculateDiscountPercentage(long mrpPrice, long sellingPrice) {
        if (mrpPrice <= 0) {
            return 0;
        }
        long discount = mrpPrice - sellingPrice;
        long discountPercentage = (discount / mrpPrice) * 100;

        return (long) discountPercentage;

    }

}
// package com.allan.service.impl;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.allan.model.Cart;
// import com.allan.model.CartItem;
// import com.allan.model.Product;
// import com.allan.model.User;
// import com.allan.repository.CartItemRepository;
// import com.allan.repository.CartRepository;
// import com.allan.service.CartService;

// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class CartServiceImpl implements CartService {

//     private final CartRepository cartRepository;
//     private final CartItemRepository cartItemRepository;

//     @Override
//     @Transactional
//     public CartItem addCartItem(User user, Product product, String size, int quantity) {
//         Cart cart = findUserCart(user);

//         CartItem isPresent = cartItemRepository.findByCartAndProductAndSize(cart, product, size);

//         if (isPresent == null) {
//             CartItem cartItem = new CartItem();
//             cartItem.setProduct(product);
//             cartItem.setQuantity(quantity);
//             cartItem.setUserId(user.getId());
//             cartItem.setSize(size);

//             // long, not int: product.getSellingPrice()/getMrpPrice() are now
//             // long (see Product.java), and quantity * price needs headroom
//             // for UGX magnitudes at bulk quantities.
//             long totalPrice = (long) quantity * product.getSellingPrice();
//             cartItem.setSellingPrice(totalPrice);
//             cartItem.setMrpPrice((long) quantity * product.getMrpPrice());

//             cart.getCartItems().add(cartItem);
//             cartItem.setCart(cart);

//             return cartItemRepository.save(cartItem);
//         }

//         return isPresent;
//     }

//     @Override
//     @Transactional
//     public Cart findUserCart(User user) {

//         Cart cart = cartRepository.findByUserId(user.getId());

//         long totalPrice = 0;
//         long totalDiscountedPrice = 0;
//         int totalItem = 0;

//         for (CartItem cartItem : cart.getCartItems()) {
//             totalPrice += cartItem.getMrpPrice();
//             totalDiscountedPrice += cartItem.getSellingPrice();
//             totalItem += cartItem.getQuantity();
//         }

//         cart.setTotalMrpPrice(totalPrice);
//         cart.setTotalItem(totalItem);
//         cart.setTotalSellingPrice(totalDiscountedPrice);
//         cart.setDiscount(calculateDiscountPercentage(totalPrice, totalDiscountedPrice));
//         cart.setTotalItem(totalItem);

//         return cart;
//     }

//     private long calculateDiscountPercentage(long mrpPrice, long sellingPrice) {
//         if (mrpPrice <= 0) {
//             return 0;
//         }
//         long discount = mrpPrice - sellingPrice;
//         // Multiply BEFORE dividing. The previous order — (discount / mrpPrice) * 100 —
//         // truncated to 0 in every normal case, since discount is always smaller
//         // than mrpPrice and integer division floors the result before the *100
//         // ever had a chance to matter. This silently zeroed out cart.discount
//         // on every cart.
//         long discountPercentage = (discount * 100) / mrpPrice;

//         return discountPercentage;

//     }

// }