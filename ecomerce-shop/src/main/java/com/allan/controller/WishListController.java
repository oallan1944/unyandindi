package com.allan.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.allan.model.Product;
import com.allan.model.User;
import com.allan.model.WishList;
import com.allan.service.ProductService;
import com.allan.service.UserService;
import com.allan.service.WishListService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishListController {

    private final WishListService wishListService;
    private final UserService userService;
    private final ProductService productService;

    @GetMapping()
    public ResponseEntity<WishList> getWishListByUserId(
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        WishList wishList = wishListService.getWishListByUserId(user);
        return ResponseEntity.ok(wishList);
    }

    @PostMapping("/add-product/{productId}")
    public ResponseEntity<WishList> addProductToWishList(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String jwt) throws Exception {
        Product product = productService.findProductById(productId);
        User user = userService.findUserByJwtToken(jwt);

        WishList updatedWishList = wishListService.addProductToWishList(
                user,
                product);

        return ResponseEntity.ok(updatedWishList);
    }

}
