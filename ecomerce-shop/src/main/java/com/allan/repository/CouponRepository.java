package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Coupon findByCode(String code);

}
