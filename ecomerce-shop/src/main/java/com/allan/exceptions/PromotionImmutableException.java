package com.allan.exceptions;

/**
 * Thrown when a caller attempts a destructive edit (rule change, reward
 * value change) on a {@code Promotion} that already has confirmed
 * {@code CouponRedemption}s.
 *
 * <p>Per {@code Promotion}'s javadoc, only {@code status}, {@code endsAt},
 * and {@code priority} remain mutable once redemptions exist — significant
 * changes require creating a new promotion instead. This exception is the
 * enforcement point for that contract; {@code PromotionService} must check
 * for confirmed redemptions before every rule/reward mutation, not just on
 * promotion status transitions.
 */
public class PromotionImmutableException extends RuntimeException {

    public PromotionImmutableException(Long promotionId) {
        super("Promotion " + promotionId + " has confirmed redemptions and can no longer have "
                + "its rules or rewards modified; create a new promotion instead");
    }
}