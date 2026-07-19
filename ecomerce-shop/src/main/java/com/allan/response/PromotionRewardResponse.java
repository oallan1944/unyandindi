package com.allan.response;

/**
 * Outbound response for a single promotion reward.
 * Embedded in {@link PromotionResponse#rewards()} and returned standalone
 * from reward management endpoints.
 *
 * <p><strong>Value field semantics by rewardType:</strong>
 * <pre>
 *   PERCENTAGE_OFF  →  whole-number percent e.g. 20 = "20% off"
 *   FLAT_OFF        →  UGX whole shillings e.g. 5000 = "UGX 5,000 off"
 *   FREE_SHIPPING   →  0 (unused)
 *   FREE_ITEM       →  Product ID of the free item
 * </pre>
 *
 * <p><strong>formattedValue:</strong> pre-built display string computed by
 * {@link com.allan.mapper.PromotionMapper#toRewardResponse}. The frontend
 * renders this directly without switching on {@code rewardType}:
 * <ul>
 *   <li>PERCENTAGE_OFF (20):  "20% off your order"</li>
 *   <li>FLAT_OFF (5000):      "UGX 5,000 off your order"</li>
 *   <li>FREE_SHIPPING:        "Free shipping applied"</li>
 *   <li>FREE_ITEM (101):      "Free item added to your order"</li>
 * </ul>
 *
 * <p><strong>editable flag:</strong> same contract as
 * {@link PromotionRuleResponse#editable()} — {@code false} when the parent
 * promotion has confirmed redemptions.
 */
public record PromotionRewardResponse(

        Long id,

        /** ID of the parent promotion. */
        Long promotionId,

        /** Enum name: PERCENTAGE_OFF | FLAT_OFF | FREE_SHIPPING | FREE_ITEM */
        String rewardType,

        /**
         * Raw numeric value. UGX long for FLAT_OFF.
         * Whole-number percent for PERCENTAGE_OFF.
         * 0 for FREE_SHIPPING. Product ID for FREE_ITEM.
         */
        long value,

        /**
         * Pre-formatted display string for the cart UI and order emails.
         * Computed by the mapper — never null.
         */
        String formattedValue,

        /**
         * Custom label entered by the admin/vendor.
         * May be null — use {@link #formattedValue()} for display in that case.
         */
        String label,

        /**
         * When non-null, this reward applies only to items from this seller.
         * Null = applies to all items in the promotion's scope.
         */
        Long applicableSellerId,

        /**
         * False when the parent promotion has confirmed redemptions —
         * the admin UI must disable Edit/Delete for this reward.
         */
        boolean editable
) {}