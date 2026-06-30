package com.allan.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.allan.model.FlashSale;

public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {

    @Query("SELECT DISTINCT fs FROM FlashSale fs LEFT JOIN FETCH fs.products WHERE fs.id = :id")
    Optional<FlashSale> findByIdWithProducts(@Param("id") Long id);

    @Query("SELECT DISTINCT fs FROM FlashSale fs LEFT JOIN FETCH fs.products " +
           "WHERE fs.active = true AND :now BETWEEN fs.startTime AND fs.endTime")
    List<FlashSale> findCurrentlyLive(@Param("now") LocalDateTime now);

    @Query("SELECT DISTINCT fs FROM FlashSale fs LEFT JOIN FETCH fs.products")
    List<FlashSale> findAllWithProducts();
}
