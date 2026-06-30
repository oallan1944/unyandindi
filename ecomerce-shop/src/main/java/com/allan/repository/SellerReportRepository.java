package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.SellerReport;

public interface SellerReportRepository extends JpaRepository<SellerReport, Long> {
    SellerReport findBySellerId(Long sellerId);

}
