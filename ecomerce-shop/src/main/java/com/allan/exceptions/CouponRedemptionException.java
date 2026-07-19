package com.allan.exceptions;

/**
 * Thrown when a coupon fails validation or redemption for a business
 * reason (expired, exhausted, per-user limit reached, cart doesn't meet
 * rules, order already redeemed against, etc).
 *
 * <p>Carries a {@link Reason} rather than only a free-text message so the
 * controller layer can map to a precise, non-leaky client response (e.g.
 * "This code isn't valid" for the customer) while the server-side log
 * captures the specific cause for support/fraud investigation. Never
 * surface {@link #getMessage()} verbatim to the end customer — it may
 * describe internal state (e.g. exact remaining stock/allowance) that
 * shouldn't be exposed.
 */
public class CouponRedemptionException extends RuntimeException {

    public enum Reason {
        INVALID_CODE,
        EXPIRED,
        NOT_YET_ACTIVE,
        EXHAUSTED,
        PER_USER_LIMIT_REACHED,
        RULES_NOT_MET,
        BELOW_MINIMUM_ORDER_VALUE,
        ORDER_ALREADY_REDEEMED,
        LOCK_TIMEOUT
    }

    private final Reason reason;

    public CouponRedemptionException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}