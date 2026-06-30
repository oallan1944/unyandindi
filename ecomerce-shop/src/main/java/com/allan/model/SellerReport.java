package com.allan.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SellerReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Seller seller;

    private Long totalEarnings = 0L;

    private Long totalSales = 0L;

    private Long totalRefunds = 0L;

    private Long totalTax = 0L;

    private Long netEarnings = 0L;

    private Long totalOrders = 0L;

    private Long canceledOrders = 0L;

    private Long totalTransactions = 0L;

    private Long completedOrders = 0L;

    private Long pendingOrders = 0L;

    private Double totalRevenue = 0.0;

    private Long totalProducts = 0L;
    
    private LocalDateTime generatedAt;

}
