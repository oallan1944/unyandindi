package com.allan.domain;

/**
 * Currencies this platform can charge in via Stripe.
 *
 * Today only UGX is used — the frontend and every current order are
 * denominated in Ugandan Shillings. This enum exists so adding a second
 * currency later (a customer paying in USD, a future multi-country
 * expansion) is a one-line addition here, not a hunt through
 * PaymentServiceImpl for hardcoded currency strings and multiplier logic.
 *
 * zeroDecimal matters specifically for Stripe: currencies like UGX and
 * JPY have no minor unit (no cents-equivalent), so Stripe expects the
 * whole-unit amount directly. Two-decimal currencies like USD expect the
 * amount in the smallest unit (cents) — amount * 100. Getting this wrong
 * doesn't fail loudly, it silently overcharges or undercharges by 100x.
 * See https://stripe.com/docs/currencies for the canonical list before
 * adding a currency this enum doesn't cover yet.
 */
public enum SupportedCurrency {

    UGX("ugx", true);

    // Add future currencies here, e.g.:
    // USD("usd", false),
    // KES("kes", false);

    private final String stripeCode;
    private final boolean zeroDecimal;

    SupportedCurrency(String stripeCode, boolean zeroDecimal) {
        this.stripeCode = stripeCode;
        this.zeroDecimal = zeroDecimal;
    }

    public String getStripeCode() {
        return stripeCode;
    }

    public boolean isZeroDecimal() {
        return zeroDecimal;
    }

    /**
     * Converts a whole-currency-unit amount into whatever unit Stripe
     * expects for this currency — a no-op for zero-decimal currencies,
     * multiplies by 100 for standard two-decimal currencies. Centralizing
     * this here means every call site gets the right behavior automatically
     * as soon as a currency is added to this enum, instead of each caller
     * needing to remember which currencies need *100 and which don't.
     */
    public long toStripeUnitAmount(long amount) {
        return zeroDecimal ? amount : amount * 100;
    }
}