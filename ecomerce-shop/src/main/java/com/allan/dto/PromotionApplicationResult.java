package com.allan.dto;

import java.util.List;

/**
 * The final, cart-level answer produced by {@code PromotionEvaluatorService}:
 * which promotion(s) actually apply, after resolving priority, stacking, and
 * exclusivity rules — not just which promotions are individually eligible.
 *
 * <p>{@code primaryPromotionId} is the lowest-priority eligible promotion
 * (or the coupon-gated promotion if a valid code was supplied).
 * {@code stackedPromotionIds} is non-empty only when every promotion
 * involved — primary included — has {@code stackable = true} and none of
 * them is {@code exclusive}. An {@code exclusive} promotion, once selected,
 * always yields an empty {@code stackedPromotionIds}, regardless of what
 * other promotions were independently eligible.
 *
 * <p>{@code totalDiscount} is the authoritative, already-capped figure to
 * charge against the order. Checkout/order services must use this value
 * directly rather than recomputing it from the individual promotions.
 */
public record PromotionApplicationResult(
        Long primaryPromotionId,
        List<Long> stackedPromotionIds,
        long totalDiscount,
        String appliedCouponCode
) {
    public static PromotionApplicationResult none() {
        return new PromotionApplicationResult(null, List.of(), 0L, null);
    }

    public boolean hasDiscount() {
        return primaryPromotionId != null && totalDiscount > 0;
    }
}