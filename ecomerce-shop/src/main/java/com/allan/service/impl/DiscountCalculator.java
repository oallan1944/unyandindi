package com.allan.service.impl;

import com.allan.domain.RewardType;
import com.allan.dto.CartContext;
import com.allan.dto.CartItem;
import com.allan.model.Promotion;
import com.allan.model.PromotionReward;

/**
 * Computes the UGX discount a {@link Promotion} produces for a given
 * {@link CartContext}, applying every {@link PromotionReward} on it.
 *
 * <p><strong>Why this exists as one shared class:</strong> both
 * {@code CouponServiceImpl} (for coupon pre-validation) and
 * {@code PromotionEvaluatorServiceImpl} (for automatic promotions) need the
 * exact same reward math. If each computed it independently, the two code
 * paths could silently drift — e.g. one capping against
 * {@code maximumDiscountAmount} and the other forgetting to — which would
 * mean the discount shown at cart-preview time doesn't match what's charged
 * at redemption. Both callers MUST route through this class.
 *
 * <p><strong>Security / correctness rules enforced here:</strong>
 * <ul>
 *   <li>All arithmetic is {@code long} — never {@code double}/{@code float} —
 *       matching every monetary field in this module.</li>
 *   <li>{@code FREE_SHIPPING} and {@code FREE_ITEM} rewards contribute
 *       {@code 0} to the returned monetary discount; they're fulfilled by
 *       the checkout service separately (zeroing shipping / adding a free
 *       unit), not represented as a UGX amount here.</li>
 *   <li>A reward with a non-null {@code applicableSellerId} only discounts
 *       the subset of cart lines belonging to that seller — never the
 *       whole cart — even on a platform-wide promotion.</li>
 *   <li>The final total is capped twice: against
 *       {@code Promotion.maximumDiscountAmount} (if set) and against the
 *       cart's own subtotal (a promotion can never discount more than the
 *       order is worth). Both caps are applied unconditionally — never
 *       skip them because a caller "already checked" upstream.</li>
 * </ul>
 */
public final class DiscountCalculator {

    private DiscountCalculator() {
    }

    public static long compute(Promotion promotion, CartContext cart) {
        long total = 0L;

        for (PromotionReward reward : promotion.getRewards()) {
            total += computeSingleReward(reward, cart);
        }

        if (promotion.getMaximumDiscountAmount() > 0) {
            total = Math.min(total, promotion.getMaximumDiscountAmount());
        }
        total = Math.min(total, cart.subtotal());
        return Math.max(total, 0L);
    }

    private static long computeSingleReward(PromotionReward reward, CartContext cart) {
        long eligibleBase = eligibleSubtotal(reward, cart);

        return switch (reward.getRewardType()) {
            case PERCENTAGE_OFF -> {
                long pct = reward.getValue();
                yield Math.floorDiv(eligibleBase * pct, 100L);
            }
            case FLAT_OFF -> Math.min(reward.getValue(), eligibleBase);
            case FREE_SHIPPING, FREE_ITEM -> 0L; // fulfilled by checkout, not a cart-total discount
        };
    }

    /**
     * The subtotal a reward is allowed to discount against: the whole cart
     * unless {@code applicableSellerId} restricts it to one seller's lines.
     */
    private static long eligibleSubtotal(PromotionReward reward, CartContext cart) {
        Long applicableSellerId = reward.getApplicableSellerId();
        if (applicableSellerId == null) {
            return cart.subtotal();
        }
        return cart.items().stream()
                .filter(item -> applicableSellerId.equals(item.sellerId()))
                .mapToLong(CartItem::lineTotal)
                .sum();
    }
}