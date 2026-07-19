package com.allan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.allan.model.Coupon;

import jakarta.persistence.LockModeType;

/**
 * Repository for {@link Coupon}.
 *
 * <p><strong>Security notes:</strong>
 * <ul>
 *   <li>{@code code} MUST be normalized to upper case by the caller
 *       ({@code CouponServiceImpl}) before calling any method here. This
 *       repository does not normalize on your behalf — it trusts the input
 *       it's given, so passing mixed-case input will silently miss matches
 *       rather than being coerced, which is intentional (fail closed).</li>
 *   <li>{@link #findWithLockByCode(String)} acquires a DB-level pessimistic
 *       write lock as the second line of defence behind the Redis lock
 *       described on {@link Coupon}. Use it only inside a transaction that
 *       will increment {@code usedCount} and hold the lock for the shortest
 *       time possible.</li>
 *   <li>{@link #findByIdAndPromotionSellerId(Long, Long)} lets the service
 *       layer verify a seller owns the coupon's parent promotion before
 *       allowing mutation (e.g. disabling a coupon), without a separate
 *       existence-then-ownership round trip.</li>
 * </ul>
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    List<Coupon> findByPromotionId(Long promotionId);

    /** Paged variant — used by admin/seller coupon-listing endpoints. */
    Page<Coupon> findByPromotionId(Long promotionId, Pageable pageable);

    /**
     * Ownership-scoped lookup for seller-facing coupon management endpoints.
     * Returns empty if the coupon doesn't exist or its parent promotion
     * belongs to a different seller.
     *
     * <p>{@code Promotion.vendorId} stores a {@link com.allan.model.Seller}
     * ID — there is no {@code Vendor} entity in this project — so this is
     * expressed via {@code @Query} rather than a derived {@code ...VendorId}
     * method name, to keep the repository API consistent with the rest of
     * the codebase's "Seller" terminology.
     */
    @Query("select c from Coupon c where c.id = :id and c.promotion.sellerId = :sellerId")
    Optional<Coupon> findByIdAndPromotionSellerId(@Param("id") Long id, @Param("sellerId") Long sellerId);

    /**
     * Locks the coupon row for update. Call only within a transaction
     * immediately before incrementing {@code usedCount}, and commit/release
     * quickly — this is a fallback safety net, not the primary concurrency
     * control (the Redis lock is).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.code = :code")
    Optional<Coupon> findWithLockByCode(@Param("code") String code);
}