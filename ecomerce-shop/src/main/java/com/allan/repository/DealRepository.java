package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.Deal;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

public interface DealRepository extends JpaRepository <Deal, Long> {

    @Query("SELECT d FROM Deal d LEFT JOIN FETCH d.category")
    List<Deal> findAllWithCategory();


}
