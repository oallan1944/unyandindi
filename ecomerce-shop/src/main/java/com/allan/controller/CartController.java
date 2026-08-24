package com.allan.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.allan.exceptions.ProductException;
import com.allan.model.Cart;
import com.allan.model.CartItem;
import com.allan.model.Product;
import com.allan.model.User;
import com.allan.request.AddItemRequest;
import com.allan.response.ApiResponse;
import com.allan.service.CartItemService;
import com.allan.service.CartService;
import com.allan.service.ProductService;
import com.allan.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * Customer cart CRUD.
 *
 * <p><strong>{@code updateCartItemHandler} accepts only {@code quantity}
 * from the client-supplied {@code CartItem} body</strong> — every other
 * field on that object (price, product reference, etc.) is discarded
 * before reaching {@code CartItemService}. The endpoint's request shape
 * is a full entity for backward compatibility with the existing frontend
 * payload, but nothing except quantity is trusted from it. If this
 * controller grows, prefer a dedicated request DTO with only the fields
 * you actually want a client to set, rather than widening what's read
 * off this entity.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;
    private final UserService userService;
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Cart> findUserCartHandler(@RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        Cart cart = cartService.findUserCart(user);

        return new ResponseEntity<Cart>(cart, HttpStatus.OK);
    }

    @PutMapping("/add")
    public ResponseEntity<CartItem> addItemToCart(@RequestBody AddItemRequest req,
            @RequestHeader("Authorization") String jwt) throws ProductException, Exception {

        User user = userService.findUserByJwtToken(jwt);
        Product product = productService.findProductById(req.getProductId());

        CartItem item = cartService.addCartItem(
                user,
                product,
                req.getSize(),
                req.getQuantity());

        ApiResponse res = new ApiResponse();
        res.setMessage("Item Added to Cart Successfully");

        return new ResponseEntity<>(item, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse> deleteCartItemHandler(
            @PathVariable Long cartItemId,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        cartItemService.removeCartItem(user.getId(), cartItemId);

        ApiResponse res = new ApiResponse();
        res.setMessage("Item Removed");

        return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
    }

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<CartItem> updateCartItemHandler(
            @PathVariable Long cartItemId,
            @RequestBody CartItem cartItem,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserByJwtToken(jwt);

        if (cartItem.getQuantity() <= 0) {
            // Previously fell through to updatedCartItem == null with a 202
            // response, which looks like success to the frontend even though
            // nothing happened. A quantity of 0 or less is a validation
            // failure, not an accepted no-op.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Only quantity is trusted from the client body — see class Javadoc.
        // Everything else on the incoming CartItem (price, product, etc.)
        // is intentionally dropped here rather than forwarded to the service.
        CartItem quantityUpdate = new CartItem();
        quantityUpdate.setQuantity(cartItem.getQuantity());

        CartItem updatedCartItem = cartItemService.updateCartItem(
                user.getId(),
                cartItemId,
                quantityUpdate);

        return new ResponseEntity<>(updatedCartItem, HttpStatus.ACCEPTED);
    }

}