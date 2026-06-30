package com.allan.service;

import com.allan.model.Product;
import com.allan.model.User;
import com.allan.model.WishList;

public interface WishListService {
    
    WishList createWishList(User user);
    WishList getWishListByUserId(User user);
    WishList addProductToWishList(User user , Product product);
} 
