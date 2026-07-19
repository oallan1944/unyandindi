package com.allan.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "flash_sale", indexes = {
        @Index(name = "idx_flash_sale_end_time", columnList = "end_time"),
        @Index(name = "idx_flash_sale_active", columnList = "active"),
         @Index(name = "idx_flash_sale_promotion",  columnList = "promotion_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FlashSale {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
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

      /**
     * Optional reference to a {@link Promotion} that backs this flash sale.
     * When set, the promotion evaluator uses this promotion's rules and rewards
     * rather than the {@link #discountPercent} field.
     * NULL = standalone mode.
     */
    @Column(name = "promotion_id")
    private Long promotionId;

    @ManyToMany
    @JoinTable(
            name = "flash_sale_products",
            joinColumns = @JoinColumn(name = "flash_sale_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public boolean isCurrentlyLive() {
        LocalDateTime now = LocalDateTime.now();
        return active && !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    public boolean isPromotionBacked() {
        return promotionId != null;
    }
}