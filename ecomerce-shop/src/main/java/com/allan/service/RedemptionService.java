package com.allan.service;

import com.allan.dto.CartContext;
import com.allan.dto.RedemptionOutcome;
import com.allan.exceptions.CouponRedemptionException;

/**
 * Records a coupon redemption and updates {@code Coupon.usedCount}. This is
 * the most concurrency- and fraud-sensitive service in the module — it is
 * the only place {@code usedCount} is ever incremented, per {@code Coupon}'s
 * javadoc.
 *
 * <p><strong>Security architecture:</strong>
 * <ul>
 *   <li><strong>Never trust a pre-computed discount.</strong>
 *       {@link #redeem(String, Long, Long, CartContext)} takes a fresh
 *       {@code CartContext}, not a discount amount — it must internally
 *       re-run {@code CouponService.validate(...)} /
 *       {@code PromotionEvaluatorService.evaluateWithCoupon(...)} at commit
 *       time and use that result, never a discount value computed earlier
 *       (e.g. during cart preview) or supplied by the caller. Prices,
 *       stock, and the coupon's own remaining allowance can all change
 *       between preview and checkout (TOCTOU); redemption is the one place
 *       that must be authoritative.</li>
 *   <li><strong>Order ownership.</strong> Implementations must verify the
 *       order identified by {@code orderId} actually belongs to
 *       {@code userId} before redeeming — otherwise a user could redeem a
 *       coupon against an order they don't own.</li>
 *   <li><strong>Layered concurrency control</strong> (all three, per the
 *       {@code Coupon} and {@code CouponRedemption} javadocs):
 *       <ol>
 *         <li>Redis distributed lock on the normalized coupon code —
 *             acquired first, held for the shortest possible time.</li>
 *         <li>{@code CouponRepository.findWithLockByCode} (DB pessimistic
 *             write lock) as the fallback if the Redis layer fails.</li>
 *         <li>The unique DB index on {@code coupon_redemptions.order_id} as
 *             the final guard — {@link #redeem} must treat a unique-
 *             constraint violation there as
 *             {@code CouponRedemptionException.Reason.ORDER_ALREADY_REDEEMED},
 *             not as an unhandled exception.</li>
 *       </ol>
 *   </li>
 *   <li><strong>Reversal is not customer-self-service.</strong>
 *       {@link #reverse(Long, String)} must only ever be invoked by the
 *       order-cancellation workflow or an admin action — never wired
 *       directly to a customer-facing "undo" endpoint — otherwise a user
 *       could reverse a redemption to regain coupon allowance while still
 *       keeping the goods/refund from the original order. The
 *       {@code actor} parameter is for the audit trail and must be the
 *       resolved system/admin identity, not free text from the request.</li>
 *   <li>{@link #remainingUserAllowance} and {@link #hasUserRedeemed} are
 *       read-only and safe to call as often as needed for UI state.</li>
 * </ul>
 */
public interface RedemptionService {

    /**
     * Validates and commits a redemption in one transaction, incrementing
     * {@code Coupon.usedCount} and writing an immutable
     * {@code CouponRedemption} row.
     *
     * @throws CouponRedemptionException on any validation failure (invalid,
     *         expired, exhausted, per-user limit reached, rules not met,
     *         order already redeemed, or lock timeout).
     */
    RedemptionOutcome redeem(String couponCode, Long userId, Long orderId, CartContext cart);

    /**
     * Reverses a prior redemption (order cancellation/refund): flips
     * {@code reversed = true}, stamps {@code reversedAt}, and decrements
     * {@code Coupon.usedCount} in the same transaction. See class javadoc —
     * restrict callers to trusted internal workflows only.
     */
    RedemptionOutcome reverse(Long orderId, String actor);

    /** Read-only. Excludes reversed redemptions, per {@code CouponRedemptionRepository} guidance. */
    boolean hasUserRedeemed(Long couponId, Long userId);

    /** Read-only. Remaining redemptions this user may still make against this coupon. */
    long remainingUserAllowance(Long couponId, Long userId);
}