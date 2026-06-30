package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.WishList;

public interface WishListRepository extends JpaRepository<WishList, Long> {

    WishList findByUserId(Long userId);

}
