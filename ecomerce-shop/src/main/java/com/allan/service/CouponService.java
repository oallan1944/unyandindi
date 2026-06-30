package com.allan.service;

import java.util.List;

import com.allan.model.Cart;
import com.allan.model.Coupon;
import com.allan.model.User;

public interface CouponService {

    Cart applyCoupon(String code, double orderValue, User user) throws Exception;

    Cart removeCoupon(String code, User user) throws Exception;

    Coupon findCouponById(Long id) throws Exception;

    Coupon createCoupon(Coupon coupon);

    List<Coupon> findAllCoupons();

    void deleteCoupon(Long id) throws Exception;

}
