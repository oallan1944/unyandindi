package com.allan.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "flash_sale", indexes = {
        @Index(name = "idx_flash_sale_end_time", columnList = "end_time"),
        @Index(name = "idx_flash_sale_active", columnList = "active")
})
public class FlashSale {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer discountPercent;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany
    @JoinTable(
            name = "flash_sale_products",
            joinColumns = @JoinColumn(name = "flash_sale_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    public boolean isCurrentlyLive() {
        LocalDateTime now = LocalDateTime.now();
        return active && !now.isBefore(startTime) && !now.isAfter(endTime);
    }
}