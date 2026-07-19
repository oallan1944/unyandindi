package com.allan.repository;

import com.allan.model.CouponRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link CouponRedemption}.
 *
 * <p><strong>Security notes:</strong>
 * <ul>
 *   <li>{@link #countByCouponIdAndUserIdAndReversedFalse(Long, Long)} is
 *       the ONLY correct query for enforcing {@code Coupon#usagePerCustomer}
 *       at checkout. Counting without {@code AndReversedFalse} would let a
 *       customer be permanently blocked after a legitimate cancellation, and
 *       counting via {@code coupon.getRedemptions()} in-memory (as the
 *       {@code Coupon} javadoc warns) risks operating on a stale/incomplete
 *       collection under concurrent load.</li>
 *   <li>{@link #existsByOrderId(Long)} backs the service-layer check that
 *       runs before releasing the Redis lock, as a second guard against an
 *       order ever receiving two redemptions (the unique DB index is the
 *       final guard).</li>
 *   <li>No delete method is exposed — redemptions are append-only; a
 *       cancellation is represented by flipping {@code reversed}, never by
 *       removing the row.</li>
 * </ul>
 */
@Repository
public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {

    /** Use for display/history purposes only — NOT for limit enforcement. */
    List<CouponRedemption> findByCouponIdAndUserId(Long couponId, Long userId);

    /**
     * The correct query for per-customer usage-limit enforcement at checkout:
     * excludes reversed redemptions so a genuine cancellation frees up the
     * user's allowance again.
     */
    long countByCouponIdAndUserIdAndReversedFalse(Long couponId, Long userId);

    Optional<CouponRedemption> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    Page<CouponRedemption> findByUserId(Long userId, Pageable pageable);

    Page<CouponRedemption> findByCouponId(Long couponId, Pageable pageable);

    /**
     * True if any non-reversed redemption exists for ANY coupon under the
     * given promotion. Backs {@code PromotionService.hasConfirmedRedemptions}
     * — the gate that blocks destructive rule/reward edits per
     * {@code Promotion}'s "immutable after redemptions" contract.
     */
    boolean existsByCoupon_Promotion_IdAndReversedFalse(Long promotionId);
}